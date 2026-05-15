package org.jeecg.modules.demo.zbu.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import org.jeecg.common.constant.ProvinceCityArea;
import org.jeecg.common.util.SpringContextUtils;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 个人账单
 * @Author: jeecg-boot
 * @Date: 2026-01-25
 * @Version: V1.0
 */
@Data
@TableName("student_bill")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "个人账单")
public class StudentBill implements Serializable {
	private static final long serialVersionUID = 1L;

	/** 主键 */
	@TableId(type = IdType.ASSIGN_ID)
	@Schema(description = "主键")
	private java.lang.String id;
	/** 学号 */
	@Excel(name = "学号", width = 15, dictTable = "t_student", dicCode = "id", dicText = "student_id")
	@Dict(dictTable = "t_student", dicCode = "id", dicText = "student_id")
	@Schema(description = "学号")
	private java.lang.String studentId;
	/** 学生姓名 */
	@TableField(exist = false)
	@Excel(name = "学生姓名", width = 15)
	@Schema(description = "学生姓名")
	private java.lang.String studentName;
	/** 班级 */
	@TableField(exist = false)
	@Schema(description = "班级")
	private java.lang.String className;
	/** 学院（仅搜索用） */
	@TableField(exist = false)
	@Schema(description = "学院")
	private java.lang.String collegeName;
	/** 专业 */
	@Excel(name = "专业", width = 15, dictTable = "t_major", dicCode = "id", dicText = "major_name")
	@Dict(dictTable = "t_major", dicCode = "id", dicText = "major_name")
	@Schema(description = "专业")
	private java.lang.String majorName;
	/** 征订学年 */
	@Excel(name = "征订学年", width = 15, dictTable = "t_subscription", dicCode = "id", dicText = "subscription_year")
	@Dict(dictTable = "t_subscription", dicCode = "id", dicText = "subscription_year")
	@Schema(description = "征订学年")
	private java.lang.String subscriptionYear;
	/** 征订学期 */
	@Excel(name = "征订学期", width = 15, dictTable = "t_subscription", dicCode = "id", dicText = "subscription_semester")
	@Dict(dictTable = "t_subscription", dicCode = "id", dicText = "subscription_semester")
	@Schema(description = "征订学期")
	private java.lang.String subscriptionSemester;
	/** 教材名称 */
	@Excel(name = "教材名称", width = 15, dictTable = "t_textbook", dicCode = "id", dicText = "textbook_name")
	@Dict(dictTable = "t_textbook", dicCode = "id", dicText = "textbook_name")
	@Schema(description = "教材名称")
	private java.lang.String textbookName;
	/** ISBN */
	@TableField(exist = false)
	@Excel(name = "ISBN", width = 20)
	@Schema(description = "ISBN")
	private java.lang.String isbn;
	/** 教材定价 */
	@Excel(name = "教材定价", width = 15, dictTable = "t_textbook", dicCode = "id", dicText = "price")
	@Dict(dictTable = "t_textbook", dicCode = "id", dicText = "price")
	@Schema(description = "教材定价")
	private java.math.BigDecimal price;
	/** 教材费用 */
	@Excel(name = "教材费用", width = 15)
	@Schema(description = "教材费用")
	private java.math.BigDecimal discountPrice;
	/** 征订状态 */
	@Excel(name = "征订状态", width = 15, dictTable = "t_subscription", dicCode = "id", dicText = "subscribe_status")
	@Dict(dictTable = "t_subscription", dicCode = "id", dicText = "subscribe_status")
	@Schema(description = "征订状态")
	private java.lang.String subscribeStatus;
	/** 领取状态 */
	@Excel(name = "领取状态", width = 15, dictTable = "t_receive", dicCode = "id", dicText = "receive_status")
	@Dict(dictTable = "t_receive", dicCode = "id", dicText = "receive_status")
	@Schema(description = "领取状态")
	private java.lang.String receiveStatus;
	/** 备注 */
	@Excel(name = "备注", width = 15)
	@Schema(description = "备注")
	private java.lang.String remark;
	/** 创建日期 */
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(description = "创建日期")
	private java.util.Date createTime;
	/** 更新日期 */
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(description = "更新日期")
	private java.util.Date updateTime;
}
