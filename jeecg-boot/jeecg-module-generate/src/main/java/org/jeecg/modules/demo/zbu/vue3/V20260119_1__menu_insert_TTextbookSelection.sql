-- 注意：该页面对应的前台目录为views/zbu文件夹下
-- 如果你想更改到其他目录，请修改sql中component字段对应的值


-- 主菜单
INSERT INTO sys_permission(id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, hide_tab, description, status, del_flag, rule_flag, create_by, create_time, update_by, update_time, internal_or_external)
VALUES ('176883557589401', NULL, '教材选用表', '/zbu/tTextbookSelectionList', 'zbu/TTextbookSelectionList', NULL, NULL, 0, NULL, '1', 0.00, 0, NULL, 1, 0, 0, 0, 0, NULL, '1', 0, 0, 'admin', '2026-01-19 23:12:55', NULL, NULL, 0);

-- 新增
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176883557589402', '176883557589401', '添加教材选用表', NULL, NULL, 0, NULL, NULL, 2, 'zbu:t_textbook_selection:add', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-19 23:12:55', NULL, NULL, 0, 0, '1', 0);

-- 编辑
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176883557589403', '176883557589401', '编辑教材选用表', NULL, NULL, 0, NULL, NULL, 2, 'zbu:t_textbook_selection:edit', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-19 23:12:55', NULL, NULL, 0, 0, '1', 0);

-- 删除
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176883557589404', '176883557589401', '删除教材选用表', NULL, NULL, 0, NULL, NULL, 2, 'zbu:t_textbook_selection:delete', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-19 23:12:55', NULL, NULL, 0, 0, '1', 0);

-- 批量删除
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176883557589405', '176883557589401', '批量删除教材选用表', NULL, NULL, 0, NULL, NULL, 2, 'zbu:t_textbook_selection:deleteBatch', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-19 23:12:55', NULL, NULL, 0, 0, '1', 0);

-- 导出excel
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176883557589406', '176883557589401', '导出excel_教材选用表', NULL, NULL, 0, NULL, NULL, 2, 'zbu:t_textbook_selection:exportXls', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-19 23:12:55', NULL, NULL, 0, 0, '1', 0);

-- 导入excel
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176883557589407', '176883557589401', '导入excel_教材选用表', NULL, NULL, 0, NULL, NULL, 2, 'zbu:t_textbook_selection:importExcel', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-19 23:12:55', NULL, NULL, 0, 0, '1', 0);

-- 角色授权（以 admin 角色为例，role_id 可替换）
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176883557589408', 'f6817f48af4fb3af11b9e8bf182f618b', '176883557589401', NULL, '2026-01-19 23:12:55', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176883557589409', 'f6817f48af4fb3af11b9e8bf182f618b', '176883557589402', NULL, '2026-01-19 23:12:55', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176883557589410', 'f6817f48af4fb3af11b9e8bf182f618b', '176883557589403', NULL, '2026-01-19 23:12:55', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176883557589411', 'f6817f48af4fb3af11b9e8bf182f618b', '176883557589404', NULL, '2026-01-19 23:12:55', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176883557589412', 'f6817f48af4fb3af11b9e8bf182f618b', '176883557589405', NULL, '2026-01-19 23:12:55', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176883557589413', 'f6817f48af4fb3af11b9e8bf182f618b', '176883557589406', NULL, '2026-01-19 23:12:55', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176883557589414', 'f6817f48af4fb3af11b9e8bf182f618b', '176883557589407', NULL, '2026-01-19 23:12:55', '127.0.0.1');