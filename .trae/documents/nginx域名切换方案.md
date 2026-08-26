# Nginx 配置域名切换方案

## 当前状态

- 旧 IP：`8.166.112.119`
- 新 IP：`118.31.226.56`
- 面板：宝塔

## 需要修改的位置（共 8 处）

### 第一处：upstream 块（自定义配置）

```nginx
# 旧
upstream 8.166.112.119 {
  server 127.0.0.1:80;
}
# 新
upstream 118.31.226.56 {
  server 127.0.0.1:80;
}
```

### 第二处：server_name

```nginx
# 旧
server_name 8.166.112.119;
# 新
server_name 118.31.226.56;
```

### 第三处：root 路径（如果目录名含 IP）

```nginx
# 旧
root /data/project/front/dist;
# 新（如目录名不含 IP 则不变）
root /data/project/front/dist;
```
> ⚠️ 如果宝塔站点根目录也用的是 IP 命名，需要同步修改。

### 第四处：extension 引用路径

```nginx
# 旧
include /www/server/panel/vhost/nginx/extension/8.166.112.119/*.conf;
# 新
include /www/server/panel/vhost/nginx/extension/118.31.226.56/*.conf;
```

### 第五处：SSL well-known 路径

```nginx
# 旧
include /www/server/panel/vhost/nginx/well-known/8.166.112.119.conf;
# 新
include /www/server/panel/vhost/nginx/well-known/118.31.226.56.conf;
```

### 第六处：伪静态 rewrite 路径

```nginx
# 旧
include /www/server/panel/vhost/rewrite/html_8.166.112.119.conf;
# 新
include /www/server/panel/vhost/rewrite/html_118.31.226.56.conf;
```

### 第七处：access_log

```nginx
# 旧
access_log  /www/wwwlogs/8.166.112.119.log;
# 新
access_log  /www/wwwlogs/118.31.226.56.log;
```

### 第八处：error_log

```nginx
# 旧
error_log  /www/wwwlogs/8.166.112.119.error.log;
# 新
error_log  /www/wwwlogs/118.31.226.56.error.log;
```

## 推荐操作方式

**直接在宝塔面板操作（推荐）**：宝塔 → 网站 → 找到 `8.166.112.119` → 设置 → 修改域名 → 填入 `118.31.226.56`。宝塔会自动更新第 2~8 处路径。

但第 1 处 `upstream` 块是自定义的（宝塔不会自动改），需要手动在宝塔的 **配置文件** 页中修改。

### 手动修改清单（如果不在宝塔操作）

| 位置 | 旧值 `8.166.112.119` | 新值 `118.31.226.56` |
|------|---------------------|---------------------|
| upstream 块名 | `upstream 8.166.112.119` | `upstream 118.31.226.56` |
| server_name | `8.166.112.119` | `118.31.226.56` |
| extension include | `.../8.166.112.119/*.conf` | `.../118.31.226.56/*.conf` |
| well-known include | `.../8.166.112.119.conf` | `.../118.31.226.56.conf` |
| rewrite include | `.../html_8.166.112.119.conf` | `.../html_118.31.226.56.conf` |
| access_log | `.../8.166.112.119.log` | `.../118.31.226.56.log` |
| error_log | `.../8.166.112.119.error.log` | `.../118.31.226.56.error.log` |

## 修改后验证

```bash
# 检查配置语法
nginx -t

# 重载配置
nginx -s reload
```
