# 角色权限体系

## 三种角色

| 角色 | role_code | 权限范围 |
|------|-----------|----------|
| 管理员 | `admin` / `sysadmin` | 全部数据，所有操作 |
| 辅导员 | `counselor` | 自己班级的学生数据 |
| 学生 | `student` | 个人数据 |

## 权限控制方式

### 1. 后端权限注解

所有 Controller 方法使用 `@RequiresPermissions` 注解：

```java
@RequiresPermissions("zbu:tStudent:add")
@RequiresPermissions("zbu:tStudent:edit")
@RequiresPermissions("zbu:tStudent:delete")
// ... 等
```

列表查询 `/list` 和按 ID 查询 `/queryById` 通常**不设权限**（公开）。

### 2. 角色级数据隔离

在 Service 层或 Controller 中通过角色代码判断：

```java
// 获取当前用户角色
String roleCode = user.getCurrentRoleCode();

// 学生只看自己的数据
if ("student".equals(roleCode)) {
    wrapper.eq("student_id", currentStudentId);
}

// 辅导员只看自己班级的学生
if ("counselor".equals(roleCode)) {
    // 通过 t_counselor → t_class → t_student 链路过滤
}
```

### 3. 前端权限控制

- 菜单根据 `sys_permission` 表中的权限配置动态加载
- `sys_role_permission` 表关联角色与权限
- 按钮级别权限由 v-permission 指令控制

## 学生端专用接口

| 接口 | Controller | 说明 |
|------|-----------|------|
| `/zbu/tSubscription/getMySubscription` | TSubscriptionController | 学生查看自己的征订 |
| `/zbu/tReceive/getMyReceive` | TReceiveController | 学生查看自己的领取记录 |
| `/zbu/tStudent/getCurrentStudent` | TStudentController | 获取当前登录学生信息 |

这些接口内部根据当前登录用户过滤数据，无需额外的权限注解。

## 新增用户时的角色分配

| 操作 | 自动操作 |
|------|----------|
| 新增学生 | 创建 `sys_user` 记录 + 分配 `student` 角色到 `sys_user_role` |
| 新增辅导员 | 创建 `sys_user` 记录 + 分配 `counselor` 角色到 `sys_user_role` |
| 删除辅导员 | 级联删除 `sys_user` + `sys_user_role` 记录 |

## 征订表权限配置（sys_role_permission 表）

| 角色 | 权限数量 | 包含 |
|------|----------|------|
| student | 7 个 | 菜单 + 6 按钮（查看、编辑、导出等） |
| counselor | 5 个 | 菜单 + 4 按钮（无删除权限） |

权限 ID 参考：父菜单 `176882849717601`，子按钮 `176882849717602`~`176882849717607`。
