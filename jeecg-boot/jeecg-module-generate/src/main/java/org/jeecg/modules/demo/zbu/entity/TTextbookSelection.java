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
 * @Description: 教材选用表
 * @Author: jeecg-boot
 * @Date: 2026-01-26
 * @Version: V1.0
 */
@Data
@TableName("t_textbook_selection")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "教材选用表")
public class TTextbookSelection implements Serializable {
	private static final long serialVersionUID = 1L;

	/** 主键 */
	@TableId(type = IdType.ASSIGN_ID)
	@Schema(description = "主键")
	private java.lang.String id;
	/** 专业 */
	@Excel(name = "专业", width = 15)
	@Schema(description = "专业")
	private java.lang.String majorId;
	/** 原始专业输入（用于导入时存储原始文本，如 "24汉本1、2、3、4班；22土木"） */
	@TableField(exist = false)
	@Schema(description = "原始专业输入")
	private java.lang.String rawMajorInput;
	/** 专业名称（用于搜索，不映射数据库） */
	@TableField(exist = false)
	@Schema(description = "专业名称")
	private java.lang.String majorName;
	/** 班级 */
	@Excel(name = "班级", width = 15, dictTable = "t_class", dicCode = "id", dicText = "class_name")
	@Dict(dictTable = "t_class", dicCode = "id", dicText = "class_name")
	@Schema(description = "班级")
	private java.lang.String classId;
	/** 班级名称（用于搜索，不映射数据库） */
	@TableField(exist = false)
	@Schema(description = "班级名称")
	private java.lang.String className;
	/** 教材 */
	@Excel(name = "教材", width = 15, dictTable = "t_textbook", dicCode = "id", dicText = "textbook_name")
	@Dict(dictTable = "t_textbook", dicCode = "id", dicText = "textbook_name")
	@Schema(description = "教材")
	private java.lang.String textbookId;
	/** 教材名称（用于搜索，不映射数据库） */
	@TableField(exist = false)
	@Schema(description = "教材名称")
	private java.lang.String textbookName;
	/** ISBN */
	@TableField(exist = false)
	@Excel(name = "ISBN", width = 20)
	@Schema(description = "ISBN")
	private java.lang.String isbn;
	/** 学年 */
	@Excel(name = "学年", width = 15)
	@Schema(description = "学年")
	private java.lang.String schoolYear;
	/** 学期 */
	@Excel(name = "学期", width = 15, dicCode = "semester")
	@Dict(dicCode = "semester")
	@Schema(description = "学期")
	private java.lang.String semester;
	/** 生效状态 */
	@Excel(name = "生效状态", width = 15, dicCode = "use_state")
	@Dict(dicCode = "use_state")
	@Schema(description = "生效状态")
	private java.lang.String selectionStatus;
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
