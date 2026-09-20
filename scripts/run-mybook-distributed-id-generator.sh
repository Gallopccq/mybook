#!/bin/bash
# 兼容旧入口：实际逻辑已统一在 run.sh
# 用法: run-mybook-distributed-id-generator.sh [start|stop|restart|status|build]
exec "$(dirname "$0")/run.sh" mybook-distributed-id-generator "$@"
