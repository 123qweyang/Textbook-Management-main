# 数据库

> 数据库：MySQL 8.0.46，库名 `jeecg-boot`，以实际数据库结构为准（非根目录 `jeecg-boot.sql`）
> 
> **注意**：`jeecg-boot.sql` 是最初版本，开发过程中数据库已多次修改，以下内容来自当前实际数据库。

## 11 张自定义业务表（完整字段定义）

### t_college（学院）

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|----|-----|------|
| id | varchar(36) | NO | PRI | 主键（雪花ID） |
| college_name | varchar(50) | NO | | 学院名称 |
| college_code | varchar(20) | NO | | 学院编码 |
| create_time | datetime | YES | | |
| update_time | datetime | YES | | |

### t_major（专业）

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|----|-----|------|
| id | varchar(36) | NO | PRI | 主键 |
| major_name | varchar(50) | NO | | 专业名称 |
| major_code | varchar(20) | NO | | 专业编码 |
| college_id | varchar(36) | NO | | FK → t_college |
| create_time | datetime | YES | | |
| update_time | datetime | YES | | |

### t_class（班级）

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|----|-----|------|
| id | varchar(36) | NO | PRI | 主键 |
| class_name | varchar(50) | NO | | 班级名称 |
| class_code | varchar(20) | NO | | 班级编码 |
| major_id | varchar(36) | NO | | FK → t_major |
| counselor_id | varchar(32) | NO | | FK → t_counselor |
| create_time | datetime | YES | | |
| update_time | datetime | YES | | |

### t_counselor（辅导员）

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|----|-----|------|
| id | varchar(36) | NO | PRI | 主键 |
| counselor_name | varchar(30) | NO | | 姓名 |
| college_id | varchar(36) | NO | | FK → t_college |
| contact | varchar(20) | NO | | 联系方式 |
| create_time | datetime | YES | | |
| update_time | datetime | YES | | |
| status | varchar(32) | NO | | 在职状态（字典 employed） |
| counselor_id | varchar(36) | NO | | 工号 |
| user_id | varchar(36) | YES | | FK → sys_user（登录账号） |

### t_student（学生）

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|----|-----|------|
| id | varchar(36) | NO | PRI | 主键 |
| student_name | varchar(100) | NO | | 姓名 |
| major_id | varchar(36) | NO | | FK → t_major |
| class_id | varchar(36) | NO | MUL | FK → t_class，有索引 |
| status | varchar(32) | YES | | 在校状态（字典 use_state） |
| admission_year | varchar(32) | YES | | 入学年份 |
| create_time | datetime | YES | | |
| update_time | datetime | YES | | |
| student_id | varchar(36) | NO | | 学号 |
| user_id | varchar(36) | NO | | FK → sys_user |

### t_textbook（教材）

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|----|-----|------|
| id | varchar(36) | NO | PRI | 主键 |
| section_code | varchar(30) | YES | | 章节号 |
| business_code | varchar(30) | YES | | 业务编号 |
| isbn | varchar(20) | NO | | ISBN |
| textbook_name | varchar(100) | NO | | 教材名称 |
| author | varchar(100) | NO | | 作者 |
| publisher | varchar(50) | YES | | 出版社 |
| publish_date | varchar(32) | NO | | 出版日期 |
| price | decimal(10,2) | NO | | 定价 |
| discount | decimal(3,2) | YES | | 折扣率 |
| enable_year | varchar(20) | NO | MUL | 适用学年，有索引 |
| enable_semester | varchar(20) | NO | | 适用学期（字典 semester） |
| status | varchar(20) | NO | | 使用状态（字典 use_state） |
| create_time | datetime | YES | | |
| update_time | datetime | YES | | |

