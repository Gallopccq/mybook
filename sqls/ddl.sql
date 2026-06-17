-- 用户服务 --
-- 用户表
use mybook;
create table if not exists `t_user` (
	`id` bigint unsigned not null auto_increment comment '主键ID',
    `mybook_id` varchar(15) not null comment 'Mybook ID（唯一凭证），可修改',
    `phone` varchar(11) not null comment '手机号',
    `password` varchar(64) default null comment '加密后的密码',
    `birthday` date default null comment '生日',
    `nickname` varchar(24) not null comment '昵称',
    `avatar` varchar(120) default null comment '头像链接',
    `background_img` varchar(120) default null comment '背景图片',
    `sex` tinyint default '0' comment '男1女0',
    `status` tinyint not null default '0' comment '状态（0：启用；1：禁用)',
    `introduction` varchar(100) default null comment '个人简介',
    `create_time` datetime not null default current_timestamp  comment '创建时间',
    `update_time` datetime not null default current_timestamp comment '更新时间',
    `is_deleted` bit(1) not null default b'0' comment '逻辑删除（0：未删除；1：已删除）',
    primary key (`id`) using BTREE,
    unique key `uk_mybook_id` (`mybook_id`),
    unique key `uk_phone` (`phone`)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_unicode_ci comment='用户表';

-- 鉴权服务 --
-- 角色表

create table if not exists `t_role` (
    `id` bigint unsigned not null auto_increment comment '主键ID',
    `role_name` varchar(32) collate utf8mb4_unicode_ci not null COMMENT '角色名',
    `role_key` varchar(32) collate utf8mb4_unicode_ci not null comment '角色唯一标识',
    `status` tinyint not null default '0' comment '状态(0启用1禁用)',
    `sort` int unsigned not null default 0 comment '管理系统中的显示顺序',
    `remark` varchar(255) collate utf8mb4_unicode_ci default null comment '备注',
    `create_time` datetime not null default current_timestamp comment '创建时间',
    `update_time` datetime not null default current_timestamp comment '最后一次更新时间',
    `is_deleted` bit(1) not null default b'0' comment '逻辑删除(0未删除1已删除)',
    primary key (`id`) using btree,
    unique key `uk_role_key` (`role_key`)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci comment '角色表';

-- 权限表

create table if not exists `t_permission` (
    `id` bigint unsigned not null auto_increment comment '主键ID',
    `parent_id` bigint unsigned not null default '0' comment '父ID',
    `name` varchar(16) collate utf8mb4_unicode_ci not null comment '权限名称',
    `type` tinyint unsigned not null comment '类型(1目录2菜单3按钮)',
    `menu_url` varchar(32) collate utf8mb4_unicode_ci not null default '' comment '菜单路由',
    `menu_icon` varchar(255) collate utf8mb4_unicode_ci not null default '' comment '菜单图标',
    `sort` int unsigned not null default 0 comment '管理系统中的显示顺序',
    `permission_key` varchar(64) collate utf8mb4_unicode_ci not null comment '权限标识',
    `status` tinyint unsigned not null default '0' comment '状态(0启用1禁用)',
    `create_time` datetime not null default current_timestamp comment '创建时间',
    `update_time` datetime not null default current_timestamp comment '更新时间',
    `is_deleted` bit(1) not null default b'0' comment '逻辑删除(0未删除 1已删除)',
    primary key (`id`) using btree
)engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci comment '权限表';

-- 用户角色关联表

create table if not exists `t_user_role_rel` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
    `role_id` bigint unsigned NOT NULL COMMENT '角色ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除(0未删除 1已删除)',
    PRIMARY KEY (`id`) USING BTREE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色表';

-- 角色权限表

create table if not exists `t_role_permission_rel` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_id` bigint unsigned NOT NULL COMMENT '角色ID',
    `permission_id` bigint unsigned NOT NULL COMMENT '权限ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '逻辑删除(0未删除 1已删除)',
    primary key (`id`) using btree
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户权限表';

-- 笔记服务 --
-- 频道表：

CREATE TABLE IF NOT EXISTS `t_channel` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(12) character set utf8mb4 collate utf8mb4_unicode_ci not null comment '频道名称',
    `create_time` datetime not null default current_timestamp comment '创建时间',
    `update_time` datetime not null default current_timestamp comment '更新时间',
    `is_deleted` bit(1) not null default b'0' comment '逻辑删除：0未删除1已删除',
    primary key (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='频道表';

-- 话题表：

CREATE TABLE IF NOT EXISTS `t_topic` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(12) character set utf8mb4 collate utf8mb4_unicode_ci not null comment '话题名称',
    `create_time` datetime not null default current_timestamp comment '创建时间',
    `update_time` datetime not null default current_timestamp comment '更新时间',
    `is_deleted` bit(1) not null default b'0' comment '逻辑删除：0未删除1已删除',
    primary key (`id`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='话题表';

-- 话题-频道关联表

CREATE TABLE IF NOT EXISTS `t_channel_topic_rel` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `channel_id` bigint(11) unsigned NOT NULL COMMENT '频道ID',
  `topic_id` bigint(11) unsigned NOT NULL COMMENT '话题ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='频道-话题关联表';

-- 笔记表

create table IF NOT EXISTS `t_note` (
	`id` bigint(11) unsigned auto_increment comment '主键ID',
    `title` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci not null comment '标题',
    `is_content_empty` bit(1) not null default b'0' comment '内容是否为空：0非空1空',
    `creator_id` bigint(11) unsigned not null comment '发布者ID',
    `topic_id` bigint(11) unsigned default null comment '话题ID',
    `is_top` bit(1) not null default b'0' comment '是否置顶：0未指定1置顶',
    `type` tinyint(2) default '0' comment '类型：0图文1视频',
    `img_uris` varchar(660) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci default null comment '笔记图片链接，逗号分隔',
    `video_uris` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci default null comment '视频链接',
    `visible` tinyint(2) default '0' comment '可见范围：0公开1仅自己可见',
    `create_time` datetime not null default current_timestamp comment '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `status` tinyint(2) not null default '0' comment '状态：0待审核1正常2被删除3被下架',
    primary key (`id`) using btree,
    key `idx_creator_id` (`creator_id`),
    key `idx_topic_id` (`topic_id`),
    key `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记表';

-- 计数服务 --
-- 点赞表

create table IF NOT EXISTS `t_note_like` (
	`id` bigint(11) unsigned NOT NULL auto_increment comment '主键ID',
    `user_id` bigint(11) unsigned not null comment '用户ID',
    `note_id` bigint(11) unsigned not null comment '笔记ID',
    `create_time` datetime not null default current_timestamp comment '创建时间',
    `status` tinyint(2) not null default '0' comment '点赞状态(0:取消点赞;1:点赞)',
    primary key (`id`) using btree,
    unique key `uk_user_id_note_id` (`user_id`, `note_id`)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_unicode_ci comment '笔记点赞表';

-- 笔记收藏表

create table IF NOT EXISTS `t_note_collection`(
	`id` bigint(11) unsigned NOT NULL auto_increment comment '主键ID',
    `user_id` bigint(11) unsigned not null comment '用户ID',
    `note_id` bigint(11) unsigned not null comment '笔记ID',
    `create_time` datetime not null default current_timestamp comment '创建时间',
    `status` tinyint(2) not null default '0' comment '收藏状态(0:取消收藏;1:收藏)',
    primary key (`id`) using btree,
    unique key `uk_user_id_note_id` (`user_id`, `note_id`)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_unicode_ci comment '笔记收藏表';


-- 笔记计数表

create table `t_note_count` (
	`id` bigint(11) unsigned not null auto_increment comment '主键ID',
    `note_id` bigint(11) unsigned not null comment '笔记ID',
    `like_total` bigint(11) unsigned not null default '0' comment '点赞总数',
    `collect_total` bigint(11) unsigned not null default '0' comment '收藏总数',
    `comment_total` bigint(11) unsigned not null default '0' comment '评论总数',
    primary key (`id`) using btree,
    unique key `uk_note_id` (`note_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci comment '笔记计数表';

-- 用户计数表

create table if not exists `t_user_count` (
	`id` bigint(11) unsigned not null auto_increment comment '主键ID',
    `user_id` bigint(11) unsigned not null  comment '用户ID',
    `fans_total` bigint(11) not null default '0' comment '粉丝总数',
    `following_total` bigint(11) unsigned not null default '0' comment '关注总数',
    `note_total` bigint(11) unsigned not null default '0' comment '发布笔记总数',
    `like_total` bigint(11) unsigned not null default '0' comment '获得点赞总数',
    `collect_total` bigint(11) unsigned not null default '0' comment '获得收藏总数',
    primary key (`id`) using btree,
    unique key `uk_user_id` (`user_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户计数表';

