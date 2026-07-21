#!/bin/bash
APP_PURE=mybook-auth
APP_NAME=target/mybook-auth*.jar

pid=$(ps -ef | grep $APP_NAME | grep -v grep | awk '{print $2}')
case "$1" in
 start)
   cd ../$APP_PURE && mvn package
   nohup java -jar $APP_NAME > $APP_PURE.log 2>&1 &
   ;;
 stop)
   [ -n "$pid" ] && kill -9 $pid
   ;;
 restart)
   [ -n "$pid" ] && kill -9 $pid
   cd ../$APP_PURE && mvn package
   nohup java -jar $APP_NAME > $APP_PURE.log 2>&1 &
   ;;
esac

