package org.jeecg.modules.demo.zbu.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
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
 * @Description: 总账单
 * @Author: jeecg-boot
 * @Date:   2026-01-28
 * @Version: V1.0
 */
@Data
@TableName("student_all_bill_summary")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="总账单")
public class StudentAllBillSummary implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private java.lang.String id;
	/**学院ID*/
	@Excel(name = "学院ID", width = 15, dictTable = "t_college", dicCode = "id", dicText = "college_code")
	@Dict(dictTable = "t_college", dicCode = "id", dicText = "college_code")
    @Schema(description = "学院ID")
    private java.lang.String collegeId;
	/**学院名称*/
	@Excel(name = "学院名称", width = 15, dictTable = "t_college", dicCode = "id", dicText = "college_name")
	@Dict(dictTable = "t_college", dicCode = "id", dicText = "college_name")
    @Schema(description = "学院名称")
    private java.lang.String collegeName;
	/**专业ID*/
	@Excel(name = "专业ID", width = 15, dictTable = "", dicCode = "", dicText = "")
	@Dict(dictTable = "", dicCode = "", dicText = "")
    @Schema(description = "专业ID")
    private java.lang.String majorId;
	/**专业名称*/
	@Excel(name = "专业名称", width = 15, dictTable = "t_major", dicCode = "id", dicText = "major_name")
	@Dict(dictTable = "t_major", dicCode = "id", dicText = "major_name")
    @Schema(description = "专业名称")
    private java.lang.String majorName;
	/**征订学年*/
	@Excel(name = "征订学年", width = 15, dictTable = "t_subscription", dicCode = "id", dicText = "subscription_year")
	@Dict(dictTable = "t_subscription", dicCode = "id", dicText = "subscription_year")
    @Schema(description = "征订学年")
    private java.lang.String subscriptionYear;
	/**征订学期*/
	@Excel(name = "征订学期", width = 15, dictTable = "t_subscription", dicCode = "id", dicText = "subscription_semester")
	@Dict(dictTable = "t_subscription", dicCode = "id", dicText = "subscription_semester")
    @Schema(description = "征订学期")
    private java.lang.String subscriptionSemester;
	/**征订学生数*/
	@Excel(name = "征订学生数", width = 15)
    @Schema(description = "征订学生数")
    private java.lang.Long studentCount;
	/**征订教材数*/
	@Excel(name = "征订教材数", width = 15)
    @Schema(description = "征订教材数")
    private java.lang.Long textbookCount;
	/**原价总价*/
	@Excel(name = "原价总价", width = 15)
    @Schema(description = "原价总价")
    private java.math.BigDecimal originalTotal;
	/**折后总价*/
	@Excel(name = "折后总价", width = 15)
    @Schema(description = "折后总价")
    private java.math.BigDecimal discountTotal;
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
}
