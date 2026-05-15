<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" :maxHeight="500" :width="800" @ok="handleSubmit">
      <BasicForm v-if="!isStudentEditRef" @register="registerFormAdmin" name="TSubscriptionFormAdmin" />
      <BasicForm v-else @register="registerFormStudent" name="TSubscriptionFormStudent" />
  </BasicModal>
</template>

<script lang="ts" setup>
    import {ref, computed, unref, reactive} from 'vue';
    import {BasicModal, useModalInner} from '/@/components/Modal';
    import {BasicForm, useForm} from '/@/components/Form/index';
    import {formSchema, studentEditFormSchema} from '../TSubscription.data';
    import {saveOrUpdate} from '../TSubscription.api';
    import { useMessage } from '/@/hooks/web/useMessage';
    import { getDateByPicker } from '/@/utils';
    const { createMessage } = useMessage();
    // Emits声明
    const emit = defineEmits(['register','success']);
    const isUpdate = ref(true);
    const isDetail = ref(false);
    const isStudentEditRef = ref(false); // 新增：判断是否为学生编辑模式
    
    // 管理员模式表单配置
    const [registerFormAdmin, { setProps: setPropsAdmin, resetFields: resetFieldsAdmin, setFieldsValue: setFieldsValueAdmin, validate: validateAdmin, scrollToField: scrollToFieldAdmin }] = useForm({
        labelWidth: 150,
        schemas: formSchema,
        showActionButtonGroup: false,
        baseColProps: {span: 24},
        baseRowStyle: { padding: "0 20px" }
    });
    
    // 学生模式表单配置
    const [registerFormStudent, { setProps: setPropsStudent, resetFields: resetFieldsStudent, setFieldsValue: setFieldsValueStudent, validate: validateStudent, scrollToField: scrollToFieldStudent }] = useForm({
        labelWidth: 150,
        schemas: studentEditFormSchema,
        showActionButtonGroup: false,
        baseColProps: {span: 24},
        baseRowStyle: { padding: "0 20px" }
    });
    
    //表单赋值
    const [registerModal, {setModalProps, closeModal}] = useModalInner(async (data) => {
        // 根据数据中的isStudentEdit标志确定当前模式
        isStudentEditRef.value = !!data?.isStudentEdit;
        
        // 重置对应表单
        if (unref(isStudentEditRef)) {
            await resetFieldsStudent();
        } else {
            await resetFieldsAdmin();
        }
        
        setModalProps({confirmLoading: false,showCancelBtn:!!data?.showFooter,showOkBtn:!!data?.showFooter});
        isUpdate.value = !!data?.isUpdate;
        isDetail.value = !!data?.showFooter;
        
        if (unref(isUpdate)) {
            // 根据当前模式设置表单值
            if (unref(isStudentEditRef)) {
                await setFieldsValueStudent({
                    ...data.record,
                });
            } else {
                await setFieldsValueAdmin({
                    ...data.record,
                });
            }
        }
        // 隐藏底部时禁用整个表单
        if (unref(isStudentEditRef)) {
            setPropsStudent({ disabled: !data?.showFooter });
        } else {
            setPropsAdmin({ disabled: !data?.showFooter });
        }
    });
    //日期个性化选择
    const fieldPickers = reactive({
    });
    //设置标题
    const title = computed(() => (!unref(isUpdate) ? '新增' : !unref(isDetail) ? '详情' : (unref(isStudentEditRef) ? '编辑征订状态' : '编辑')));
    //表单提交事件
    async function handleSubmit(v) {
        try {
            let values;
            if (unref(isStudentEditRef)) {
                values = await validateStudent();
            } else {
                values = await validateAdmin();
            }
            
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
               if (unref(isStudentEditRef)) {
                   scrollToFieldStudent(firstField.name, { behavior: 'smooth', block: 'center' });
               } else {
                   scrollToFieldAdmin(firstField.name, { behavior: 'smooth', block: 'center' });
               }
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