package org.jeecg.modules.demo.zbu.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.*;
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
 * @Description: 征订表
 * @Author: jeecg-boot
 * @Date:   2026-01-19
 * @Version: V1.0
 */
@Data
@TableName("t_subscription")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="征订表")
public class TSubscription implements Serializable {
	private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
	@Schema(description = "主键")
	private java.lang.String id;
	/**学生*/
	@Excel(name = "学号", width = 15, dictTable = "t_student", dicCode = "id", dicText = "student_id")
	@Dict(dictTable = "t_student", dicCode = "id", dicText = "student_id")
	@Schema(description = "学生")
	private java.lang.String studentId;
	/**学生姓名*/
	@TableField(exist = false)
	@Excel(name = "学生姓名", width = 15)
	@Schema(description = "学生姓名")
	private java.lang.String studentName;
	/**教材*/
	@Excel(name = "教材", width = 15, dictTable = "t_textbook", dicCode = "id", dicText = "textbook_name")
	@Dict(dictTable = "t_textbook", dicCode = "id", dicText = "textbook_name")
	@Schema(description = "教材")
	private java.lang.String textbookId;
	/**教材选用*/
	@Excel(name = "教材选用", width = 15, dictTable = "t_textbook_selection", dicCode = "id", dicText = "selection_status")
	@Dict(dictTable = "t_textbook_selection", dicCode = "id", dicText = "selection_status")
	@Schema(description = "教材选用")
	private java.lang.String selectionId;
	/**专业*/
	@Excel(name = "专业", width = 15, dictTable = "t_major", dicCode = "id", dicText = "major_name")
	@Dict(dictTable = "t_major", dicCode = "id", dicText = "major_name")
	@Schema(description = "专业")
	private java.lang.String majorId;
	/**征订学年*/
	@Excel(name = "征订学年", width = 15)
	@Schema(description = "征订学年")
	private java.lang.String subscriptionYear;
	/**征订学期*/
	@Excel(name = "征订学期", width = 15, dicCode = "semester")
	@Dict(dicCode = "semester")
	@Schema(description = "征订学期")
	private java.lang.String subscriptionSemester;
	/**征订状态*/
	@Excel(name = "征订状态", width = 15, dicCode = "subscribe_status")
	@Dict(dicCode = "subscribe_status")
	@Schema(description = "征订状态")
	private java.lang.String subscribeStatus;
	/**征订备注*/
	@Excel(name = "征订备注", width = 15)
	@Schema(description = "征订备注")
	private java.lang.String remark;
	/**征订操作时间*/
	@Excel(name = "征订操作时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@Schema(description = "征订操作时间")
	private java.util.Date subscribeTime;
	/**征订截止时间*/
//	@Excel(name = "征订截止时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
//	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
//    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
//    @Schema(description = "征订截止时间")
	private java.util.Date deadline;
	/**创建日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@Schema(description = "创建日期")
	private java.util.Date createTime;
	/**更新日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@Schema(description = "更新日期")
	private java.util.Date updateTime;

	@TableField(exist = false)
	@Excel(name = "学院", width = 20)
	private String collegeName;
}
