import { render } from '@/common/renderUtils';
//列表数据
export const columns = [
    {
    title: '班级名称',
    align:"center",
    sorter: true,
    dataIndex: 'className'
   },
   {
    title: '班级编码',
    align:"center",
    sorter: true,
    dataIndex: 'classCode'
   },
   {
    title: '所属专业',
    align:"center",
    dataIndex: 'majorId'
   },
   {
    title: '辅导员',
    align:"center",
    dataIndex: 'counselorId'
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