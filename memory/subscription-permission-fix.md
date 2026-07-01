---
name: subscription-permission-fix
description: 征订表权限授权功能——为学生和辅导员角色添加征订表菜单和按钮权限
metadata:
  type: project
---

## 征订表权限授权

### 问题

管理员在角色管理页面无法为学生/辅导员角色授权征订表权限——`sys_role_permission` 表中 student 和 counselor 角色缺少征订表相关记录。

老版本（JeecgBoot-main）同样存在此问题，不是后期改动引入。

### 修复

只改了数据库 `sys_role_permission` 表，代码未改动（后端 `@RequiresPermissions` 注解与其他 Controller 模式一致）。

| 角色 | 新增 | 说明 |
|------|------|------|
| student | 7个（菜单+6按钮） | 与领取表/账单一致 |
| counselor | 5个（菜单+4按钮，无删除） | 与领取表一致 |

权限ID参考：父菜单 `176882849717601`，子按钮 `176882849717602`~`176882849717607`。

### 数据隔离

学生端使用 `getMySubscription` 接口（角色过滤：学生只看自己），管理员端 `/list` 无 `@RequiresPermissions`（与领取表/账单一致）。

**Why:** 管理员需要在角色管理页面统一管理征订表权限，与领取表、账单的操作体验一致。

**How to apply:** 后续如需同样方式处理其他表，参照此模式分配 `sys_role_permission`。
