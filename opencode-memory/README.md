# OpenCode 项目记忆索引

本目录由 OpenCode 维护，记录教材管理云平台的开发记忆，融合了全局配置（AGENTS.md）和 Codex 遗留记忆。

## 文件清单

| 文件 | 说明 |
|------|------|
| [`dev-environment.md`](dev-environment.md) | 开发环境配置：JDK、MySQL、Redis、Node.js、Maven 及项目配置映射 |
| [`project-overview.md`](project-overview.md) | 项目概览：架构、技术栈、目录结构、配置说明 |
| [`database.md`](database.md) | 数据库：11 张业务表结构、字段、外键关系、数据链路 |
| [`controllers.md`](controllers.md) | 后端 Controller：路由映射、API 方法、权限注解、角色数据隔离 |
| [`frontend.md`](frontend.md) | 前端：页面组件、API 文件、路由机制、组件模式 |
| [`permissions.md`](permissions.md) | 角色权限体系：admin/counselor/student 三种角色的数据隔离规则 |
| [`known-issues.md`](known-issues.md) | 已知问题与修复记录（含 Codex 记忆中的两个修复案例） |

## 更新原则

- 配置变更时同步更新对应文件
- 新问题修复后记录到 `known-issues.md`
- 新增业务表/Controller/页面时更新 `database.md`、`controllers.md`、`frontend.md`
