-- ============================================
-- 增量同步 SQL：从原始 jeecg-boot.sql → 当前本地完整状态
-- 使用方式：宝塔导入原始 jeecg-boot.sql 后，执行本文件
-- 大小：约 6KB，远低于 50MB 限制
-- ============================================

-- ============================================
-- 第一部分：表结构调整
-- ============================================

-- 1.1 student_bill 新增 3 个字段（幂等：列存在则跳过）
-- MySQL 不支持 ADD COLUMN IF NOT EXISTS，用存储过程绕开

DROP PROCEDURE IF EXISTS add_column_if_missing;
DELIMITER $$
CREATE PROCEDURE add_column_if_missing(
    IN tbl VARCHAR(64), IN col VARCHAR(64), 
    IN col_def VARCHAR(256), IN after_col VARCHAR(64)
)
BEGIN
    DECLARE col_count INT;
    SELECT COUNT(*) INTO col_count 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tbl AND COLUMN_NAME = col;
    IF col_count = 0 THEN
        SET @stmt = CONCAT('ALTER TABLE `', tbl, '` ADD COLUMN `', col, '` ', col_def, ' AFTER `', after_col, '`');
        PREPARE stmt FROM @stmt;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

CALL add_column_if_missing('student_bill', 'class_name', "VARCHAR(50) NULL DEFAULT '' COMMENT '班级名称'", 'major_name');
CALL add_column_if_missing('student_bill', 'college_name', "VARCHAR(50) NULL DEFAULT '' COMMENT '学院名称'", 'class_name');
CALL add_column_if_missing('student_bill', 'isbn', "VARCHAR(50) NULL DEFAULT NULL COMMENT 'ISBN'", 'textbook_name');

DROP PROCEDURE IF EXISTS add_column_if_missing;


-- ============================================
-- 第二部分：数据清洗与字典修正
-- ============================================

-- 2.1 学期字典标签统一（一/二 → 第一学期/第二学期）
UPDATE sys_dict_item SET item_text = '第一学期' 
WHERE dict_id = (SELECT id FROM sys_dict WHERE dict_code = 'semester') AND item_value = '1';
UPDATE sys_dict_item SET item_text = '第二学期' 
WHERE dict_id = (SELECT id FROM sys_dict WHERE dict_code = 'semester') AND item_value = '2';

-- 2.2 清洗中文学期脏数据（统一为字典码 1/2）
UPDATE t_subscription SET subscription_semester = '1' WHERE subscription_semester IN ('第一学期', '一');
UPDATE t_subscription SET subscription_semester = '2' WHERE subscription_semester IN ('第二学期', '二');
UPDATE t_textbook_selection SET semester = '1' WHERE semester IN ('第一学期', '一');
UPDATE t_textbook_selection SET semester = '2' WHERE semester IN ('第二学期', '二');

-- 2.3 清洗 selection_status 脏数据
UPDATE t_textbook_selection SET selection_status = '1' WHERE selection_status IN ('生效', '启用');
UPDATE t_textbook_selection SET selection_status = '0' WHERE selection_status IN ('失效', '停用', '未生效');

-- 2.4 清除2025-2026第二学期脏数据（有账单无征订）
DELETE FROM student_bill WHERE subscription_year = '2025-2026' AND subscription_semester = '2';
DELETE FROM student_all_bill_summary WHERE subscription_year = '2025-2026' AND subscription_semester = '2';

-- 2.5 新增教材选用生效状态字典
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


-- ============================================
-- 第三部分：创建 7 个应用视图
-- ============================================

-- 3.1 班级关联辅导员
CREATE OR REPLACE VIEW `v_class_with_counselor` AS 
  SELECT `c`.`id`, `c`.`class_name`, `c`.`class_code`, 
         `c`.`major_id`, `c`.`counselor_id`, 
         `co`.`counselor_id` AS `counselor_no`, 
         `c`.`create_time`, `c`.`update_time` 
  FROM `t_class` `c` 
  LEFT JOIN `t_counselor` `co` ON `c`.`counselor_id` = `co`.`id`;

-- 3.2 领取记录（关联学生、教材、专业、班级）
CREATE OR REPLACE VIEW `v_receive_with_details` AS 
  SELECT `r`.`id`, `r`.`receive_operator` AS `receiveOperator`, 
         `st`.`student_id` AS `studentNo`, `st`.`student_name` AS `studentName`, 
         `r`.`subscription_id`, `sub`.`textbook_id`, 
         `tb`.`textbook_name`, `tb`.`isbn`, 
         `sub`.`major_id`, `m`.`major_name` AS `majorName`, 
         `m`.`college_id`, `c`.`college_name` AS `collegeName`, 
         `sub`.`subscription_year` AS `subscriptionYear`, 
         `sub`.`subscription_semester` AS `subscriptionSemester`, 
         `cl`.`class_name` AS `className`, 
         `r`.`receive_status` AS `receiveStatus`, 
         `r`.`receive_time` AS `receiveTime`, 
         `r`.`receive_remark` AS `receiveRemark`, 
         `r`.`create_time`, `r`.`update_time` 
  FROM `t_receive` `r` 
  LEFT JOIN `t_subscription` `sub` ON `r`.`subscription_id` = `sub`.`id`
  LEFT JOIN `t_major` `m` ON `sub`.`major_id` = `m`.`id`
  LEFT JOIN `t_college` `c` ON `m`.`college_id` = `c`.`id`
  LEFT JOIN `t_student` `st` ON `r`.`receive_operator` = `st`.`id`
  LEFT JOIN `t_textbook` `tb` ON `sub`.`textbook_id` = `tb`.`id`
  LEFT JOIN `t_class` `cl` ON `st`.`class_id` = `cl`.`id`;

