# 教材管理云平台（Textbook Management）

面向高校的**教材全流程管理云平台**，覆盖「学院 → 专业 → 班级 → 学生 → 教材选用 → 征订 → 领取 → 账单」完整业务链路。

项目基于 **JeecgBoot 3.9.0** 低代码平台二次开发，业务代码集中在后端 `jeecg-module-generate` 模块的 `zbu` 包与前端 `src/views/zbu` 目录，支持管理员 / 辅导员 / 学生三种角色的数据隔离与权限控制。

---

## ✨ 功能特性

### 基础数据管理
- **学院 / 专业 / 班级 / 辅导员 / 学生** 五类基础数据维护，支持 Excel 批量导入导出
- 新增学生、辅导员时自动创建 `sys_user` 登录账号并分配对应角色
- 学生表支持按专业级联选择班级、学号模糊搜索

### 教材管理
- 教材信息维护：ISBN、定价、折扣率、适用学年学期、使用状态
- 教材选用：按专业/班级配置教材，支持学年学期筛选、生效状态管理
- 新增教材选用时自动为该专业下所有学生生成征订记录

### 征订管理
- 学生端「同意征订」一键确认，自动创建领取记录
- 批量标记征订状态，学生仅允许操作**当前学年学期**的数据（6/12 月学期边界）
- 征订状态变更自动联动：创建/删除领取记录、同步个人账单、回滚总账单
- 并发安全：学生级锁防止重复创建领取记录（写偏斜防护）

### 领取与账单
- 领取记录跟踪（未领取/已领取），状态批量更新
- 个人账单：从征订数据自动同步（含专业/班级/学院/ISBN 冗余字段）
- 总账单：按学院 + 专业 + 学年 + 学期汇总，支持手动触发与 `@Scheduled` 定时自动汇总

### 数据能力
- **大数量级流式导出**：SXSSFWorkbook + JDBC 分批查询（5000 条/批），支持几十万级数据导出
- **导入错误回传**：逐行校验，出错行生成错误 Excel（`code=201` 触发前端弹窗自动下载）
- 导出/导入均带角色数据过滤，与页面查询条件保持一致

---

## 🛠 技术栈

| 层 | 技术 | 版本 |
|----|------|------|
| 后端框架 | Spring Boot | 3.5.5 |
| 持久层 | MyBatis-Plus | 3.5.12 |
| 安全框架 | Apache Shiro + JWT | 2.0.4 / 4.5.0 |
| 语言 / 构建 | Java / Maven | JDK 17 / 3.9.x |
| 前端框架 | Vue 3 + TypeScript | 3.5 / 5.9 |
| UI 组件 | Ant Design Vue | 4.2 |
| 构建工具 | Vite | 6.x |
| 状态管理 | Pinia | 2.1 |
| 数据库 | MySQL | 8.0.46（库名 `jeecg-boot`） |
| 缓存 | Redis | 3.x（无密码） |
| 报表/AI 能力 | JimuReport / JeecgBoot AI 平台（deepseek-chat） | 内置 |

---

## 📁 目录结构

```
Textbook-Management-main/
├── jeecg-boot/                    # 后端（Maven 多模块父工程）
│   ├── jeecg-boot-base-core/      # 框架核心（工具类、基础 Controller）
│   ├── jeecg-module-system/       # 系统模块（用户/角色/权限/字典/文件上传）
│   │   └── jeecg-system-start/    # 启动入口 + 配置文件（application-dev.yml 等）
│   ├── jeecg-module-generate/     # 【教材管理业务逻辑 - 主要开发模块】
│   │   └── src/main/java/org/jeecg/modules/demo/zbu/
│   │       ├── controller/        # 11 个 Controller（业务核心）
│   │       ├── entity/            # 12 个 Entity
│   │       ├── mapper/            # Mapper 接口
│   │       ├── service/           # 接口 + 实现
│   │       └── vo/                # 导出 VO + ImportErrorExportUtil 等
│   └── jeecg-boot-module/         # Demo 和 AIRag 模块
├── jeecgboot-vue3/                # 前端
│   └── src/views/zbu/             # 【教材管理页面】
│       ├── *List.vue              # 12 个列表/汇总页（BasicTable）
│       ├── *.api.ts / *.data.ts   # API 接口与列/搜索/表单 Schema
│       └── components/            # 弹窗/表单子组件
├── jeecg-boot.sql                 # 数据库初始化脚本（原始版本，约 2.1 万行）
├── 数据库同步.sql                  # 字典清洗 + 数据修正（学期/状态归一化）
├── incremental_sync.sql           # 增量同步脚本（建视图 v_*、加冗余字段等）
├── memory/                        # 项目记忆（环境配置 / 修复记录）
├── opencode-memory/               # 开发记忆（项目概览 / 数据库 / 权限 / 已知问题）
└── CLAUDE.md                      # Claude Code 开发指南
```

---

## 🗄 数据模型（11 张业务表）

| 表 | 说明 | 关键外键 |
|----|------|----------|
| `t_college` | 学院 | — |
| `t_major` | 专业 | college_id → t_college |
| `t_class` | 班级 | major_id → t_major, counselor_id → t_counselor |
| `t_counselor` | 辅导员 | college_id, user_id → sys_user |
| `t_student` | 学生 | major_id, class_id, user_id → sys_user |
| `t_textbook` | 教材 | — |
| `t_textbook_selection` | 教材选用 | major_id, class_id, textbook_id |
| `t_subscription` | 征订 | student_id, textbook_id, selection_id |
| `t_receive` | 领取 | receive_operator(学生ID), subscription_id |
| `student_bill` | 个人账单 | student_id(学号)，冗余专业/班级/学院/ISBN |
| `student_all_bill_summary` | 总账单 | college_id, major_id |

