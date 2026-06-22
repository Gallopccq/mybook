docker run mysql
mkdir mysql/data
docker cp mysql8.0:/var/lib/mysql mysql/data

mysql -uroot -p123456 -h 172.21.0.2 < nacos/conf/mysql-schema.sql