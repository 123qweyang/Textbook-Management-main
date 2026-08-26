# 后端 Controller 参考

> 所有 Controller 位于 `jeecg-boot/jeecg-module-generate/src/main/java/org/jeecg/modules/demo/zbu/controller/`
> 均继承 `JeecgController<Entity, Service>`

## Controller 一览

| Controller | 路由前缀 | 对应表 | 行数 |
|------------|----------|--------|------|
| `TCollegeController` | `/zbu/tCollege` | t_college | ~294 |
| `TMajorController` | `/zbu/tMajor` | t_major | ~317 |
| `TClassController` | `/zbu/tClass` | t_class | ~626 |
| `TCounselorController` | `/zbu/tCounselor` | t_counselor | ~673 |
| `TStudentController` | `/zbu/tStudent` | t_student | ~1512 |
| `TTextbookController` | `/zbu/tTextbook` | t_textbook | ~662 |
| `TTextbookSelectionController` | `/zbu/tTextbookSelection` | t_textbook_selection | ~1457 |
| `TSubscriptionController` | `/zbu/tSubscription` | t_subscription | ~1179 |
| `TReceiveController` | `/zbu/tReceive` | t_receive | ~1067 |
| `StudentBillController` | `/zbu/studentBill` | student_bill | ~1134 |
| `StudentAllBillSummaryController` | `/zbu/studentAllBillSummary` | student_all_bill_summary | ~496 |

## 标准 CRUD 接口（所有 Controller 共有）

| HTTP 方法 | 路径 | 说明 | 权限注解 |
|-----------|------|------|----------|
| GET | `/list` | 分页列表查询（使用 QueryGenerator） | 无（公开） |
| POST | `/add` | 新增 | `@RequiresPermissions("zbu:表名:add")` |
| PUT/POST | `/edit` | 编辑 | `@RequiresPermissions("zbu:表名:edit")` |
| DELETE | `/delete` | 按 ID 删除 | `@RequiresPermissions("zbu:表名:delete")` |
| DELETE | `/deleteBatch` | 批量删除 | `@RequiresPermissions("zbu:表名:deleteBatch")` |
| GET | `/queryById` | 按 ID 查询 | 无 |
| GET | `/exportXls` | 导出 Excel | `@RequiresPermissions("zbu:表名:exportXls")` |
| POST | `/importExcel` | 导入 Excel | `@RequiresPermissions("zbu:表名:importExcel")` |

## 各 Controller 特有接口

### TStudentController（最复杂）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/getCurrentStudent` | 当前登录学生信息 |
| GET | `/queryByStudentId` | 按学号查询 |
| GET | `/queryByNo` | 按学号查询（别名） |
| GET | `/getMajorList` | 获取专业列表 |
| GET | `/getClassListByMajor` | 按专业获取班级 |
| GET | `/getClassList` | 获取班级列表 |
| POST | `/updateByExcel` | 通过 Excel 批量更新 |

### TCounselorController

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/queryByCounselorId` | 按工号查询 |
| POST | `/updateByExcel` | 通过 Excel 批量更新 |

### TTextbookController

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/getAllIds` | 获取所有教材 ID |
| POST | `/updateByExcel` | 通过 Excel 批量更新 |
| POST | `/editBatch` | 批量编辑 |

### TTextbookSelectionController

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/deleteBatch` | POST 方式批量删除（支持大数据量） |

### TSubscriptionController

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/getMySubscription` | 学生端查看自己的征订 |
| POST | `/batchUpdateSubscribeStatus` | 批量更新征订状态 |
| POST | `/agreeSubscription` | 同意征订 |

### TReceiveController

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/getMyReceive` | 学生端查看自己的领取记录 |
| GET | `/getStudentByNo` | 按学号查学生 |
| GET | `/getStudentById` | 按 ID 查学生 |
| POST | `/batchUpdateReceiveStatus` | 批量更新领取状态 |

### StudentBillController

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/syncFromSubscription` | 从征订表同步到个人账单 |
| GET | `/getCurrentSchoolYear` | 获取当前学年 |
| GET | `/summary` | 账单汇总查询 |
| GET/POST | `/exportSummary` | 导出汇总账单 |
| POST | `/deleteSummaryBatch` | 批量删除汇总记录 |
| POST | `/batchUpdateReceiveStatus` | 批量更新领取状态 |

### StudentAllBillSummaryController

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/summarySubscriptionData` | 手动触发汇总征订数据 |
| — | `autoSummarySubscriptionData()` | `@Scheduled` 定时任务自动汇总 |

### TClassController

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/updateByExcel` | 通过 Excel 批量更新班级辅导员 |

## 开发模式要点

1. **列表查询**：`queryPageList()` + `QueryGenerator.initQueryWrapper()`，通过 `code` 参数支持 `WHERE id IN (...)` 精确查询
2. **导入校验**：自定义逐行校验逻辑，失败时调用 `ImportErrorExportUtil.buildErrorResult()` 生成错误 Excel
3. **导出**：`exportXls()` 支持自定义查询逻辑，外键字段需配置 `@Excel(dictTable=...)` 翻译
4. **CSV 支持**：部分 Controller 支持 CSV 格式导入（如 TStudent、TTextbook）
