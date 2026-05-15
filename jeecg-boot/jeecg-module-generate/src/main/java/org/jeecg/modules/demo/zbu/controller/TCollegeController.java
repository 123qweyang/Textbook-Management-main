package org.jeecg.modules.demo.zbu.controller;

import java.util.*;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.query.QueryRuleEnum;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.demo.zbu.entity.TCollege;
import org.jeecg.modules.demo.zbu.service.ITCollegeService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.modules.demo.zbu.vo.ImportErrorExportUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.apache.shiro.authz.annotation.RequiresPermissions;
/**
 * @Description: 学院表
 * @Author: jeecg-boot
 * @Date:   2026-01-19
 * @Version: V1.0
 */
@Tag(name="学院表")
@RestController
@RequestMapping("/zbu/tCollege")
@Slf4j
public class TCollegeController extends JeecgController<TCollege, ITCollegeService> {
	@Autowired
	private ITCollegeService tCollegeService;
	@Autowired
	private RedisUtil redisUtil;
	@Value("${jeecg.path.upload}")
	private String uploadPath;

	/**
	 * 分页列表查询
	 *
	 * @param tCollege
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "学院表-分页列表查询")
	@Operation(summary="学院表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<TCollege>> queryPageList(TCollege tCollege,
												 @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
												 @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
												 HttpServletRequest req) {


		QueryWrapper<TCollege> queryWrapper = QueryGenerator.initQueryWrapper(tCollege, req.getParameterMap());
		Page<TCollege> page = new Page<TCollege>(pageNo, pageSize);
		IPage<TCollege> pageList = tCollegeService.page(page, queryWrapper);
		return Result.OK(pageList);
	}

	/**
	 *   添加
	 *
	 * @param tCollege
	 * @return
	 */
	@AutoLog(value = "学院表-添加")
	@Operation(summary="学院表-添加")
	@RequiresPermissions("zbu:t_college:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody TCollege tCollege) {
		// 检查学院编码是否已存在
		QueryWrapper<TCollege> codeWrapper = new QueryWrapper<>();
		codeWrapper.eq("college_code", tCollege.getCollegeCode());
		if (tCollegeService.count(codeWrapper) > 0) {
			return Result.error("添加失败：学院编码已存在！");
		}

		// 检查学院名称是否已存在
		QueryWrapper<TCollege> nameWrapper = new QueryWrapper<>();
		nameWrapper.eq("college_name", tCollege.getCollegeName());
		if (tCollegeService.count(nameWrapper) > 0) {
			return Result.error("添加失败：学院名称已存在！");
		}

		tCollegeService.save(tCollege);

		return Result.OK("添加成功！");
	}

