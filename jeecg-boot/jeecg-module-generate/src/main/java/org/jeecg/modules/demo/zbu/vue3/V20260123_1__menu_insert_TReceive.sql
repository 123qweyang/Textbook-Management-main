-- 注意：该页面对应的前台目录为views/zbu文件夹下
-- 如果你想更改到其他目录，请修改sql中component字段对应的值


-- 主菜单
INSERT INTO sys_permission(id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, hide_tab, description, status, del_flag, rule_flag, create_by, create_time, update_by, update_time, internal_or_external)
VALUES ('176916875923701', NULL, '领取表', '/zbu/tReceiveList', 'zbu/TReceiveList', NULL, NULL, 0, NULL, '1', 0.00, 0, NULL, 1, 0, 0, 0, 0, NULL, '1', 0, 0, 'admin', '2026-01-23 19:45:59', NULL, NULL, 0);

-- 新增
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176916875923702', '176916875923701', '添加领取表', NULL, NULL, 0, NULL, NULL, 2, 'zbu:t_receive:add', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-23 19:45:59', NULL, NULL, 0, 0, '1', 0);

-- 编辑
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176916875923703', '176916875923701', '编辑领取表', NULL, NULL, 0, NULL, NULL, 2, 'zbu:t_receive:edit', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-23 19:45:59', NULL, NULL, 0, 0, '1', 0);

-- 删除
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176916875923704', '176916875923701', '删除领取表', NULL, NULL, 0, NULL, NULL, 2, 'zbu:t_receive:delete', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-23 19:45:59', NULL, NULL, 0, 0, '1', 0);

-- 批量删除
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176916875923705', '176916875923701', '批量删除领取表', NULL, NULL, 0, NULL, NULL, 2, 'zbu:t_receive:deleteBatch', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-23 19:45:59', NULL, NULL, 0, 0, '1', 0);

-- 导出excel
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176916875923706', '176916875923701', '导出excel_领取表', NULL, NULL, 0, NULL, NULL, 2, 'zbu:t_receive:exportXls', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-23 19:45:59', NULL, NULL, 0, 0, '1', 0);

-- 导入excel
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176916875923707', '176916875923701', '导入excel_领取表', NULL, NULL, 0, NULL, NULL, 2, 'zbu:t_receive:importExcel', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-23 19:45:59', NULL, NULL, 0, 0, '1', 0);

-- 角色授权（以 admin 角色为例，role_id 可替换）
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176916875923708', 'f6817f48af4fb3af11b9e8bf182f618b', '176916875923701', NULL, '2026-01-23 19:45:59', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176916875923709', 'f6817f48af4fb3af11b9e8bf182f618b', '176916875923702', NULL, '2026-01-23 19:45:59', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176916875923710', 'f6817f48af4fb3af11b9e8bf182f618b', '176916875923703', NULL, '2026-01-23 19:45:59', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176916875923711', 'f6817f48af4fb3af11b9e8bf182f618b', '176916875923704', NULL, '2026-01-23 19:45:59', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176916875923712', 'f6817f48af4fb3af11b9e8bf182f618b', '176916875923705', NULL, '2026-01-23 19:45:59', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176916875923713', 'f6817f48af4fb3af11b9e8bf182f618b', '176916875923706', NULL, '2026-01-23 19:45:59', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176916875923714', 'f6817f48af4fb3af11b9e8bf182f618b', '176916875923707', NULL, '2026-01-23 19:45:59', '127.0.0.1');