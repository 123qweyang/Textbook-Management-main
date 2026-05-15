-- 注意：该页面对应的前台目录为views/zbu文件夹下
-- 如果你想更改到其他目录，请修改sql中component字段对应的值


-- 主菜单
INSERT INTO sys_permission(id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, hide_tab, description, status, del_flag, rule_flag, create_by, create_time, update_by, update_time, internal_or_external)
VALUES ('176960718588401', NULL, '总账单', '/zbu/studentAllBillSummaryList', 'zbu/StudentAllBillSummaryList', NULL, NULL, 0, NULL, '1', 0.00, 0, NULL, 1, 0, 0, 0, 0, NULL, '1', 0, 0, 'admin', '2026-01-28 21:33:05', NULL, NULL, 0);

-- 新增
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176960718588402', '176960718588401', '添加总账单', NULL, NULL, 0, NULL, NULL, 2, 'zbu:student_all_bill_summary:add', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-28 21:33:05', NULL, NULL, 0, 0, '1', 0);

-- 编辑
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176960718588403', '176960718588401', '编辑总账单', NULL, NULL, 0, NULL, NULL, 2, 'zbu:student_all_bill_summary:edit', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-28 21:33:05', NULL, NULL, 0, 0, '1', 0);

-- 删除
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176960718588404', '176960718588401', '删除总账单', NULL, NULL, 0, NULL, NULL, 2, 'zbu:student_all_bill_summary:delete', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-28 21:33:05', NULL, NULL, 0, 0, '1', 0);

-- 批量删除
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176960718588405', '176960718588401', '批量删除总账单', NULL, NULL, 0, NULL, NULL, 2, 'zbu:student_all_bill_summary:deleteBatch', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-28 21:33:05', NULL, NULL, 0, 0, '1', 0);

-- 导出excel
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176960718588406', '176960718588401', '导出excel_总账单', NULL, NULL, 0, NULL, NULL, 2, 'zbu:student_all_bill_summary:exportXls', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-28 21:33:05', NULL, NULL, 0, 0, '1', 0);

-- 导入excel
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176960718588407', '176960718588401', '导入excel_总账单', NULL, NULL, 0, NULL, NULL, 2, 'zbu:student_all_bill_summary:importExcel', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-28 21:33:05', NULL, NULL, 0, 0, '1', 0);

-- 角色授权（以 admin 角色为例，role_id 可替换）
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176960718588408', 'f6817f48af4fb3af11b9e8bf182f618b', '176960718588401', NULL, '2026-01-28 21:33:05', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176960718588409', 'f6817f48af4fb3af11b9e8bf182f618b', '176960718588402', NULL, '2026-01-28 21:33:05', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176960718588410', 'f6817f48af4fb3af11b9e8bf182f618b', '176960718588403', NULL, '2026-01-28 21:33:05', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176960718588411', 'f6817f48af4fb3af11b9e8bf182f618b', '176960718588404', NULL, '2026-01-28 21:33:05', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176960718588412', 'f6817f48af4fb3af11b9e8bf182f618b', '176960718588405', NULL, '2026-01-28 21:33:05', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176960718588413', 'f6817f48af4fb3af11b9e8bf182f618b', '176960718588406', NULL, '2026-01-28 21:33:05', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176960718588414', 'f6817f48af4fb3af11b9e8bf182f618b', '176960718588407', NULL, '2026-01-28 21:33:05', '127.0.0.1');