	/**
	 *  编辑
	 *
	 * @param tCollege
	 * @return
	 */
	@AutoLog(value = "学院表-编辑")
	@Operation(summary="学院表-编辑")
	@RequiresPermissions("zbu:t_college:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody TCollege tCollege) {
		tCollegeService.updateById(tCollege);
		// 清除字典缓存，确保专业表中的学院名称实时刷新
		redisUtil.removeAll(CacheConstant.SYS_DICT_TABLE_CACHE);
		return Result.OK("编辑成功!");
	}

	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "学院表-通过id删除")
	@Operation(summary="学院表-通过id删除")
	@RequiresPermissions("zbu:t_college:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		tCollegeService.removeById(id);
		// 清除字典缓存
		redisUtil.removeAll(CacheConstant.SYS_DICT_TABLE_CACHE);
		return Result.OK("删除成功!");
	}

	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "学院表-批量删除")
	@Operation(summary="学院表-批量删除")
	@RequiresPermissions("zbu:t_college:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.tCollegeService.removeByIds(Arrays.asList(ids.split(",")));
		// 清除字典缓存
		redisUtil.removeAll(CacheConstant.SYS_DICT_TABLE_CACHE);
		return Result.OK("批量删除成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "学院表-通过id查询")
	@Operation(summary="学院表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<TCollege> queryById(@RequestParam(name="id",required=true) String id) {
		TCollege tCollege = tCollegeService.getById(id);
		if(tCollege==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(tCollege);
	}

	/**
	 * 导出excel
	 *
	 * @param request
	 * @param tCollege
	 */
	@RequiresPermissions("zbu:t_college:exportXls")
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request, TCollege tCollege) {
		return super.exportXls(request, tCollege, TCollege.class, "学院表");
	}

	/**
	 * 通过excel导入数据
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	@RequiresPermissions("zbu:t_college:importExcel")
	@RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
		try {
			// 1. 获取上传的Excel文件
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
			Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
			if (fileMap.isEmpty()) {
				return Result.error("请选择要导入的Excel文件！");
			}

			// 2. 基础导入参数配置
			ImportParams importParams = new ImportParams();
			importParams.setTitleRows(2); // 标题行数量
			importParams.setHeadRows(1); // 表头行数量
			importParams.setNeedSave(false);

			// 存储有效数据和失败信息
			List<TCollege> validCollegeList = new ArrayList<>();
			List<String> failMsgList = new ArrayList<>();
			int totalRow = 0;

			// 3. 遍历解析Excel文件
			for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
				MultipartFile file = entry.getValue();
				if (file.isEmpty()) {
					continue;
				}

				// 4. 解析Excel为学院列表
				List<TCollege> tempList = ExcelImportUtil.importExcel(
						file.getInputStream(),
						TCollege.class,
						importParams);

				// 5. 逐行校验+过滤
				for (int i = 0; i < tempList.size(); i++) {
					totalRow = i + 2; // Excel行号（标题+表头后从第2行开始）
					TCollege college = tempList.get(i);

					// 5.1 学院编码空值校验
					String collegeCode = college.getCollegeCode();
					if (oConvertUtils.isEmpty(collegeCode) || collegeCode.trim().isEmpty()) {
						failMsgList.add("第" + totalRow + "行：学院编码为空，跳过导入");
						continue;
					}
					college.setCollegeCode(collegeCode.trim());

					// 5.2 学院名称空值校验
					String collegeName = college.getCollegeName();
					if (oConvertUtils.isEmpty(collegeName) || collegeName.trim().isEmpty()) {
						failMsgList.add("第" + totalRow + "行：学院名称为空（编码：" + collegeCode + "），跳过导入");
						continue;
					}
					college.setCollegeName(collegeName.trim());

					// 6. 检查学院编码是否已存在
					QueryWrapper<TCollege> codeWrapper = new QueryWrapper<>();
					codeWrapper.eq("college_code", college.getCollegeCode());
					if (tCollegeService.count(codeWrapper) > 0) {
						failMsgList.add("第" + totalRow + "行：学院编码【" + collegeCode + "】已存在，跳过导入");
						continue;
					}

					// 7. 检查学院名称是否已存在
					QueryWrapper<TCollege> nameWrapper = new QueryWrapper<>();
					nameWrapper.eq("college_name", college.getCollegeName());
					if (tCollegeService.count(nameWrapper) > 0) {
						failMsgList.add("第" + totalRow + "行：学院名称【" + collegeName + "】已存在，跳过导入");
						continue;
					}

					// 8. 加入有效列表
					college.setCreateTime(new Date());
					validCollegeList.add(college);
				}
			}

			// 9. 批量保存有效数据
			if (!validCollegeList.isEmpty()) {
				tCollegeService.saveBatch(validCollegeList);
			}

			// 10. 构建返回结果
			String successMsg = "导入完成！成功导入【" + validCollegeList.size() + "】条有效数据";
			return ImportErrorExportUtil.buildErrorResult(failMsgList, successMsg, uploadPath, "学院表");

		} catch (Exception e) {
			log.error("Excel导入学院数据失败", e);
			return Result.error("导入失败：" + e.getMessage());
		}
	}

}
