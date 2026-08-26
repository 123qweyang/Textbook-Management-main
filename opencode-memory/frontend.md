# 前端参考

> 前端代码位于 `jeecgboot-vue3/src/views/zbu/`

## 页面组件清单（12 个）

| 页面文件 | 功能 | 对应路由 |
|----------|------|----------|
| `TCollegeList.vue` | 学院列表 | `/zbu/tCollege` |
| `TMajorList.vue` | 专业列表 | `/zbu/tMajor` |
| `TClassList.vue` | 班级列表 | `/zbu/tClass` |
| `TCounselorList.vue` | 辅导员列表 | `/zbu/tCounselor` |
| `TStudentList.vue` | 学生列表 | `/zbu/tStudent` |
| `TTextbookList.vue` | 教材列表 | `/zbu/tTextbook` |
| `TTextbookSelectionList.vue` | 教材选用列表 | `/zbu/tTextbookSelection` |
| `TSubscriptionList.vue` | 征订列表（含学生端） | `/zbu/tSubscription` |
| `TReceiveList.vue` | 领取列表 | `/zbu/tReceive` |
| `StudentBillList.vue` | 个人账单列表 | `/zbu/studentBill` |
| `StudentAllBillSummaryList.vue` | 总账单列表 | `/zbu/studentAllBillSummary` |
| `StudentBillSummary.vue` | 账单汇总页面 | 配套路由 |

## 文件组织模式（每个表三个文件）

```
src/views/zbu/
├── XXXList.vue          # 列表页（BasicTable）
├── XXX.api.ts           # API 接口定义
├── XXX.data.ts          # 列/搜索/表单 Schema
└── components/
    ├── XXXModal.vue     # 弹窗封装
    └── XXXForm.vue      # 表单内容
```

## API 文件标准接口

每个 `*.api.ts` 包含以下标准方法（以 `tCollege` 为例）：

```typescript
export const list     = (params) => get('/zbu/tCollege/list', params)
export const add      = (params) => post('/zbu/tCollege/add', params)
export const edit     = (params) => put('/zbu/tCollege/edit', params)
export const del      = (params) => del('/zbu/tCollege/delete', params)
export const delBatch = (params) => del('/zbu/tCollege/deleteBatch', params)
export const importXls = (params) => upload('/zbu/tCollege/importExcel', params)
export const exportXls = (params) => download('/zbu/tCollege/exportXls', params)
```

## data.ts 文件标准结构

每个 `*.data.ts` 导出三个 Schema：

```typescript
export const columns: BasicColumn[]       // 表格列定义（含 @Dict 字典翻译列）
export const searchFormSchema: FormSchema[] // 搜索表单字段
export const formSchema: FormSchema[]       // 新增/编辑表单字段
```

## 列表页组件模式

```vue
<template>
  <BasicTable @register="registerTable">
    <template #toolbar>
      <a-button @click="handleAdd">新增</a-button>
      <JUploadButton @importXls="handleImport" />
      <a-button @click="handleExport">导出</a-button>
    </template>
  </BasicTable>
  <XXXModal @register="registerModal" @success="reload" />
</template>
```

- 使用 `useListPage({ tableProps, importConfig, exportConfig })` 统一处理
- `importConfig.url` 指向 `/zbu/xxx/importExcel`
- 导入结果由 `useMethods.ts` 的 `importXls` 统一处理（code=201 弹窗 + 自动下载错误 Excel）
- `JUploadButton` 组件触发 `onImportXls` 回调

## 路由机制

- **动态菜单路由**：前端通过 `import.meta.glob('./modules/**/*.ts')` 自动加载路由模块
- 实际菜单数据来自后端数据库 `sys_permission` 表
- `/zbu` 路径下无静态路由文件定义，所有 zbu 页面通过后端菜单权限动态加载

## TSubscriptionList.vue 特殊处理（学生端）

`TSubscriptionList.vue` 根据当前登录用户角色切换数据源：
- **学生角色**：调用 `getMySubscription` 接口，只能看自己的征订记录
- **管理员/辅导员**：调用 `/list` 接口，可查看全部征订记录

## 导入错误处理流程

```
前端 JUploadButton → upload API → 后端 importExcel()
  ├── code=200 → 成功提示，刷新列表
  └── code=201 → 弹窗提示，自动下载后端生成的错误 Excel 文件
```
