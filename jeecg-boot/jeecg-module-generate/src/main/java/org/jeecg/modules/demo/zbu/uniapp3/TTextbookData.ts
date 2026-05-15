import { render } from '@/common/renderUtils';
//列表数据
export const columns = [
    {
    title: '标段',
    align:"center",
    sorter: true,
    dataIndex: 'sectionCode'
   },
   {
    title: '编号',
    align:"center",
    sorter: true,
    dataIndex: 'businessCode'
   },
   {
    title: '征订号或书号（ISBN)',
    align:"center",
    sorter: true,
    dataIndex: 'isbn'
   },
   {
    title: '教材名称',
    align:"center",
    dataIndex: 'textbookName'
   },
   {
    title: '编著',
    align:"center",
    dataIndex: 'author'
   },
   {
    title: '出版社',
    align:"center",
    dataIndex: 'publisher'
   },
   {
    title: '出版时间',
    align:"center",
    dataIndex: 'publishDate'
   },
   {
    title: '定价',
    align:"center",
    dataIndex: 'price'
   },
   {
    title: '折扣',
    align:"center",
    dataIndex: 'discount'
   },
   {
    title: '启用学年',
    align:"center",
    sorter: true,
    dataIndex: 'enableYear'
   },
   {
    title: '启用学期',
    align:"center",
    sorter: true,
    dataIndex: 'enableSemester_dictText'
   },
   {
    title: '启用状态',
    align:"center",
    sorter: true,
    dataIndex: 'status_dictText'
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