### t_textbook_selection（教材选用）

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|----|-----|------|
| id | varchar(36) | NO | PRI | 主键 |
| major_id | varchar(36) | NO | | FK → t_major |
| class_id | varchar(36) | NO | | FK → t_class |
| textbook_id | varchar(36) | YES | | FK → t_textbook |
| school_year | varchar(20) | NO | | 学年 |
| semester | varchar(20) | NO | | 学期（字典 semester） |
| selection_status | varchar(20) | YES | MUL | 选用状态（字典 selection_status），有索引 |
| remark | varchar(200) | YES | | 备注 |
| create_time | datetime | YES | | |
| update_time | datetime | YES | | |

### t_subscription（征订）

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|----|-----|------|
| id | varchar(36) | NO | PRI | 主键 |
| student_id | varchar(36) | NO | MUL | FK → t_student，有索引 |
| textbook_id | varchar(36) | NO | | FK → t_textbook |
| selection_id | varchar(36) | NO | | FK → t_textbook_selection |
| major_id | varchar(36) | NO | MUL | FK → t_major，有索引 |
| subscription_year | varchar(32) | NO | | 征订学年 |
| subscription_semester | varchar(32) | NO | | 征订学期 |
| subscribe_status | varchar(32) | YES | | 征订状态（字典 subscribe_status） |
| subscribe_time | datetime | YES | | 征订时间 |
| deadline | datetime | YES | | 截止日期 |
| remark | varchar(32) | YES | | 备注 |
| create_time | datetime | YES | | |
| update_time | datetime | YES | | |

### t_receive（领取）

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|----|-----|------|
| id | varchar(36) | NO | PRI | 主键 |
| receive_operator | varchar(36) | NO | MUL | FK → t_student（学生ID），有索引 |
| subscription_id | varchar(36) | NO | MUL | FK → t_subscription，有索引 |
| college_name | varchar(255) | YES | | 学院名称（冗余） |
| receive_status | varchar(32) | YES | | 领取状态（字典 receive_status） |
| receive_time | datetime | YES | | 领取时间 |
| receive_remark | varchar(200) | YES | | 领取备注 |
| create_time | datetime | YES | | |
| update_time | datetime | YES | | |

### student_bill（个人账单）

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|----|-----|------|
| id | varchar(36) | NO | PRI | 主键 |
| student_id | varchar(36) | NO | | 学号（t_student.student_id） |
| major_name | varchar(50) | NO | | 专业名称（冗余） |
| class_name | varchar(50) | YES | | 班级名称（冗余，新增字段） |
| college_name | varchar(50) | YES | | 学院名称（冗余，新增字段） |
| subscription_year | varchar(32) | NO | | 征订学年 |
| subscription_semester | varchar(32) | NO | | 征订学期 |
| textbook_name | varchar(100) | NO | | 教材名称 |
| isbn | varchar(50) | YES | | ISBN |
| price | decimal(10,2) | YES | | 定价 |
| discount_price | decimal(10,2) | YES | | 折扣后价格 |
| subscribe_status | varchar(32) | NO | | 征订状态 |
| receive_status | varchar(32) | NO | | 领取状态 |
| remark | varchar(1000) | YES | | 备注 |
| create_time | datetime | YES | | |
| update_time | datetime | YES | | |

> **新增字段**：`class_name` 和 `college_name` 是开发过程中加入的冗余字段，原始 SQL 文件中不包含。

### student_all_bill_summary（总账单汇总）

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|----|-----|------|
| id | varchar(36) | NO | PRI | 主键 |
| college_id | varchar(36) | NO | | FK → t_college |
| college_name | varchar(200) | NO | | 学院名称（冗余） |
| major_id | varchar(36) | NO | | FK → t_major |
| major_name | varchar(200) | NO | | 专业名称（冗余） |
| subscription_year | varchar(32) | NO | | 征订学年 |
| subscription_semester | varchar(32) | NO | | 征订学期 |
| student_count | bigint | YES | | 学生人数 |
| textbook_count | bigint | YES | | 教材数量 |
| original_total | decimal(10,2) | YES | | 原始总价 |
| discount_total | decimal(10,2) | YES | | 折扣总价 |
| create_time | datetime | YES | | |
| update_time | datetime | YES | | |

## 数据库视图（7 个，开发过程中创建）

