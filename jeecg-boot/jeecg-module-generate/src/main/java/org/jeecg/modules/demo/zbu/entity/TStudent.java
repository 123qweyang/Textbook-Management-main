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
 * @Description: 学生表
 * @Author: jeecg-boot
 * @Date:   2026-01-19
 * @Version: V1.0
 */
@Data
@TableName("t_student")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="学生表")
public class TStudent implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private java.lang.String id;
	/**学号*/
	@Excel(name = "学号", width = 15)
    @Schema(description = "学号")
    private java.lang.String studentId;
    //关联用户ID
    @Schema(description = "关联系统用户ID")
    private java.lang.String userId;
	/**学生姓名*/
	@Excel(name = "学生姓名", width = 15)
    @Schema(description = "学生姓名")
    private java.lang.String studentName;
	/**专业*/
	@Excel(name = "专业", width = 15, dictTable = "t_major", dicCode = "id", dicText = "major_name")
	@Dict(dictTable = "t_major", dicCode = "id", dicText = "major_name")
    @Schema(description = "专业")
    private java.lang.String majorId;
	/**班级*/
	@Excel(name = "班级", width = 15, dictTable = "t_class", dicCode = "id", dicText = "class_name")
	@Dict(dictTable = "t_class", dicCode = "id", dicText = "class_name")
    @Schema(description = "班级")
    private java.lang.String classId;
	/**状态*/
	@Excel(name = "状态", width = 15, dicCode = "use_state")
	@Dict(dicCode = "use_state")
    @Schema(description = "状态")
    private java.lang.String status;
	/**入学年份*/
	@Excel(name = "入学年份", width = 15)
    @Schema(description = "入学年份")
    private java.lang.String admissionYear;
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
