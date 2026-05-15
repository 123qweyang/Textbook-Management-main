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
 * @Description: 教材表
 * @Author: jeecg-boot
 * @Date:   2026-01-19
 * @Version: V1.0
 */
@Data
@TableName("t_textbook")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="教材表")
public class TTextbook implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private java.lang.String id;
	/**标段*/
	@Excel(name = "标段", width = 15)
    @Schema(description = "标段")
    private java.lang.String sectionCode;
	/**编号*/
	@Excel(name = "编号", width = 15)
    @Schema(description = "编号")
    private java.lang.String businessCode;
	/**征订号或书号（ISBN)*/
	@Excel(name = "征订号或书号（ISBN)", width = 15)
    @Schema(description = "征订号或书号（ISBN)")
    private java.lang.String isbn;
	/**教材名称*/
	@Excel(name = "教材名称", width = 15)
    @Schema(description = "教材名称")
    private java.lang.String textbookName;
	/**编著*/
	@Excel(name = "编著", width = 15)
    @Schema(description = "编著")
    private java.lang.String author;
	/**出版社*/
	@Excel(name = "出版社", width = 15)
    @Schema(description = "出版社")
    private java.lang.String publisher;
	/**出版时间*/
	@Excel(name = "出版时间", width = 15)
    @Schema(description = "出版时间")
    private java.lang.String publishDate;
	/**定价*/
	@Excel(name = "定价", width = 15)
    @Schema(description = "定价")
    private java.math.BigDecimal price;
	/**折扣*/
	@Excel(name = "折扣", width = 15)
    @Schema(description = "折扣")
    private java.math.BigDecimal discount;
	/**启用学年*/
	@Excel(name = "启用学年", width = 15)
    @Schema(description = "启用学年")
    private java.lang.String enableYear;
	/**启用学期*/
	@Excel(name = "启用学期", width = 15, dicCode = "semester")
	@Dict(dicCode = "semester")
    @Schema(description = "启用学期")
    private java.lang.String enableSemester;
	/**启用状态*/
	@Excel(name = "启用状态", width = 15, dicCode = "use_state")
	@Dict(dicCode = "use_state")
    @Schema(description = "启用状态")
    private java.lang.String status;
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
