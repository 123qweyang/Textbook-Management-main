<template>
    <view>
        <!--标题和返回-->
		<cu-custom :bgColor="NavBarColor" isBack :backRouterName="backRouteName">
			<block slot="backText">返回</block>
			<block slot="content">教材表</block>
		</cu-custom>
		 <!--表单区域-->
		<view>
			<form>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">标段：</text></view>
                  <input  placeholder="请输入标段" v-model="model.sectionCode"/>
                </view>
              </view>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">编号：</text></view>
                  <input  placeholder="请输入编号" v-model="model.businessCode"/>
                </view>
              </view>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">征订号或书号（ISBN)：</text></view>
                  <input  placeholder="请输入征订号或书号（ISBN)" v-model="model.isbn"/>
                </view>
              </view>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">教材名称：</text></view>
                  <input  placeholder="请输入教材名称" v-model="model.textbookName"/>
                </view>
              </view>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">编著：</text></view>
                  <input  placeholder="请输入编著" v-model="model.author"/>
                </view>
              </view>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">出版社：</text></view>
                  <input  placeholder="请输入出版社" v-model="model.publisher"/>
                </view>
              </view>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">出版时间：</text></view>
                  <input  placeholder="请输入出版时间" v-model="model.publishDate"/>
                </view>
              </view>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">定价：</text></view>
                  <input type="number" placeholder="请输入定价" v-model="model.price"/>
                </view>
              </view>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">折扣：</text></view>
                  <input type="number" placeholder="请输入折扣" v-model="model.discount"/>
                </view>
              </view>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">启用学年：</text></view>
                  <input  placeholder="请输入启用学年" v-model="model.enableYear"/>
                </view>
              </view>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">启用学期：</text></view>
                  <input  placeholder="请输入启用学期" v-model="model.enableSemester"/>
                </view>
              </view>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">启用状态：</text></view>
                  <input  placeholder="请输入启用状态" v-model="model.status"/>
                </view>
              </view>
              <my-date label="创建日期：" v-model="model.createTime" placeholder="请输入创建日期"></my-date>
              <my-date label="更新日期：" v-model="model.updateTime" placeholder="请输入更新日期"></my-date>
				<view class="padding">
					<button class="cu-btn block bg-blue margin-tb-sm lg" @click="onSubmit">
						<text v-if="loading" class="cuIcon-loading2 cuIconfont-spin"></text>提交
					</button>
				</view>
			</form>
		</view>
    </view>
</template>

<script>
    import myDate from '@/components/my-componets/my-date.vue'

    export default {
        name: "TTextbookForm",
        components:{ myDate },
        props:{
          formData:{
              type:Object,
              default:()=>{},
              required:false
          }
        },
        data(){
            return {
				CustomBar: this.CustomBar,
				NavBarColor: this.NavBarColor,
				loading:false,
                model: {},
                backRouteName:'index',
                url: {
                  queryById: "/zbu/tTextbook/queryById",
                  add: "/zbu/tTextbook/add",
                  edit: "/zbu/tTextbook/edit",
                },
            }
        },
        created(){
             this.initFormData();
        },
        methods:{
           initFormData(){
               if(this.formData){
                    let dataId = this.formData.dataId;
                    this.$http.get(this.url.queryById,{params:{id:dataId}}).then((res)=>{
                        if(res.data.success){
                            console.log("表单数据",res);
                            this.model = res.data.result;
                        }
                    })
                }
            },
            onSubmit() {
                let myForm = {...this.model};
                this.loading = true;
                let url = myForm.id?this.url.edit:this.url.add;
				this.$http.post(url,myForm).then(res=>{
				   console.log("res",res)
				   this.loading = false
				   this.$Router.push({name:this.backRouteName})
				}).catch(()=>{
					this.loading = false
				});
            }
        }
    }
</script>
