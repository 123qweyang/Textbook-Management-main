---
name: counselor-user-management-fix
description: 辅导员端用户管理页面无数据问题的根因与修复
metadata:
  type: project
---

## 辅导员端用户管理页面无数据

### 根因

`SysUserController.getCounselorStudents` 查询链路正确，`LoginController.getUserInfo` 返回 `roleCode` 正确，权限也配置正确。

真正的问题在 [index.vue wrappedApi](jeecgboot-vue3/src/views/system/user/index.vue#L190-L214)：

```javascript
const allRes = await listNoCareTenant({ pageNo: 1, pageSize: 1000 });
```

系统有 20,306 个用户，`listAll` 按 `create_time DESC` 排序。辅导员管辖的 209 个学生创建时间早于 4,601 个其他用户，因此全部排在 1000 条之后。前端过滤匹配到 0 条，页面显示空数据。

### 修复

改为按当前页切片学生ID，通过 `code` 参数（后端 `queryPageList` 中 `WHERE id IN (...)` ）直接查询：

1. 对 `counselorStudentUserIds` 排序确保跨页一致
2. 按 `pageNo/pageSize` 切片，取出当前页的 ID
3. 传给 `listNoCareTenant({ pageNo:1, code: "id1,id2,..." })` 精确查询
4. 返回时补充正确的 `total`/`current`/`size`

### 数据链路

`getCounselorStudents` 三步查询：`sys_user → t_counselor → t_class → t_student → user_id`，三步都正确。

**Why:** 辅导员登录后打开用户管理页面，表格显示空数据。排查发现是 pageSize=1000 太小，学生记录排在 1000 条之外。

**How to apply:** 已修复。后续如需扩展辅导员数据权限（如只能看自己班级），参考此链路。
