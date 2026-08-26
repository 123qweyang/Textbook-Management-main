# 已知问题与修复记录

## 1. 辅导员端用户管理页面无数据

**根因**：`SysUserController.getCounselorStudents` 查询链路正确，但前端 `index.vue` 的 `wrappedApi` 通过 `listNoCareTenant({ pageNo: 1, pageSize: 1000 })` 加载全量用户后进行前端过滤。系统 20,306 个用户按 `create_time DESC` 排序，辅导员管辖的 209 个学生因创建时间早，排在 1000 条之后被截断，前端过滤匹配到 0 条。

**修复**：改为按当前页切片学生 ID，通过 `code` 参数（`WHERE id IN (...)`）直接查询：

1. 对 `counselorStudentUserIds` 排序确保跨页一致
2. 按 `pageNo/pageSize` 切片，取出当前页 ID
3. 传给 `listNoCareTenant({ pageNo: 1, code: "id1,id2,..." })` 精确查询
4. 返回时补充正确的 `total`/`current`/`size`

**影响范围**：前端 `jeecgboot-vue3/src/views/system/user/index.vue`（第 190-214 行附近）

**数据链路**：`sys_user → t_counselor → t_class → t_student → user_id`

---

## 2. 征订表权限授权

**问题**：管理员在角色管理页面无法为学生/辅导员角色授权征订表权限——`sys_role_permission` 表中 `student` 和 `counselor` 角色缺少征订表相关记录。老版本存在同样问题。

**修复**：只改数据库 `sys_role_permission` 表，代码未改动。

| 角色 | 新增权限 | 说明 |
|------|----------|------|
| student | 7 个（菜单 + 6 按钮） | 与领取表/账单一致 |
| counselor | 5 个（菜单 + 4 按钮，无删除） | 与领取表一致 |

**权限 ID 参考**：父菜单 `176882849717601`，子按钮 `176882849717602`~`176882849717607`

---

## 开发注意事项

### Entity 注解

- 外键 ID 字段的 `@Excel` 必须配置 `dictTable`/`dicCode`/`dicText`，否则导出显示原始数字 ID
- `@Dict` 只支持单层外键翻译，无法处理二级关联（如通过 `t_subscription` 查 `t_textbook` 名称）
- `@TableField(exist = false)` 标记非数据库虚拟字段（用于搜索、展示）

### 导入错误处理

`ImportErrorExportUtil.buildErrorResult(errorMessages, successMsg, uploadPath, tableName)`：
- 空错误列表 → 返回 `code=200` + 成功消息
- 有错误 → 生成 Excel 到 `{uploadPath}/import_error/{日期}/{表名}_导入错误_{日期}_{时间}.xlsx`，返回 `code=201` 触发前端弹窗 + 下载

### 密码规则

- 加密方式：`PasswordUtil.encrypt(用户名, 明文密码, 随机8位salt)`
- 学生默认密码：学号 + `Zbu1`
- 辅导员默认密码：工号 + `Zbu1`

### 跨系统编码问题

- WSL 终端 UTF-8，Windows 命令行 GBK
- 读取 Windows 命令输出中文时需 `| iconv -f GBK -t UTF-8` 转码
- `jeecg-boot.sql` 中的中文数据直接导入 MySQL 正常
