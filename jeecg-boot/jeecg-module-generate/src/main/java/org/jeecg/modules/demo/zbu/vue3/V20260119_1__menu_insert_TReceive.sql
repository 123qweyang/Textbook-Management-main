-- 注意：该页面对应的前台目录为views/zbu文件夹下
-- 如果你想更改到其他目录，请修改sql中component字段对应的值


-- 主菜单
INSERT INTO sys_permission(id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, hide_tab, description, status, del_flag, rule_flag, create_by, create_time, update_by, update_time, internal_or_external)
VALUES ('176883559627001', NULL, '领取表', '/zbu/tReceiveList', 'zbu/TReceiveList', NULL, NULL, 0, NULL, '1', 0.00, 0, NULL, 1, 0, 0, 0, 0, NULL, '1', 0, 0, 'admin', '2026-01-19 23:13:16', NULL, NULL, 0);

-- 新增
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176883559627002', '176883559627001', '添加领取表', NULL, NULL, 0, NULL, NULL, 2, 'zbu:t_receive:add', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-19 23:13:16', NULL, NULL, 0, 0, '1', 0);

-- 编辑
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176883559627003', '176883559627001', '编辑领取表', NULL, NULL, 0, NULL, NULL, 2, 'zbu:t_receive:edit', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-19 23:13:16', NULL, NULL, 0, 0, '1', 0);

-- 删除
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176883559627004', '176883559627001', '删除领取表', NULL, NULL, 0, NULL, NULL, 2, 'zbu:t_receive:delete', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-19 23:13:16', NULL, NULL, 0, 0, '1', 0);

-- 批量删除
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176883559627005', '176883559627001', '批量删除领取表', NULL, NULL, 0, NULL, NULL, 2, 'zbu:t_receive:deleteBatch', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-19 23:13:16', NULL, NULL, 0, 0, '1', 0);

-- 导出excel
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176883559627006', '176883559627001', '导出excel_领取表', NULL, NULL, 0, NULL, NULL, 2, 'zbu:t_receive:exportXls', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-19 23:13:16', NULL, NULL, 0, 0, '1', 0);

-- 导入excel
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176883559627007', '176883559627001', '导入excel_领取表', NULL, NULL, 0, NULL, NULL, 2, 'zbu:t_receive:importExcel', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-19 23:13:16', NULL, NULL, 0, 0, '1', 0);

-- 角色授权（以 admin 角色为例，role_id 可替换）
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176883559627008', 'f6817f48af4fb3af11b9e8bf182f618b', '176883559627001', NULL, '2026-01-19 23:13:16', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176883559627009', 'f6817f48af4fb3af11b9e8bf182f618b', '176883559627002', NULL, '2026-01-19 23:13:16', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176883559627010', 'f6817f48af4fb3af11b9e8bf182f618b', '176883559627003', NULL, '2026-01-19 23:13:16', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176883559627011', 'f6817f48af4fb3af11b9e8bf182f618b', '176883559627004', NULL, '2026-01-19 23:13:16', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176883559627012', 'f6817f48af4fb3af11b9e8bf182f618b', '176883559627005', NULL, '2026-01-19 23:13:16', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176883559627013', 'f6817f48af4fb3af11b9e8bf182f618b', '176883559627006', NULL, '2026-01-19 23:13:16', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176883559627014', 'f6817f48af4fb3af11b9e8bf182f618b', '176883559627007', NULL, '2026-01-19 23:13:16', '127.0.0.1');