-- 3.3 学生账单汇总
CREATE OR REPLACE VIEW `v_student_bill_summary` AS 
  SELECT `b`.`student_id` AS `studentId`, 
         `s`.`student_id` AS `studentNo`, 
         `s`.`student_name` AS `studentName`, 
         `b`.`class_name` AS `className`, 
         `b`.`major_name` AS `majorName`, 
         `b`.`college_name` AS `collegeName`, 
         `b`.`subscription_year` AS `schoolYear`, 
         `b`.`subscription_semester` AS `semester`, 
         SUM(`b`.`discount_price`) AS `totalDiscountPrice` 
  FROM `student_bill` `b` 
  LEFT JOIN `t_student` `s` ON `b`.`student_id` = `s`.`student_id`
  GROUP BY `b`.`student_id`, `s`.`student_id`, `s`.`student_name`, 
           `b`.`class_name`, `b`.`major_name`, `b`.`college_name`, 
           `b`.`subscription_year`, `b`.`subscription_semester`;

-- 3.4 学生账单关联教材ISBN
CREATE OR REPLACE VIEW `v_student_bill_with_isbn` AS 
  SELECT `b`.`id`, `b`.`student_id`, `b`.`major_name`, 
         `b`.`subscription_year`, `b`.`subscription_semester`, 
         `b`.`textbook_name`, `b`.`price`, `b`.`discount_price`, 
         `b`.`subscribe_status`, `b`.`receive_status`, `b`.`remark`, 
         `b`.`create_time`, `b`.`update_time`, 
         `t`.`isbn` 
  FROM `student_bill` `b` 
  LEFT JOIN `t_textbook` `t` ON `b`.`textbook_name` = `t`.`textbook_name`;

-- 3.5 学生关联辅导员
CREATE OR REPLACE VIEW `v_student_with_counselor` AS 
  SELECT `s`.`id` AS `student_id`, `s`.`student_id` AS `student_no`, 
         `s`.`student_name`, `s`.`major_id`, `s`.`class_id`, 
         `s`.`status`, `s`.`admission_year`, 
         `s`.`user_id` AS `student_user_id`, 
         `c`.`id` AS `class_table_id`, `c`.`class_name`, `c`.`class_code`, 
         `c`.`major_id` AS `class_major_id`, 
         `c`.`counselor_id` AS `class_counselor_id`, 
         `co`.`id` AS `counselor_table_id`, 
         `co`.`counselor_name`, `co`.`user_id` AS `counselor_user_id` 
  FROM `t_student` `s` 
  LEFT JOIN `t_class` `c` ON `s`.`class_id` = `c`.`id`
  LEFT JOIN `t_counselor` `co` ON `c`.`counselor_id` = `co`.`id`;

-- 3.6 征订记录（关联学生、教材、专业、学院）
CREATE OR REPLACE VIEW `v_subscription_with_details` AS 
  SELECT `s`.`id`, `s`.`student_id`, 
         `st`.`student_id` AS `studentNo`, `st`.`student_name` AS `studentName`, 
         `s`.`textbook_id`, `tb`.`textbook_name`, `tb`.`isbn`, 
         `s`.`selection_id`, `s`.`major_id`, 
         `m`.`major_name` AS `majorName`, 
         `m`.`college_id`, `c`.`college_name` AS `collegeName`, 
         `s`.`subscription_year` AS `subscriptionYear`, 
         `s`.`subscription_semester` AS `subscriptionSemester`, 
         `s`.`subscribe_status` AS `subscribeStatus`, 
         `s`.`remark`, `s`.`subscribe_time` AS `subscribeTime`, 
         `s`.`create_time` AS `createTime`, `s`.`update_time` AS `updateTime` 
  FROM `t_subscription` `s` 
  LEFT JOIN `t_student` `st` ON `s`.`student_id` = `st`.`id`
  LEFT JOIN `t_textbook` `tb` ON `s`.`textbook_id` = `tb`.`id`
  LEFT JOIN `t_major` `m` ON `s`.`major_id` = `m`.`id`
  LEFT JOIN `t_college` `c` ON `m`.`college_id` = `c`.`id`;

-- 3.7 教材选用关联ISBN
CREATE OR REPLACE VIEW `v_textbook_selection_with_isbn` AS 
  SELECT `s`.`id`, `s`.`major_id` AS `majorId`, 
         `m`.`major_name` AS `majorName`, 
         `s`.`class_id` AS `classId`, `c`.`class_name` AS `className`, 
         `s`.`textbook_id` AS `textbookId`, 
         `t`.`textbook_name` AS `textbookName`, `t`.`isbn`, 
         `s`.`school_year` AS `schoolYear`, `s`.`semester` AS `semester`, 
         `s`.`selection_status` AS `selectionStatus`, 
         `s`.`remark`, `s`.`create_time` AS `createTime`, 
         `s`.`update_time` AS `updateTime` 
  FROM `t_textbook_selection` `s` 
  LEFT JOIN `t_major` `m` ON `s`.`major_id` = `m`.`id`
  LEFT JOIN `t_class` `c` ON `s`.`class_id` = `c`.`id`
  LEFT JOIN `t_textbook` `t` ON `s`.`textbook_id` = `t`.`id`;
