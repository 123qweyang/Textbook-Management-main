-- 注意：该页面对应的前台目录为views/zbu文件夹下
-- 如果你想更改到其他目录，请修改sql中component字段对应的值


-- 主菜单
INSERT INTO sys_permission(id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, hide_tab, description, status, del_flag, rule_flag, create_by, create_time, update_by, update_time, internal_or_external)
VALUES ('176932426949601', NULL, '个人账单', '/zbu/studentBillList', 'zbu/StudentBillList', NULL, NULL, 0, NULL, '1', 0.00, 0, NULL, 1, 0, 0, 0, 0, NULL, '1', 0, 0, 'admin', '2026-01-25 14:57:49', NULL, NULL, 0);

-- 新增
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176932426949602', '176932426949601', '添加个人账单', NULL, NULL, 0, NULL, NULL, 2, 'zbu:student_bill:add', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-25 14:57:49', NULL, NULL, 0, 0, '1', 0);

-- 编辑
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176932426949603', '176932426949601', '编辑个人账单', NULL, NULL, 0, NULL, NULL, 2, 'zbu:student_bill:edit', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-25 14:57:49', NULL, NULL, 0, 0, '1', 0);

-- 删除
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176932426949604', '176932426949601', '删除个人账单', NULL, NULL, 0, NULL, NULL, 2, 'zbu:student_bill:delete', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-25 14:57:49', NULL, NULL, 0, 0, '1', 0);

-- 批量删除
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176932426949605', '176932426949601', '批量删除个人账单', NULL, NULL, 0, NULL, NULL, 2, 'zbu:student_bill:deleteBatch', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-25 14:57:49', NULL, NULL, 0, 0, '1', 0);

-- 导出excel
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176932426949606', '176932426949601', '导出excel_个人账单', NULL, NULL, 0, NULL, NULL, 2, 'zbu:student_bill:exportXls', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-25 14:57:49', NULL, NULL, 0, 0, '1', 0);

-- 导入excel
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176932426949607', '176932426949601', '导入excel_个人账单', NULL, NULL, 0, NULL, NULL, 2, 'zbu:student_bill:importExcel', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2026-01-25 14:57:49', NULL, NULL, 0, 0, '1', 0);

-- 角色授权（以 admin 角色为例，role_id 可替换）
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176932426949608', 'f6817f48af4fb3af11b9e8bf182f618b', '176932426949601', NULL, '2026-01-25 14:57:49', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176932426949609', 'f6817f48af4fb3af11b9e8bf182f618b', '176932426949602', NULL, '2026-01-25 14:57:49', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176932426949610', 'f6817f48af4fb3af11b9e8bf182f618b', '176932426949603', NULL, '2026-01-25 14:57:49', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176932426949611', 'f6817f48af4fb3af11b9e8bf182f618b', '176932426949604', NULL, '2026-01-25 14:57:49', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176932426949612', 'f6817f48af4fb3af11b9e8bf182f618b', '176932426949605', NULL, '2026-01-25 14:57:49', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176932426949613', 'f6817f48af4fb3af11b9e8bf182f618b', '176932426949606', NULL, '2026-01-25 14:57:49', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176932426949614', 'f6817f48af4fb3af11b9e8bf182f618b', '176932426949607', NULL, '2026-01-25 14:57:49', '127.0.0.1');