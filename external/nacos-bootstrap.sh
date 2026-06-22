docker run nacos
mkdir nacos
docker cp nacos:/home/nacos/conf nacos/conf
docker cp nacos:/home/nacos/data nacos/data
docker cp nacos:/home/nacos/logs nacos/logs

# vim nacos/conf/application.properties