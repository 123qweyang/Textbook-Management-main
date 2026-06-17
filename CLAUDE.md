# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

教材管理云平台，基于 JeecgBoot 3.9.0（Spring Boot 3 + MyBatis-Plus + Vue 3 + Ant Design Vue）。
本地开发环境：JDK17，MySQL 8.0.46，Maven 3.9.12，Redis 无密码。

## 常用命令

### 后端

```bash
# 完整编译
cd jeecg-boot && mvn compile -DskipTests

# 只编译教材管理模块
cd jeecg-boot && mvn compile -pl jeecg-module-generate -am -DskipTests

# 启动（主类在 jeecg-module-system/jeecg-system-start）
```

### 前端

```bash
cd jeecgboot-vue3

# 开发模式
pnpm dev          # 端口 3100，API 代理到 localhost:8080

# 生产构建
pnpm build
```

## 架构

```
Textbook-Management-main/
├── jeecg-boot/                    # 后端（Maven 多模块父工程）
│   ├── jeecg-boot-base-core/      # 框架核心（工具类、基础Controller）
│   ├── jeecg-module-system/       # 系统模块（用户/角色/权限/字典/文件上传）
│   │   ├── jeecg-system-api/      # API 定义
│   │   ├── jeecg-system-biz/      # 业务实现
│   │   └── jeecg-system-start/    # 启动入口 + 配置文件
│   ├── jeecg-module-generate/     # 【教材管理业务逻辑 - 主要开发目标】
│   │   └── src/main/java/org/jeecg/modules/demo/zbu/
│   │       ├── controller/        # 11个 Controller
│   │       ├── entity/            # 11个 Entity
│   │       ├── mapper/            # MyBatis-Plus Mapper
│   │       ├── service/           # 接口 + 实现
│   │       └── vo/                # 导出VO + ImportErrorExportUtil
│   └── jeecg-boot-module/         # Demo 和 AIRag 模块
├── jeecgboot-vue3/                # 前端
│   └── src/views/zbu/             # 【教材管理页面】
│       ├── *List.vue              # 列表页（BasicTable）
│       ├── *.api.ts               # API 接口
│       ├── *.data.ts              # 列/搜索/表单 Schema
│       └── components/            # 弹窗/表单组件
└── jeecg-boot.sql                 # 数据库（库名: jeecg-boot）
```

## 配置

- **数据库**: `jdbc:mysql://127.0.0.1:3306/jeecg-boot`，root / 123456
- **Redis**: `127.0.0.1:6379`，无密码
- **后端端口**: 8080，上下文路径 `/jeecg-boot`
- **文件上传路径**: `/opt/upFiles`（配置项 `jeecg.path.upload`）
- **前端API代理**: `.env.development` 中 `VITE_PROXY` 指向 `http://localhost:8080/jeecg-boot`

## 自定义数据库表（11张）

| 表 | 说明 | 关键外键 |
|----|------|----------|
| `t_college` | 学院 | — |
| `t_major` | 专业 | college_id → t_college |
| `t_class` | 班级 | major_id, counselor_id |
| `t_counselor` | 辅导员 | college_id, user_id → sys_user |
| `t_student` | 学生 | major_id, class_id, user_id → sys_user |
| `t_textbook` | 教材 | — |
| `t_textbook_selection` | 教材选用 | major_id, class_id, textbook_id |
| `t_subscription` | 征订 | student_id, textbook_id, selection_id |
| `t_receive` | 领取 | receive_operator(学生ID), subscription_id |
| `student_bill` | 个人账单 | student_id(学号) |
| `student_all_bill_summary` | 总账单 | college_id, major_id |

## 开发模式

### Controller 标准模式
- 继承 `JeecgController<Entity, Service>`
- `@RequestMapping("/zbu/xxx")`
- `@RequiresPermissions("zbu:表名:操作")` 权限控制
- 查询列表: `queryPageList()` + `QueryGenerator.initQueryWrapper()`
- 导出: `exportXls()` 支持自定义查询逻辑
- 导入: `importExcel()` 含逐行校验，失败时调用 `ImportErrorExportUtil.buildErrorResult()` 生成错误Excel

### Entity 标准模式
- `@TableName` 映射表名
- `@TableId(type = IdType.ASSIGN_ID)` 雪花算法主键
- `@Excel(name, dictTable, dicCode, dicText)` 导出翻译配置
- `@Dict(dictTable, dicCode, dicText)` 列表页字典翻译
- 外键字段必须配置 `dictTable`，否则导出显示数字ID
- `@TableField(exist = false)` 标记非数据库虚拟字段

### 前端标准模式（/zbu 页面）
- 列表页使用 `useListPage({ tableProps, importConfig, exportConfig })`
- `importConfig.url` 指向 `/zbu/xxx/importExcel`
- 导入结果由 `useMethods.ts` 的 `importXls` 统一处理（code=201 弹窗+自动下载错误Excel）
- `JUploadButton` 组件触发 `onImportXls` 回调

### 三种角色权限
- **admin/sysadmin** — 全局管理
- **counselor**（role_code="counselor"）— 只能管理自己班级的学生
- **student**（role_code="student"）— 查看/操作个人数据

## 导入错误处理

`ImportErrorExportUtil.buildErrorResult(errorMessages, successMsg, uploadPath, tableName)`:
- 空错误列表 → 返回 code=200 + 成功消息
- 有错误 → 生成 Excel 到 `{uploadPath}/import_error/{日期}/{表名}_导入错误_{日期}_{时间}.xlsx`，返回 code=201 触发前端弹窗+下载

## 注意事项

- 外键ID字段的 `@Excel` 必须配置 `dictTable`/`dicCode`/`dicText`，否则导出显示原始数字ID
- `@Dict` 只支持单层外键翻译，无法处理二级关联（如通过 t_subscription 查 t_textbook 名称）
- 复杂导入的 Controller 不要使用 `super.importExcel()`，需要自定义逐行校验逻辑
- 密码加密：`PasswordUtil.encrypt(用户名, 明文密码, 随机8位salt)`，学生默认密码=学号+Zbu1
