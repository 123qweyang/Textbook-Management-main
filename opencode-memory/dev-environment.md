# 开发环境配置

> 融合自 AGENTS.md 全局配置 和 Codex 记忆 `dev-environment-config.md`

## 运行环境总览

| 层 | 环境 | 说明 |
|----|------|------|
| **运行 OpenCode** | WSL2 Ubuntu（内核 6.18.33.2） | Shell: `/bin/bash`，用户 `zhang`，WSL 内 Node v24.18.0 (nvm) |
| **开发工具链** | Windows 11 Pro | JDK / Maven / Node.js / Python / MySQL / Redis 均在 Windows 侧 |
| **编码** | WSL: UTF-8 / Windows: GBK | 跨系统读取 Windows 命令输出时需 `iconv -f GBK -t UTF-8` 转码 |

## JDK

| 版本 | 路径 | 用途 |
|------|------|------|
| JDK 17.0.18（默认） | `C:\Program Files\Java\jdk-17.0.18` | `JAVA_HOME`，项目使用 |
| JDK 8 | `C:\Program Files\Java\jdk1.8.0_481` | 历史遗留 |
| JDK 23 | `C:\Program Files\Java\jdk-23` | 历史遗留 |

## MySQL

| 项 | 值 |
|------|-----|
| 版本 | 8.0.46 |
| 地址 | `127.0.0.1:3306` |
| 用户名 | `root` |
| 密码 | `123456` |
| 数据库 | `jeecg-boot` |

## Redis

| 项 | 值 |
|------|-----|
| 版本 | 3.0.504 |
| 安装路径 | `C:\Coding\Redis-x64-3.0.504\` |
| 地址 | `127.0.0.1:6379` |
| 密码 | 无 |

## Node.js / 前端

| 项 | 值 |
|------|-----|
| Node.js（Windows） | `C:\Program Files\nodejs` |
| 包管理器 | pnpm |
| 开发端口 | 3100 |
| API 代理 | `http://localhost:8080/jeecg-boot` |

## Maven

| 项 | 值 |
|------|-----|
| 版本 | 3.9.16 |
| 安装路径 | `C:\Coding\apache-maven-3.9.16\` |
| 本地仓库 | `C:\Coding\maven_repository\` |

> **重要**：使用 `~/.opencode/bin/mvn`（自定义包装脚本，通过 `wslpath -w` 路径转换），而不是原生 Maven shell 脚本。

## Python & 文档工具

| 工具 | 版本 | 用途 |
|------|------|------|
| Python | 3.12.10 | 脚本处理 |
| python-docx | 1.2.0 | 创建/编辑 Word 文档 |
| Pandoc | 3.10 | 通用文档格式转换 |
| Poppler | 26.02.0 | PDF 工具集（pdftotext 等） |
| LibreOffice | 26.2 | Office 文档转换（备用） |

## 项目配置映射

| 配置 | 路径 |
|------|------|
| 后端配置 | `jeecg-boot/jeecg-module-system/jeecg-system-start/src/main/resources/application-dev.yml` |
| 前端环境 | `jeecgboot-vue3/.env` / `.env.development` |
| 数据库初始化 | `jeecg-boot.sql`（根目录） |
| 后端端口 | 8080，上下文路径 `/jeecg-boot` |
| 文件上传路径 | `/data/project/upload`（配置项 `jeecg.path.upload`） |

## 注意事项

- Redis 无密码，与 `application-dev.yml` 配置一致
- 文件 I/O 操作优先用 Windows 命令（`cmd.exe /c "..."` 或 `powershell.exe -Command "..."`），WSL 通过 `/mnt/c/` 跨文件系统操作效率很低（9p 协议）
- Flyway 已关闭（`enabled: false`）
