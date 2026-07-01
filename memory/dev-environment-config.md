---
name: dev-environment-config
description: 本地开发环境配置——Java、MySQL、Redis、Node.js 及连接信息
metadata:
  type: reference
---

## 本地开发环境配置

### JDK

| 版本 | 路径 |
|------|------|
| JDK 8 | `C:\Program Files\Java\jdk1.8.0_481` |
| JDK 17（默认） | `C:\Program Files\Java\jdk-17.0.18` |
| JDK 23 | `C:\Program Files\Java\jdk-23` |

`JAVA_HOME` 指向 JDK 17。项目使用 JDK 17。

### MySQL

| 项 | 值 |
|------|-----|
| 版本 | 8.0.46 |
| 地址 | `127.0.0.1:3306` |
| 用户名 | `root` |
| 密码 | `123456` |
| 数据库 | `jeecg-boot` |

### Redis

| 项 | 值 |
|------|-----|
| 版本 | 3.0.504 |
| 安装路径 | `C:\Coding\Redis-x64-3.0.504\` |
| 地址 | `127.0.0.1:6379` |
| 密码 | 无 |

### Node.js / 前端

| 项 | 值 |
|------|-----|
| Node.js | `C:\Program Files\nodejs` |
| 包管理器 | pnpm |
| 前端端口 | 3100（开发模式） |
| API 代理 | `http://localhost:8080/jeecg-boot` |

### Maven

| 项 | 值 |
|------|-----|
| 版本 | 3.9.16 |
| 安装路径 | `C:\Coding\apache-maven-3.9.16\` |

### 文档工具

| 工具 | 版本 | 路径 |
|------|------|------|
| Pandoc | 3.10 | `C:\Users\27966\AppData\Local\Pandoc\` |
| Poppler | 26.02.0 | `C:\Coding\poppler-26.02.0\Library\bin\` |
| Python | 3.12.10 | `C:\Users\27966\AppData\Local\Programs\Python\Python312\` |
| python-docx | 1.2.0 | Python 库，**主要**用于创建/编辑 Word 文档 |
| LibreOffice | 26.2 | `C:\Program Files\LibreOffice\program\soffice.exe`（备用） |

- **Pandoc** — 通用文档格式转换（Markdown ↔ docx ↔ HTML 等）
- **Poppler** — PDF 工具集（`pdftotext`、`pdfinfo`、`pdftoppm`、`pdftohtml` 等）。已通过 `~/.bashrc` 将 Poppler bin 目录置于 PATH 最前，确保优先于 Git Bash 自带的 MinGW `pdftotext` 4.00
- **python-docx** — Python 库，**主要**用于创建和编辑 Word 文档（.docx）
- **Python** — 通过 winget 安装，`~/.bashrc` 中置于 PATH 最前。含 python-docx、lxml 等文档处理库
- **LibreOffice** — Office 文档转换（docx ↔ PDF 等），通过 `--headless` 模式命令行调用。**备用**，优先使用 python-docx

### 项目配置映射

- 后端配置：[application-dev.yml](jeecg-boot/jeecg-module-system/jeecg-system-start/src/main/resources/application-dev.yml)
- 前端环境：[.env](jeecgboot-vue3/.env)、[.env.development](jeecgboot-vue3/.env.development)
- 数据库初始化：[jeecg-boot.sql](jeecg-boot.sql)

### 注意事项

- Redis 无密码，与项目 `application-dev.yml` 配置一致。
- JDK 8 和 JDK 23 为历史遗留安装，项目仅使用 JDK 17。
- 文件上传路径：`/opt/upFiles`（配置项 `jeecg.path.upload`）。

**Why:** 汇集所有开发环境配置信息，包括数据库和中间件的连接凭据，避免每次启动项目时反复查找。

**How to apply:** 当需要连接 MySQL/Redis、切换 JDK 版本、或解决连接问题时参考此文件。配置有变更时同步更新。
