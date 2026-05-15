-- 注意：该页面对应的前台目录为views/zbu文件夹下
-- 如果你想更改到其他目录，请修改sql中component字段对应的值


-- 主菜单
INSERT INTO sys_permission(id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, hide_tab, description, status, del_flag, rule_flag, create_by, create_time, update_by, update_time, internal_or_external)
VALUES ('176883556062401', NULL, '辅导员表', '/zbu/tCounselorList', 'zbu/TCounselorList', NULL, NULL, 0, NULL, '1', 0.00, 0, NULL, 1, 0, 0, 0, 0, NULL, '1', 0, 0, 'admin', '2026-01-19 23:12:40', NULL, NULL, 0);

-- 新增
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176883556062402', '176883556062401', '添加辅导员表', NULL, NULL, 0, NULL, NULL, 2, 'zbu:t_counselor:add', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-19 23:12:40', NULL, NULL, 0, 0, '1', 0);

-- 编辑
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176883556062403', '176883556062401', '编辑辅导员表', NULL, NULL, 0, NULL, NULL, 2, 'zbu:t_counselor:edit', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-19 23:12:40', NULL, NULL, 0, 0, '1', 0);

-- 删除
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176883556062404', '176883556062401', '删除辅导员表', NULL, NULL, 0, NULL, NULL, 2, 'zbu:t_counselor:delete', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-19 23:12:40', NULL, NULL, 0, 0, '1', 0);

-- 批量删除
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176883556062405', '176883556062401', '批量删除辅导员表', NULL, NULL, 0, NULL, NULL, 2, 'zbu:t_counselor:deleteBatch', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-19 23:12:40', NULL, NULL, 0, 0, '1', 0);

-- 导出excel
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176883556062406', '176883556062401', '导出excel_辅导员表', NULL, NULL, 0, NULL, NULL, 2, 'zbu:t_counselor:exportXls', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-19 23:12:40', NULL, NULL, 0, 0, '1', 0);

-- 导入excel
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176883556062407', '176883556062401', '导入excel_辅导员表', NULL, NULL, 0, NULL, NULL, 2, 'zbu:t_counselor:importExcel', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-19 23:12:40', NULL, NULL, 0, 0, '1', 0);

-- 角色授权（以 admin 角色为例，role_id 可替换）
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176883556062508', 'f6817f48af4fb3af11b9e8bf182f618b', '176883556062401', NULL, '2026-01-19 23:12:40', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176883556062509', 'f6817f48af4fb3af11b9e8bf182f618b', '176883556062402', NULL, '2026-01-19 23:12:40', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176883556062510', 'f6817f48af4fb3af11b9e8bf182f618b', '176883556062403', NULL, '2026-01-19 23:12:40', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176883556062511', 'f6817f48af4fb3af11b9e8bf182f618b', '176883556062404', NULL, '2026-01-19 23:12:40', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176883556062512', 'f6817f48af4fb3af11b9e8bf182f618b', '176883556062405', NULL, '2026-01-19 23:12:40', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176883556062513', 'f6817f48af4fb3af11b9e8bf182f618b', '176883556062406', NULL, '2026-01-19 23:12:40', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176883556062514', 'f6817f48af4fb3af11b9e8bf182f618b', '176883556062407', NULL, '2026-01-19 23:12:40', '127.0.0.1');