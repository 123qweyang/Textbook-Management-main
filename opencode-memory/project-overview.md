# 项目概览

## 项目名称

教材管理云平台

## 基础框架

JeecgBoot 3.9.0（Spring Boot 3.5.5 + MyBatis-Plus + Vue 3 + Ant Design Vue 4.2 + Vite 6）

## 目录结构

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
│   │       ├── controller/        # 11 个 Controller
│   │       ├── entity/            # 12 个 Entity
│   │       ├── mapper/            # 11 个 Mapper + XML
│   │       ├── service/           # 11 个 Service 接口 + 实现
│   │       └── vo/                # 6 个 VO/导出工具类
│   ├── jeecg-boot-module/         # Demo 和 AIRag 模块
│   └── jeecg-server-cloud/        # 微服务模块
├── jeecgboot-vue3/                # 前端
│   └── src/views/zbu/             # 【教材管理页面】
│       ├── *List.vue              # 11 个列表页 + 1 个汇总页
│       ├── *.api.ts               # 11 个 API 接口文件
│       ├── *.data.ts              # 11 个列/搜索/表单 Schema
│       └── components/            # 22 个弹窗/表单子组件
├── jeecg-boot.sql                 # 数据库初始化（约21209行）
├── 数据库同步.sql                  # 额外同步脚本
├── docker-compose.yml
└── opencode-memory/               # 【本目录】OpenCode 项目记忆
```

## 核心启动类

`jeecg-boot/jeecg-module-system/jeecg-system-start/src/main/java/org/jeecg/JeecgSystemApplication.java`

## 常用命令

### 后端

```bash
# 完整编译
cd jeecg-boot && mvn compile -DskipTests

# 只编译教材管理模块
cd jeecg-boot && mvn compile -pl jeecg-module-generate -am -DskipTests

# 启动（使用 IDEA 或 java -jar 运行 JeecgSystemApplication）
```

### 前端

```bash
cd jeecgboot-vue3
pnpm dev          # 端口 3100，API 代理到 localhost:8080
pnpm build        # 生产构建
```

## 本地访问

- 前端：`http://localhost:3100`
- 后端 API：`http://localhost:8080/jeecg-boot`
- 文件上传目录：`/data/project/upload`

## 技术栈细节

| 层 | 技术 | 版本 |
|----|------|------|
| 后端框架 | Spring Boot | 3.5.5 |
| ORM | MyBatis-Plus | (JeecgBoot 内置) |
| 前端框架 | Vue | 3.5 |
| UI 组件 | Ant Design Vue | 4.2 |
| 构建 | Vite | 6.3 |
| 状态管理 | Pinia | 2.1 |
| 路由 | Vue Router | 4.5 |
| HTTP | Axios | 1.12 |
| 语言 | TypeScript | 5.9 |

## AI 模型配置

| 项 | 值 |
|------|-----|
| 模型 | deepseek-chat |
| 配置位置 | `application-dev.yml` |