| 视图名 | 说明 | 关联表 |
|--------|------|--------|
| `v_class_with_counselor` | 班级关联辅导员信息 | t_class LEFT JOIN t_counselor |
| `v_student_with_counselor` | 学生关联班级和辅导员信息 | t_student LEFT JOIN t_class LEFT JOIN t_counselor |
| `v_subscription_with_details` | 征订关联学生/教材/专业/学院详情 | t_subscription LEFT JOIN t_student, t_textbook, t_major, t_college |
| `v_receive_with_details` | 领取关联征订/学生/教材/专业/学院/班级 | t_receive LEFT JOIN t_subscription, t_student, t_textbook, t_major, t_college, t_class |
| `v_textbook_selection_with_isbn` | 教材选用关联专业/班级/教材/ISBN | t_textbook_selection LEFT JOIN t_major, t_class, t_textbook |
| `v_student_bill_with_isbn` | 个人账单关联 t_textbook 获取 ISBN | student_bill LEFT JOIN t_textbook（按 textbook_name 匹配） |
| `v_student_bill_summary` | 账单按学生汇总 | student_bill LEFT JOIN t_student，GROUP BY student/major/college/year/semester |

## 字典表

| 字典 | dicCode | 说明 |
|------|---------|------|
| 学期 | semester | 第一学期/第二学期，另有 `year_terms` 学年学期组合 |
| 在职状态 | employed | 在职/离职等 |
| 使用状态 | use_state | 启用/禁用等 |
| 选用状态 | selection_status | 教材选用状态 |
| 征订状态 | subscribe_status | 征订状态 |
| 领取状态 | receive_status | 领取状态 |

## 数据关系链

```
t_college
  ├── t_major
  │     ├── t_class
  │     │     └── t_student
  │     └── t_textbook_selection (按专业选用教材)
  └── t_counselor (辅导员属于学院)

t_textbook → t_textbook_selection (按专业/班级选用)
                  └── t_subscription (学生征订)
                        ├── t_receive (领取记录)
                        └── student_bill (个人账单)

student_bill → student_all_bill_summary (按学院+专业+学年+学期汇总)
```

## 关键索引

| 表 | 索引字段 | 用途 |
|----|----------|------|
| t_student | class_id | 按班级查询学生 |
| t_textbook | enable_year | 按学年查询教材 |
| t_textbook_selection | selection_status | 按选用状态筛选 |
| t_subscription | student_id | 按学生查询征订 |
| t_subscription | major_id | 按专业查询征订 |
| t_receive | receive_operator | 按领取人查询 |
| t_receive | subscription_id | 按征订记录查询领取 |

## 级联关系

| 操作 | 级联影响 |
|------|----------|
| 新增学生 | 自动在 `sys_user` 创建登录账号 + 分配 `student` 角色 |
| 新增辅导员 | 自动在 `sys_user` 创建登录账号 + 分配 `counselor` 角色 |
| 删除辅导员 | 级联删除 `sys_user` + `sys_user_role` 记录 |
| 新增教材选用 | 自动为该专业下所有学生生成 `t_subscription` 征订记录 |
| 征订同步 | `StudentBillController.syncFromSubscription()` 从征订表同步到 `student_bill` |
| 定时汇总 | `StudentAllBillSummaryController.autoSummarySubscriptionData()` 定时汇总到 `student_all_bill_summary` |

## 密码加密

`PasswordUtil.encrypt(用户名, 明文密码, 随机8位salt)`
- 学生默认密码：学号 + `Zbu1`
- 辅导员默认密码：工号 + `Zbu1`

## sys_role_permission 已配置状态

| 角色 | 征订表权限数 | 包含 |
|------|-------------|------|
| student | 7 个 | 菜单 + add/edit/delete/deleteBatch/export/import |
| counselor | 5 个 | 菜单 + add/edit/export/import（无删除权限） |

权限 ID：父菜单 `176882849717601`，子按钮 `176882849717602`~`176882849717607`。