### 数据关系链

```
t_college
  ├── t_major
  │     ├── t_class
  │     │     └── t_student
  │     └── t_textbook_selection (按专业/班级选用教材)
  └── t_counselor (辅导员属于学院)

t_textbook → t_textbook_selection → t_subscription (学生征订)
                                        ├── t_receive (领取记录)
                                        └── student_bill (个人账单)
student_bill → student_all_bill_summary (按学院+专业+学年+学期汇总)
```

> 另建有 7 个查询视图（`v_subscription_with_details`、`v_receive_with_details`、`v_student_bill_summary` 等），定义见 `incremental_sync.sql`。

---

## 🔐 角色权限体系

| 角色 | role_code | 数据范围 |
|------|-----------|----------|
| 管理员 | `admin` / `sysadmin` | 全部数据，所有操作 |
| 辅导员 | `counselor` | 仅自己管理班级的学生（经 t_counselor → t_class → t_student 链路过滤） |
| 学生 | `student` | 仅本人数据 |

- 后端：`@RequiresPermissions("zbu:表名:操作")` 按钮权限 + Controller 内角色级数据隔离
- 前端：菜单由 `sys_permission` 动态加载，按钮级权限由 `v-auth` 指令控制，页面按角色切换操作入口（如征订页学生端显示「同意征订」、管理端显示「批量标记为已征订」）
- 学生端专用接口（`getMySubscription` / `getMyReceive` / `getCurrentStudent`）按登录用户自动过滤

---

## 🚀 快速启动

### 环境要求

| 依赖 | 版本要求 |
|------|----------|
| JDK | 17 |
| MySQL | 8.x（root/123456，库 `jeecg-boot`） |
| Redis | 任意版本（127.0.0.1:6379，无密码） |
| Node.js | 20+ |
| pnpm | 9+ |
| Maven | 3.9.x |

### 1. 初始化数据库

```bash
# 方式一：导入完整初始化脚本（原始版本）
mysql -uroot -p123456 < jeecg-boot.sql

# 方式二（推荐，宝塔/已有库场景）：导入原始脚本后执行增量同步
# 执行 数据库同步.sql 与 incremental_sync.sql
```

> ⚠️ 注意：`jeecg-boot.sql` 为最初版本，开发过程中数据库已多次调整（新增冗余字段、7 个视图、字典修正），**以实际数据库结构为准**，增量脚本见根目录两个 SQL 文件。

### 2. 启动后端

```bash
cd jeecg-boot
mvn compile -pl jeecg-module-generate -am -DskipTests   # 只编译教材模块
# 或完整编译
mvn compile -DskipTests
```

主类：`org.jeecg.JeecgSystemApplication`（位于 `jeecg-module-system/jeecg-system-start`），默认端口 **8080**，上下文路径 `/jeecg-boot`。

### 3. 启动前端

```bash
cd jeecgboot-vue3
pnpm install
pnpm dev     # 端口 3100，API 代理到 http://localhost:8080/jeecg-boot
```

访问 **http://localhost:3100**，默认账号 `admin / 123456`。

---

## 📝 常用命令

```bash
# 后端完整编译
cd jeecg-boot && mvn compile -DskipTests

# 只编译教材管理模块
cd jeecg-boot && mvn compile -pl jeecg-module-generate -am -DskipTests

# 前端开发模式 / 生产构建
cd jeecgboot-vue3 && pnpm dev
cd jeecgboot-vue3 && pnpm build
```

---

## 📊 Excel 导入导出约定

- 外键 ID 字段的 `@Excel` 必须配置 `dictTable` / `dicCode` / `dicText`，否则导出显示原始数字 ID
- `@Dict` 只支持单层外键翻译，无法处理二级关联（如通过 t_subscription 查 t_textbook 名称）
- 复杂导入的 Controller 自定义逐行校验，失败行调用 `ImportErrorExportUtil.buildErrorResult()` 生成错误 Excel（`{uploadPath}/import_error/{日期}/` 目录）
- 导入结果前端统一处理：`code=200` 成功提示，`code=201` 弹窗 + 自动下载错误 Excel

## 🔑 账号密码规则

- 加密方式：`PasswordUtil.encrypt(用户名, 明文密码, 随机8位salt)`
- 学生默认密码：学号 + `Zbu1`
- 辅导员默认密码：工号 + `Zbu1`

---

## 📚 项目文档

| 文档 | 内容 |
|------|------|
| [CLAUDE.md](./CLAUDE.md) | Claude Code 开发指南（架构/标准模式/注意事项） |
| [memory/](./memory/) | 环境配置、硬件配置、修复记录 |
| [opencode-memory/](./opencode-memory/) | 项目概览、数据库字段、权限体系、Controller 参考、已知问题 |
| [README-AI.md](./README-AI.md) | JeecgBoot AI 平台说明（框架自带） |
| [README-old.md](./README-old.md) | JeecgBoot 官方原版 README（框架自带，归档保留） |

---

## 🌿 分支说明

- `main` — 主干分支
- `CAS` — CAS 安全加固分支（JWT 有效期 2h、CAS ticket 校验、XSS 全局防护、JmReport SSTI 阻断）

---

## 🗓 近期更新

- 安全加固：JmReport SSTI 阻断、JWT 2h 缩短、CAS ticket 校验、XSS 全局防护
- 征订/领取并发重复写入与学年学期越权问题修复
- 跨页全选、导出筛选条件补齐、学期/状态字典归一化
- 辅导员用户管理 1000 条分页截断修复（系统 2 万+ 用户场景）
- 征订表角色权限授权（student 7 个 / counselor 5 个）
- Tomcat / Druid 连接池高并发压测优化
