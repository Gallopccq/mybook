insert into sys_user(user_id,user_name,password,nick_name,status)
values(1,'admin','$2a$10$ySX3z/St8BDUV3.V2lcLp.53qx9eNILGlj9SdSkEbb6.YTHIgPwU6','系统管理员','0');

insert into sys_role(role_id,role_key,status) values(1,'admin','0');
insert into sys_user_role(user_id,role_id) values(1,1);

insert into sys_menu(menu_id,parent_id,menu_name,path,perms,status,order_num) values
(1,0,'工作台','/dashboard','system:dashboard:view','0',1),
(2,0,'系统管理','/system','','0',2),
(3,2,'用户管理','/system/user','system:user:list','0',1),
(4,2,'菜单管理','/system/menu','system:menu:list','0',2);

insert into sys_role_menu(role_id,menu_id) values
(1,1),(1,2),(1,3),(1,4);