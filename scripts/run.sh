#!/usr/bin/env bash
#
# 编译并以可执行 jar 的方式管理 mybook 各服务。
#
# 用法:
#   scripts/run.sh                                  显示帮助 + 各服务状态
#   scripts/run.sh status                           查看运行状态
#   scripts/run.sh build user                       只编译（含依赖模块）
#   scripts/run.sh start user                       只启动（不重新编译）
#   scripts/run.sh stop user                        停止
#   scripts/run.sh user                             默认动作 restart：编译 + 启动
#   scripts/run.sh restart all                      全部重新编译并启动
#   scripts/run.sh clean user                       清理 target
#   scripts/run.sh log user                         跟踪日志
#   scripts/run.sh user -- --server.port=8084       追加 Spring 启动参数
#   scripts/run.sh -n restart user                  只打印将要执行的命令，不真正执行
#
# 模块名可用短名（user / note / gateway ...）或完整名（mybook-user ...）。
#
# 环境变量:
#   JAVA_BIN    指定 java 可执行文件（优先级最高）
#   JAVA_HOME   指定 JDK（默认沿用，未设置时自动查找 17）
#   JVM_OPTS    额外 JVM 参数（按空格拆分），如 "-Xmx512m -Dfile.encoding=UTF-8"
#   APP_ARGS    额外应用参数（按空格拆分），等价于追加到 jar 之后
#   MVN_FLAGS   额外 Maven 参数，如 "-o -T 1C"
#   WITH_TESTS=1 编译时执行单元测试（默认跳过）
#   DRY_RUN=1   等价于 -n / --dry-run

set -uo pipefail

SCRIPT_DIR=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
ROOT_DIR=$(cd -- "$SCRIPT_DIR/.." && pwd)
LOG_DIR="$ROOT_DIR/logs"

JVM_OPTS="${JVM_OPTS:-}"
APP_ARGS="${APP_ARGS:-}"
MVN_FLAGS="${MVN_FLAGS:-}"
DRY_RUN="${DRY_RUN:-0}"

declare -A MODULES=(
  [gateway]="mybook-gateway"
  [auth]="mybook-auth"
  [user]="mybook-user/mybook-user-biz"
  [note]="mybook-note/mybook-note-biz"
  [oss]="mybook-oss/mybook-oss-biz"
  [kv]="mybook-kv/mybook-kv-biz"
  [user-relation]="mybook-user-relation/mybook-user-relation-biz"
  [distributed-id-generator]="mybook-distributed-id-generator/mybook-distributed-id-generator-biz"
)

declare -A ALIASES=(
  [id-generator]="distributed-id-generator"
)

MODULE_ORDER=(
  gateway auth user note oss kv user-relation distributed-id-generator
)

ENABLED=1
ACTION_NAMES="build start stop restart status clean log help"
DEFAULT_ACTION="restart"

die() { echo "错误: $*" >&2; exit 1; }

