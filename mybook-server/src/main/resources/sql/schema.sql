create table if not exists sys_user(
  user_id bigint primary key,
  user_name varchar(64) not null unique,
  password varchar(255) not null,
  nick_name varchar(64) not null,
  status char(1) not null default '0'
);

create table if not exists sys_role(
  role_id bigint primary key,
  role_key varchar(64) not null,
  status char(1) not null default '0'
);

create table if not exists sys_user_role(
  user_id bigint not null,
  role_id bigint not null,
  primary key(user_id, role_id)
);

create table if not exists sys_menu(
  menu_id bigint primary key,
  parent_id bigint not null,
  menu_name varchar(64) not null,
  path varchar(128) not null,
  perms varchar(128),
  status char(1) not null default '0',
  order_num int not null default 0
);

create table if not exists sys_role_menu(
  role_id bigint not null,
  menu_id bigint not null,
  primary key(role_id, menu_id)
);