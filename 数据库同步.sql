-- =====================================================
-- 教材管理云平台 - 数据库同步脚本
-- 日期: 2026-06-17
-- =====================================================

-- 1. 学期字典标签统一（一/二 → 第一学期/第二学期）
UPDATE sys_dict_item SET item_text = '第一学期' WHERE dict_id = (SELECT id FROM sys_dict WHERE dict_code = 'semester') AND item_value = '1';
UPDATE sys_dict_item SET item_text = '第二学期' WHERE dict_id = (SELECT id FROM sys_dict WHERE dict_code = 'semester') AND item_value = '2';

-- 2. 清洗中文学期脏数据（统一为字典码 1/2）
UPDATE t_subscription SET subscription_semester = '1' WHERE subscription_semester IN ('第一学期', '一');
UPDATE t_subscription SET subscription_semester = '2' WHERE subscription_semester IN ('第二学期', '二');
UPDATE t_textbook_selection SET semester = '1' WHERE semester IN ('第一学期', '一');
UPDATE t_textbook_selection SET semester = '2' WHERE semester IN ('第二学期', '二');

-- 3. 清洗 selection_status 脏数据
UPDATE t_textbook_selection SET selection_status = '1' WHERE selection_status IN ('生效', '启用');
UPDATE t_textbook_selection SET selection_status = '0' WHERE selection_status IN ('失效', '停用', '未生效');

-- 4. 清除2025-2026第二学期脏数据（有账单无征订）
DELETE FROM student_bill WHERE subscription_year = '2025-2026' AND subscription_semester = '2';
DELETE FROM student_all_bill_summary WHERE subscription_year = '2025-2026' AND subscription_semester = '2';

-- 5. 新增教材选用生效状态字典（独立于 use_state）
INSERT INTO sys_dict (id, dict_name, dict_code, description, del_flag, create_by, create_time, update_by, update_time, type)
SELECT REPLACE(UUID(), '-', ''), '生效状态', 'selection_status', '教材选用生效状态', 0, 'admin', NOW(), 'admin', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM sys_dict WHERE dict_code = 'selection_status');

SET @dict_id = (SELECT id FROM sys_dict WHERE dict_code = 'selection_status');

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time, update_by, update_time)
SELECT REPLACE(UUID(), '-', ''), @dict_id, '生效', '1', '教材选用生效', 1, 1, 'admin', NOW(), 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_item WHERE dict_id = @dict_id AND item_value = '1');

INSERT INTO sys_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, create_by, create_time, update_by, update_time)
SELECT REPLACE(UUID(), '-', ''), @dict_id, '失效', '0', '教材选用失效', 2, 1, 'admin', NOW(), 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_item WHERE dict_id = @dict_id AND item_value = '0');