# 把命令数组拼成可直接复制执行的字符串（含空格的参数会加引号）
cmd_str() {
  local a out=""
  for a in "$@"; do
    case "$a" in
      *[[:space:]\'\"]*) a="'${a//\'/\'\\\'\'}'" ;;
    esac
    out+="${out:+ }$a"
  done
  echo "$out"
}

usage() {
  awk 'NR>1 && /^#/ { sub(/^# ?/, ""); print; next } NR>1 { exit }' "${BASH_SOURCE[0]}"
}

pick_java() {
  local candidate major
  if [ -n "${JAVA_BIN:-}" ]; then
    [ -x "$JAVA_BIN" ] || die "JAVA_BIN 不可执行: $JAVA_BIN"
    echo "$JAVA_BIN"
    return
  fi
  for candidate in "${JAVA_HOME:+$JAVA_HOME/bin/java}" "$HOME"/.sdkman/candidates/java/17*/bin/java /usr/lib/jvm/java-17*/bin/java; do
    [ -n "$candidate" ] && [ -x "$candidate" ] && { echo "$candidate"; return; }
  done
  command -v java || die "找不到 java，请设置 JAVA_HOME"
}

JAVA=$(pick_java) || exit 1

warn_java_version() {
  local major
  major=$("$JAVA" -version 2>&1 | sed -n '1s/.*version "\([0-9]*\).*/\1/p')
  if [ -n "$major" ] && [ "$major" -gt 17 ]; then
    echo "提示: 当前使用 $JAVA (JDK $major)，项目编译目标是 JDK 17，建议设置 JAVA_HOME 指向 17。" >&2
  fi
}

module_path() { echo "${MODULES[$1]}"; }
app_name() { basename "$(module_path "$1")"; }
pid_file() { echo "$LOG_DIR/$(app_name "$1").pid"; }
log_file() { echo "$LOG_DIR/$(app_name "$1").log"; }

# 把用户输入（短名/完整名）解析为模块键
resolve_module() {
  local name=$1
  name=${name#mybook-}
  name=${ALIASES[$name]:-$name}
  [ -n "${MODULES[$name]:-}" ] || die "未知模块 '$1'，可用模块: ${MODULE_ORDER[*]}"
  echo "$name"
}

# 目标 jar：target 下最新的可执行 jar（排除 sources/javadoc/原始 jar）
find_jar() {
  local dir="$ROOT_DIR/$(module_path "$1")/target" jar
  [ -d "$dir" ] || return 1
  jar=$(ls -t "$dir"/*.jar 2>/dev/null | grep -v -e '-sources\.jar$' -e '-javadoc\.jar$' | head -n1)
  [ -n "$jar" ] && echo "$jar"
}

running_pid() {
  local m=$1 pf pid
  pf=$(pid_file "$m")
  if [ -f "$pf" ]; then
    pid=$(cat "$pf" 2>/dev/null)
    if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
      echo "$pid"
      return 0
    fi
    rm -f "$pf"
  fi
  pid=$(pgrep -f -- "-jar .*/$(app_name "$m")-[0-9][^ ]*\.jar" 2>/dev/null | head -n1)
  [ -n "$pid" ] && { echo "$pid"; return 0; }
  return 1
}

app_port() {
  local yml port
  yml="$ROOT_DIR/$(module_path "$1")/src/main/resources/application.yml"
  [ -f "$yml" ] || { echo "-"; return; }
  port=$(awk '/^server:/ {on=1; next} /^[^[:space:]]/ {on=0} on && /^[[:space:]]+port:[[:space:]]*[0-9]+/ {print $2; exit}' "$yml")
  echo "${port:--}"
}

build_modules() {
  local pl="" m
  for m in "$@"; do pl+="$(module_path "$m"),"; done
  pl=${pl%,}
  local -a cmd=(mvn -ntp -pl "$pl" -am)
  [ "${WITH_TESTS:-0}" = "1" ] || cmd+=(-DskipTests)
  cmd+=(package)
  [ -z "$MVN_FLAGS" ] || cmd+=($MVN_FLAGS)
  echo ">>> cd $ROOT_DIR && $(cmd_str "${cmd[@]}")"
  if [ "$DRY_RUN" = 1 ]; then
    echo "    (dry-run: 未执行)"
    return 0
  fi
  ( cd "$ROOT_DIR" && "${cmd[@]}" ) || return 1
}

do_build() {
  build_modules "$@" || die "编译失败"
}

do_start() {
  local m=$1 jar pid log
  if pid=$(running_pid "$m"); then
    if [ "$DRY_RUN" = 1 ]; then
      echo "$(app_name "$m"): 当前正在运行 (pid $pid)，实际执行时会先 stop 再启动"
    else
      echo "$(app_name "$m"): 已在运行 (pid $pid)，跳过启动"
      return 0
    fi
  fi
  jar=$(find_jar "$m") || true
  if [ -z "$jar" ]; then
    [ "$DRY_RUN" = 1 ] || die "$(app_name "$m"): 找不到 jar，请先执行 build 或 restart"
    jar="<target 下暂无 jar，需先 build>"
  fi
  log=$(log_file "$m")
  mkdir -p "$LOG_DIR"
  if [ "$DRY_RUN" = 1 ]; then
    echo ">>> cd $ROOT_DIR && nohup $(cmd_str "$JAVA" $JVM_OPTS -jar "$jar" $APP_ARGS) > $log 2>&1 &"
    printf '    echo $! > %s\n' "$(pid_file "$m")"
    return 0
  fi
  nohup "$JAVA" $JVM_OPTS -jar "$jar" $APP_ARGS > "$log" 2>&1 &
  pid=$!
  echo "$pid" > "$(pid_file "$m")"
  sleep 1
  if kill -0 "$pid" 2>/dev/null; then
    echo "$(app_name "$m"): 已启动 (pid $pid, 端口 $(app_port "$m"))，日志: $log"
  else
    rm -f "$(pid_file "$m")"
    echo "$(app_name "$m"): 启动失败，日志尾部:" >&2
    tail -n 20 "$log" >&2
    return 1
  fi
}

do_stop() {
  local m=$1 pid i
  if ! pid=$(running_pid "$m"); then
    [ "$DRY_RUN" = 1 ] || rm -f "$(pid_file "$m")"
    echo "$(app_name "$m"): 未运行"
    return 0
  fi
  if [ "$DRY_RUN" = 1 ]; then
    echo ">>> kill $pid    # $(app_name "$m")，若 10s 未退出则 kill -9"
    [ ! -f "$(pid_file "$m")" ] || echo ">>> rm -f $(pid_file "$m")"
    return 0
  fi
  kill "$pid" 2>/dev/null
  for i in $(seq 1 10); do
    kill -0 "$pid" 2>/dev/null || break
    sleep 1
  done
  if kill -0 "$pid" 2>/dev/null; then
    echo "$(app_name "$m"): 优雅退出超时，强制结束 (pid $pid)"
    kill -9 "$pid" 2>/dev/null
  else
    echo "$(app_name "$m"): 已停止 (pid $pid)"
  fi
  rm -f "$(pid_file "$m")"
}

do_status() {
  local m pid state jar
  echo "服务状态:"
  for m in "${MODULE_ORDER[@]}"; do
    if pid=$(running_pid "$m"); then
      state="RUNNING pid=$pid"
    else
      state="STOPPED"
    fi
    jar=$(find_jar "$m") || jar="未编译"
    printf '  %-36s %-16s 端口 %-6s %s\n' "$(app_name "$m")" "$state" "$(app_port "$m")" "${jar#"$ROOT_DIR/"}"
  done
}

do_clean() {
  local pl="" m
  for m in "$@"; do pl+="$(module_path "$m"),"; done
  pl=${pl%,}
  local -a cmd=(mvn -ntp -pl "$pl" clean)
  [ -z "$MVN_FLAGS" ] || cmd+=($MVN_FLAGS)
  echo ">>> cd $ROOT_DIR && $(cmd_str "${cmd[@]}")"
  if [ "$DRY_RUN" = 1 ]; then
    echo "    (dry-run: 未执行)"
    return 0
  fi
  ( cd "$ROOT_DIR" && "${cmd[@]}" ) || die "clean 失败"
}

do_log() {
  local log
  log=$(log_file "$1")
  [ -f "$log" ] || die "$(app_name "$1"): 日志不存在 ($log)"
  if [ "$DRY_RUN" = 1 ]; then
    echo ">>> tail -n 100 -f $log"
    return 0
  fi
  tail -n 100 -f "$log"
}

main() {
  local action="" modules=() passthru=() arg m
  local argc=$#
  while [ $# -gt 0 ]; do
    arg=$1
    case "$arg" in
      --) shift; passthru=("$@"); break ;;
      -h|--help|help) action="help" ;;
      -n|--dry-run) DRY_RUN=1 ;;
      all) for m in "${MODULE_ORDER[@]}"; do modules+=("$m"); done ;;
      *)
        if [ -z "$action" ] && [[ " $ACTION_NAMES " == *" $arg "* ]]; then
          action="$arg"
        else
          modules+=("$(resolve_module "$arg")")
        fi
        ;;
    esac
    shift
  done

  case "$action" in
    help) usage; return 0 ;;
    status) do_status; return 0 ;;
  esac

  [ ${#modules[@]} -eq 0 ] && modules=("${MODULE_ORDER[@]}")

  if [ -z "$action" ]; then
    if [ "$argc" -eq 0 ]; then
      usage
      echo
      do_status
      return 0
    fi
    action="$DEFAULT_ACTION"
  fi

  warn_java_version

  [ "$DRY_RUN" = 1 ] && echo "== dry-run: 仅打印将要执行的命令 =="

  [ ${#passthru[@]} -gt 0 ] && APP_ARGS+=" ${passthru[*]}"

  case "$action" in
    build) do_build "${modules[@]}" ;;
    start) for m in "${modules[@]}"; do do_start "$m" || ENABLED=0; done ;;
    stop) for m in "${modules[@]}"; do do_stop "$m"; done ;;
    restart)
      do_build "${modules[@]}" || exit 1
      for m in "${modules[@]}"; do do_stop "$m"; done
      for m in "${modules[@]}"; do do_start "$m" || ENABLED=0; done
      ;;
    clean) do_clean "${modules[@]}" ;;
    log) do_log "${modules[0]}" ;;
    *) die "未知动作 '$action'" ;;
  esac

  [ "$ENABLED" = 1 ] || exit 1
}

main "$@"
