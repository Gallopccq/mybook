-- 权限数据
INSERT INTO `mybook`.`t_permission` (`id`, `parent_id`, `name`, `type`, `menu_url`, `menu_icon`, `sort`, `permission_key`, `status`, `create_time`, `update_time`, `is_deleted`) VALUES (1, 0, '发布笔记', 3, '', '', 1, 'app:note:publish', 0, now(), now(), b'0');
INSERT INTO `mybook`.`t_permission` (`id`, `parent_id`, `name`, `type`, `menu_url`, `menu_icon`, `sort`, `permission_key`, `status`, `create_time`, `update_time`, `is_deleted`) VALUES (2, 0, '发布评论', 3, '', '', 2, 'app:comment:publish', 0, now(), now(), b'0');

-- 角色数据
INSERT INTO `mybook`.`t_role` (`id`, `role_name`, `role_key`, `status`, `sort`, `remark`, `create_time`, `update_time`, `is_deleted`) VALUES (1, '普通用户', 'common_user', 0, 1, '', now(), now(), b'0');

-- 关联数据
INSERT INTO `mybook`.`t_role_permission_rel` (`id`, `role_id`, `permission_id`, `create_time`, `update_time`, `is_deleted`) VALUES (1, 1, 1, now(), now(), b'0');
INSERT INTO `mybook`.`t_role_permission_rel` (`id`, `role_id`, `permission_id`, `create_time`, `update_time`, `is_deleted`) VALUES (2, 1, 2, now(), now(), b'0');

-- Leaf
insert into leaf_alloc(biz_tag, max_id, step, description) values('leaf-segment-test', 1, 2000, 'Test leaf Segment Mode Get Id')
INSERT INTO `leaf`.`leaf_alloc` (`biz_tag`, `max_id`, `step`, `description`, `update_time`) VALUES ('leaf-segment-mybook-id', 10100, 2000, '小哈书 ID', now());
INSERT INTO `leaf`.`leaf_alloc` (`biz_tag`, `max_id`, `step`, `description`, `update_time`) VALUES ('leaf-segment-user-id', 100, 2000, '用户 ID', now());

-- Channle
INSERT INTO `mybook`.`t_channel` (`name`, `create_time`, `update_time`, `is_deleted`) VALUES ('美食', now(), now(), 0);
INSERT INTO `mybook`.`t_channel` (`name`, `create_time`, `update_time`, `is_deleted`) VALUES ('娱乐', now(), now(), 0);

-- Topic
INSERT INTO `mybook`.`t_topic` (`name`, `create_time`, `update_time`, `is_deleted`) VALUES ('高分美剧推荐', now(), now(), 0);
INSERT INTO `mybook`.`t_topic` (`name`, `create_time`, `update_time`, `is_deleted`) VALUES ('下饭综艺推荐', now(), now(), 0);

-- ChannelTopicRel
INSERT INTO `mybook`.`t_channel_topic_rel` (`channel_id`, `topic_id`, `create_time`, `update_time`) VALUES (2, 1, now(), now());
INSERT INTO `mybook`.`t_channel_topic_rel` (`channel_id`, `topic_id`, `create_time`, `update_time`) VALUES (2, 2, now(), now());

