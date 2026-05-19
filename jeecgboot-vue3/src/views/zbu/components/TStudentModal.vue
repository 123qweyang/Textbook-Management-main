<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" :maxHeight="500" :width="800" @ok="handleSubmit">
    <BasicForm @register="registerForm" name="TStudentForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
import {ref, computed, unref, reactive} from 'vue';
import {BasicModal, useModalInner} from '/@/components/Modal';
import {BasicForm, useForm} from '/@/components/Form/index';
import {formSchema} from '../TStudent.data';
import {saveOrUpdate, getClassListByMajor} from '../TStudent.api';
import { useMessage } from '/@/hooks/web/useMessage';
import { getDateByPicker } from '/@/utils';
const { createMessage } = useMessage();
// Emits声明
const emit = defineEmits(['register','success']);
const isUpdate = ref(true);
const isDetail = ref(false);
//表单配置
const [registerForm, { setProps,resetFields, setFieldsValue, validate, scrollToField, updateSchema }] = useForm({
  labelWidth: 150,
  schemas: formSchema,
  showActionButtonGroup: false,
  baseColProps: {span: 24},
  baseRowStyle: { padding: "0 20px" }
});
//表单赋值
const [registerModal, {setModalProps, closeModal}] = useModalInner(async (data) => {
  //重置表单
  await resetFields();
  setModalProps({confirmLoading: false,showCancelBtn:!!data?.showFooter,showOkBtn:!!data?.showFooter});
  isUpdate.value = !!data?.isUpdate;
  isDetail.value = !!data?.showFooter;
  if (unref(isUpdate)) {
    // 编辑时：先根据专业加载班级选项，再赋值，避免classId回写成ID
    const record = data.record;
    if (record.majorId) {
      try {
        const result = await getClassListByMajor(record.majorId);
        if (result && result.length > 0) {
          const classOptions = result.map((item: any) => ({
            label: item.className,
            value: item.id,
          }));
          await updateSchema([{
            field: 'classId',
            component: 'Select',
            componentProps: {
              options: classOptions,
              placeholder: '请选择班级',
            },
          }]);
        }
      } catch (e) {
        console.warn('加载班级选项失败', e);
      }
    }
    //表单赋值
    await setFieldsValue({
      ...record,
    });
  }
  // 隐藏底部时禁用整个表单
  setProps({ disabled: !data?.showFooter })
});
//日期个性化选择
const fieldPickers = reactive({
});
//设置标题
const title = computed(() => (!unref(isUpdate) ? '新增' : !unref(isDetail) ? '详情' : '编辑'));
//表单提交事件
async function handleSubmit(v) {
  try {
    let values = await validate();
    // 预处理日期数据
    changeDateValue(values);
    setModalProps({confirmLoading: true});
    //提交表单
    await saveOrUpdate(values, isUpdate.value);
    //关闭弹窗
    closeModal();
    //刷新列表
    emit('success');
  } catch ({ errorFields }) {
    if (errorFields) {
      const firstField = errorFields[0];
      if (firstField) {
        scrollToField(firstField.name, { behavior: 'smooth', block: 'center' });
      }
    }
    return Promise.reject(errorFields);
  } finally {
    setModalProps({confirmLoading: false});
  }
}

/**
 * 处理日期值
 * @param formData 表单数据
 */
const changeDateValue = (formData) => {
  if (formData && fieldPickers) {
    for (let key in fieldPickers) {
      if (formData[key]) {
        formData[key] = getDateByPicker(formData[key], fieldPickers[key]);
      }
    }
  }
};

</script>

<style lang="less" scoped>
/** 时间和数字输入框样式 */
:deep(.ant-input-number) {
  width: 100%;
}

:deep(.ant-calendar-picker) {
  width: 100%;
}
</style>
