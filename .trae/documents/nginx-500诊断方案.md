# Nginx 500 错误诊断方案

## 最常见的 3 个原因

### 原因1：文件权限问题（概率 80%）

nginx worker 进程通常以 `www` / `www-data` / `nobody` 用户运行，没有权限读取 `/data/project/front/dist`。

```bash
# 1. 查看 nginx 运行用户
ps aux | grep nginx | grep -v grep

# 2. 查看 dist 目录权限
ls -la /data/project/front/dist/

# 3. 修复（假设 nginx 用户是 www）
chown -R www:www /data/project/front/dist
# 或者给所有人读权限
chmod -R 755 /data/project/front/dist
```

### 原因2：index.html 不存在或 dist 为空（概率 15%）

`try_files` 找不到 `/index.html`，导致 nginx 内部错误。

```bash
ls -la /data/project/front/dist/index.html
```

如果没有，说明前端没构建成功或传错位置。

### 原因3：SELinux 拦截（CentOS/RHEL 特有，概率 5%）

```bash
# 临时关闭验证
setenforce 0

# 如果关了之后 500 消失，永久解决：
chcon -R -t httpd_sys_content_t /data/project/front/dist
setenforce 1
```

---

## 诊断步骤（直接 SSH 执行）

```bash
# 1. 看 nginx 错误日志（最关键）
tail -50 /www/wwwlogs/118.31.226.56.error.log

# 2. 如果上面路径不存在（域名切换后日志路径变了），看全局日志
tail -50 /var/log/nginx/error.log

# 3. 检查 nginx 语法
nginx -t

# 4. 检查后端是否运行
curl http://127.0.0.1:8080/jeecg-boot/
# 如果返回 JSON 或 HTML → 后端正常
# 如果 Connection refused → 后端没启动

# 5. 检查静态文件
curl -I http://127.0.0.1/
```

---

## 根据日志关键词判断

| 日志关键词 | 原因 | 解决 |
|-----------|------|------|
| `Permission denied` | 文件权限 | `chmod -R 755 /data/project/front/dist` |
| `No such file or directory` | dist 路径不存在或为空 | 重新构建并上传前端 |
| `connect() failed` / `Connection refused` | 后端未启动 | 启动 Spring Boot 服务 |
| `13: Permission denied` + ELF | SELinux | `setenforce 0` 临时关闭 |

> **把 `tail -50 /www/wwwlogs/118.31.226.56.error.log` 的输出发给我，我就能精准定位。**
