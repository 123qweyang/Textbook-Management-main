import { render } from '@/common/renderUtils';
//列表数据
export const columns = [
    {
    title: '专业',
    align:"center",
    sorter: true,
    dataIndex: 'majorId'
   },
   {
    title: '班级',
    align:"center",
    sorter: true,
    dataIndex: 'classId'
   },
   {
    title: '教材',
    align:"center",
    dataIndex: 'textbookId'
   },
   {
    title: '学年',
    align:"center",
    sorter: true,
    dataIndex: 'schoolYear'
   },
   {
    title: '学期',
    align:"center",
    sorter: true,
    dataIndex: 'semester_dictText'
   },
   {
    title: '生效状态',
    align:"center",
    sorter: true,
    dataIndex: 'selectionStatus_dictText'
   },
   {
    title: '备注',
    align:"center",
    dataIndex: 'remark'
   },
   {
    title: '创建日期',
    align:"center",
    dataIndex: 'createTime'
   },
   {
    title: '更新日期',
    align:"center",
    dataIndex: 'updateTime'
   },
];