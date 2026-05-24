package org.jeecg.modules.demo.zbu.controller;

import java.util.*;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.query.QueryRuleEnum;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.demo.zbu.entity.TClass;
import org.jeecg.modules.demo.zbu.entity.TMajor;
import org.jeecg.modules.demo.zbu.entity.TCounselor;
import org.jeecg.modules.demo.zbu.service.ITClassService;
import org.jeecg.modules.demo.zbu.service.ITMajorService;
import org.jeecg.modules.demo.zbu.service.ITCounselorService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.enmus.ExcelType;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.modules.demo.zbu.vo.ImportErrorExportUtil;
import org.springframework.jdbc.core.JdbcTemplate;
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
 * @Description: 班级表
 * @Author: jeecg-boot
 * @Date: 2026-01-26
 * @Version: V1.0
 */
@Tag(name = "班级表")
@RestController
@RequestMapping("/zbu/tClass")
@Slf4j
public class TClassController extends JeecgController<TClass, ITClassService> {
	@Autowired
	private ITClassService tClassService;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private ITMajorService tMajorService;
	@Autowired
	private ITCounselorService tCounselorService;
	@Value("${jeecg.path.upload}")
	private String uploadPath;

	/**
	 * 分页列表查询
	 *
	 * @param tClass
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	// @AutoLog(value = "班级表-分页列表查询")
	@Operation(summary = "班级表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<TClass>> queryPageList(TClass tClass,
											   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
											   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
											   HttpServletRequest req) {

		// 处理搜索参数
		String counselorName = req.getParameter("counselorName");
		String counselorNo = req.getParameter("counselorNo");
		Map<String, String[]> paramMap = new HashMap<>(req.getParameterMap());
		if (counselorName != null && !counselorName.isEmpty()) {
			paramMap.remove("counselorName");
		}
		if (counselorNo != null && !counselorNo.isEmpty()) {
			paramMap.remove("counselorNo");
		}

		QueryWrapper<TClass> queryWrapper = QueryGenerator.initQueryWrapper(tClass, paramMap);

		// 按辅导员名称搜索
		if (counselorName != null && !counselorName.isEmpty()) {
			queryWrapper.inSql("counselor_id",
					"SELECT id FROM t_counselor WHERE counselor_name LIKE '%" + counselorName + "%'");
		}

		// 按辅导员工号搜索
		if (counselorNo != null && !counselorNo.isEmpty()) {
			queryWrapper.inSql("counselor_id",
					"SELECT id FROM t_counselor WHERE counselor_id LIKE '%" + counselorNo + "%'");
		}

		Page<TClass> page = new Page<TClass>(pageNo, pageSize);
		IPage<TClass> pageList = tClassService.page(page, queryWrapper);

		// 填充辅导员姓名和工号
		for (TClass record : pageList.getRecords()) {
			if (record.getCounselorId() != null && !record.getCounselorId().isEmpty()) {
				try {
					String sql = "SELECT counselor_name, counselor_id FROM t_counselor WHERE id = ? LIMIT 1";
					Map<String, Object> info = jdbcTemplate.queryForMap(sql, record.getCounselorId());
					record.setCounselorName((String) info.get("counselor_name"));
					record.setCounselorNo((String) info.get("counselor_id"));
				} catch (Exception e) {
					log.warn("查询辅导员信息失败：{}", e.getMessage());
				}
			}
		}

		return Result.OK(pageList);
	}

	/**
	 * 添加
	 *
	 * @param tClass
	 * @return
	 */
	@AutoLog(value = "班级表-添加")
	@Operation(summary = "班级表-添加")
	@RequiresPermissions("zbu:t_class:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody TClass tClass) {

		// 检查班级编码是否已存在
		QueryWrapper<TClass> codeWrapper = new QueryWrapper<>();
		codeWrapper.eq("class_code", tClass.getClassCode());
		if (tClassService.count(codeWrapper) > 0) {
			return Result.error("添加失败：班级编码已存在！");
		}

		// 检查班级名称是否已存在
		QueryWrapper<TClass> nameWrapper = new QueryWrapper<>();
		nameWrapper.eq("class_name", tClass.getClassName());
		if (tClassService.count(nameWrapper) > 0) {
			return Result.error("添加失败：班级名称已存在！");
		}

		tClassService.save(tClass);

		return Result.OK("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param tClass
	 * @return
	 */
	@AutoLog(value = "班级表-编辑")
	@Operation(summary = "班级表-编辑")
	@RequiresPermissions("zbu:t_class:edit")
	@RequestMapping(value = "/edit", method = { RequestMethod.PUT, RequestMethod.POST })
	public Result<String> edit(@RequestBody TClass tClass) {
		tClassService.updateById(tClass);
		return Result.OK("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "班级表-通过id删除")
	@Operation(summary = "班级表-通过id删除")
	@RequiresPermissions("zbu:t_class:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
		tClassService.removeById(id);
		return Result.OK("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "班级表-批量删除")
	@Operation(summary = "班级表-批量删除")
	@RequiresPermissions("zbu:t_class:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
		this.tClassService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 * 班级表批量删除（POST请求，支持大数据量）
	 *
	 * @param requestBody 请求体，格式为 {"ids":["id1","id2",...]} 或 "id1,id2,id3"
	 * @return
	 */
	@AutoLog(value = "班级表-批量删除")
	@Operation(summary = "班级表-批量删除")
	@RequiresPermissions("zbu:t_class:deleteBatch")
	@PostMapping(value = "/deleteBatch")
	public Result<String> deleteBatchPost(@RequestBody String requestBody) {
		if (oConvertUtils.isEmpty(requestBody)) {
			return Result.error("删除参数不能为空");
		}

		List<String> idList = new ArrayList<>();

		// 尝试解析 JSON 数组格式 {"ids":["id1","id2",...]}
		if (requestBody.contains("[")) {
			try {
				// 提取 ids 数组
				int startIdx = requestBody.indexOf("[");
				int endIdx = requestBody.indexOf("]");
				if (startIdx >= 0 && endIdx > startIdx) {
					String idsArray = requestBody.substring(startIdx + 1, endIdx);
					// 去掉引号和空格，分割成列表
					String[] ids = idsArray.replace("\"", "").replace(" ", "").split(",");
					for (String id : ids) {
						if (oConvertUtils.isNotEmpty(id)) {
							idList.add(id);
						}
					}
				}
			} catch (Exception e) {
				log.warn("解析JSON格式失败，尝试按逗号分割：{}", requestBody);
			}
		}

		// 如果不是 JSON 格式，按逗号分割
		if (idList.isEmpty()) {
			String[] ids = requestBody.replace("\"", "").replace(" ", "").split(",");
			for (String id : ids) {
				if (oConvertUtils.isNotEmpty(id)) {
					idList.add(id);
				}
			}
		}

		if (idList.isEmpty()) {
			return Result.error("删除参数不能为空");
		}

		log.info("班级表批量删除，IDs数量：{}", idList.size());
		this.tClassService.removeByIds(idList);
		return Result.OK("批量删除成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	// @AutoLog(value = "班级表-通过id查询")
	@Operation(summary = "班级表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<TClass> queryById(@RequestParam(name = "id", required = true) String id) {
		TClass tClass = tClassService.getById(id);
		if (tClass == null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(tClass);
	}

	/**
	 * 导出excel
	 *
	 * @param request
	 * @param tClass
	 */
	/**
	 * 通过Excel更新班级辅导员信息（按班级编码匹配）
	 */
	@RequiresPermissions("zbu:t_class:importExcel")
	@RequestMapping(value = "/updateByExcel", method = RequestMethod.POST)
	public Result<?> updateByExcel(HttpServletRequest request) {
		try {
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
			Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
			if (fileMap.isEmpty()) {
				return Result.error("请选择要上传的Excel文件！");
			}

			int updateCount = 0;
			List<String> failMsgList = new ArrayList<>();

			for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
				MultipartFile file = entity.getValue();
				if (file.isEmpty()) continue;

				ImportParams params = new ImportParams();
				params.setTitleRows(2);
				params.setHeadRows(1);
				params.setNeedSave(false);

				List<TClass> list = ExcelImportUtil.importExcel(file.getInputStream(), TClass.class, params);

				for (int i = 0; i < list.size(); i++) {
					TClass tb = list.get(i);
					int rowNum = i + 3;

					// 班级编码必填
					if (oConvertUtils.isEmpty(tb.getClassCode())) {
						failMsgList.add("第" + rowNum + "行：班级编码为空，跳过更新");
						continue;
					}

					// 按班级编码查找
					QueryWrapper<TClass> queryWrapper = new QueryWrapper<>();
					queryWrapper.eq("class_code", tb.getClassCode());
					TClass exist = tClassService.getOne(queryWrapper);
					if (exist == null) {
						failMsgList.add("第" + rowNum + "行：班级编码【" + tb.getClassCode() + "】不存在，无法更新");
						continue;
					}

					String counselorNoInput = tb.getCounselorNo();
					String counselorNameInput = tb.getCounselorName();

					// 辅导员工号和姓名均为空则跳过
					if (oConvertUtils.isEmpty(counselorNoInput) && oConvertUtils.isEmpty(counselorNameInput)) {
						failMsgList.add("第" + rowNum + "行：班级编码【" + tb.getClassCode() + "】未提供辅导员信息，跳过更新");
						continue;
					}

					if (oConvertUtils.isEmpty(counselorNoInput)) {
						failMsgList.add("第" + rowNum + "行：辅导员工号为空（编码：" + tb.getClassCode() + "），跳过更新");
						continue;
					}
					if (oConvertUtils.isEmpty(counselorNameInput)) {
						failMsgList.add("第" + rowNum + "行：辅导员姓名为空（编码：" + tb.getClassCode() + "），跳过更新");
						continue;
					}

					// 按工号查找辅导员
					QueryWrapper<TCounselor> counselorWrapper = new QueryWrapper<>();
					counselorWrapper.eq("counselor_id", counselorNoInput);
					TCounselor counselor = tCounselorService.getOne(counselorWrapper);
					if (counselor == null) {
						failMsgList.add("第" + rowNum + "行：辅导员工号【" + counselorNoInput + "】不存在（编码：" + tb.getClassCode() + "），跳过更新");
						continue;
					}

					// 验证姓名匹配
					if (!counselor.getCounselorName().equals(counselorNameInput)) {
						failMsgList.add("第" + rowNum + "行：辅导员姓名【" + counselorNameInput + "】与工号【" + counselorNoInput + "】不匹配（实际姓名：" + counselor.getCounselorName() + "）（编码：" + tb.getClassCode() + "），跳过更新");
						continue;
					}

					// 更新辅导员信息
					exist.setCounselorId(counselor.getId());
					tClassService.updateById(exist);
					updateCount++;
				}
			}

			String successMsg = "更新完成！成功更新【" + updateCount + "】条记录";
			return ImportErrorExportUtil.buildErrorResult(failMsgList, successMsg, uploadPath, "班级表");

		} catch (Exception e) {
			log.error("通过Excel更新班级失败", e);
			return Result.error("更新失败：" + e.getMessage());
		}
	}

	@RequiresPermissions("zbu:t_class:exportXls")
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request, TClass tClass) {
		// 先用父类方法生成查询条件，获取数据
		QueryWrapper<TClass> queryWrapper = QueryGenerator.initQueryWrapper(tClass, request.getParameterMap());
		List<TClass> exportList = tClassService.list(queryWrapper);

		// 填充辅导员姓名和工号
		for (TClass record : exportList) {
			if (record.getCounselorId() != null && !record.getCounselorId().isEmpty()) {
				try {
					String sql = "SELECT counselor_name, counselor_id FROM t_counselor WHERE id = ? LIMIT 1";
					Map<String, Object> counselorInfo = jdbcTemplate.queryForMap(sql, record.getCounselorId());
					record.setCounselorName((String) counselorInfo.get("counselor_name"));
					record.setCounselorNo((String) counselorInfo.get("counselor_id"));
				} catch (Exception e) {
					log.warn("查询辅导员信息失败：{}", e.getMessage());
				}
			}
		}

		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
		mv.addObject(NormalExcelConstants.FILE_NAME, "班级表");
		mv.addObject(NormalExcelConstants.CLASS, TClass.class);
		mv.addObject(NormalExcelConstants.PARAMS,
				new ExportParams("班级表报表", "导出人:" + sysUser.getRealname(), "班级表", ExcelType.XSSF));
		mv.addObject(NormalExcelConstants.DATA_LIST, exportList);
		return mv;
	}

	/**
	 * 通过excel导入数据
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	@RequiresPermissions("zbu:t_class:importExcel")
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
			List<TClass> validClassList = new ArrayList<>();
			List<String> failMsgList = new ArrayList<>();
			int totalRow = 0;

			// 用于检测Excel内部的重复（按编码和名称）
			java.util.Set<String> classCodesInFile = new java.util.HashSet<>();
			java.util.Set<String> classNamesInFile = new java.util.HashSet<>();

			// 3. 只处理第一个上传的文件（避免重复处理）
			MultipartFile file = fileMap.values().iterator().next();
			if (!file.isEmpty()) {
				// 4. 解析Excel为班级列表
				List<TClass> tempList = ExcelImportUtil.importExcel(
						file.getInputStream(),
						TClass.class,
						importParams);

				log.info("班级导入：Excel解析到 {} 条数据", tempList.size());

				// 打印导入前数据库中的班级总数
				long totalCountBefore = tClassService.count();
				log.info("班级导入：导入前数据库中有 {} 条记录", totalCountBefore);

				// 5. 逐行校验+过滤
				for (int i = 0; i < tempList.size(); i++) {
					totalRow = i + 2; // Excel行号（标题+表头后从第2行开始）
					TClass clazz = tempList.get(i);
					log.debug("第{}行：班级编码={}，班级名称={}", totalRow, clazz.getClassCode(), clazz.getClassName());

					// 5.1 班级编码空值校验
					String classCode = clazz.getClassCode();
					if (oConvertUtils.isEmpty(classCode) || classCode.trim().isEmpty()) {
						failMsgList.add("第" + totalRow + "行：班级编码为空，跳过导入");
						continue;
					}
					clazz.setClassCode(classCode.trim());

					// 5.2 班级名称空值校验
					String className = clazz.getClassName();
					if (oConvertUtils.isEmpty(className) || className.trim().isEmpty()) {
						failMsgList.add("第" + totalRow + "行：班级名称为空（编码：" + classCode + "），跳过导入");
						continue;
					}
					clazz.setClassName(className.trim());

					// 5.3 所属专业空值校验
					if (oConvertUtils.isEmpty(clazz.getMajorId())) {
						failMsgList.add("第" + totalRow + "行：所属专业为空（编码：" + classCode + "），跳过导入");
						continue;
					}

					// 5.4 验证所属专业是否真实存在
					TMajor major = tMajorService.getById(clazz.getMajorId());
					if (major == null) {
						failMsgList.add(
								"第" + totalRow + "行：所属专业ID【" + clazz.getMajorId() + "】不存在（编码：" + classCode + "），跳过导入");
						continue;
					}

					// 5.5 辅导员工号/姓名校验
					String counselorNoInput = clazz.getCounselorNo();
					String counselorNameInput = clazz.getCounselorName(); // Excel中"辅导员"列存的是姓名

					if (oConvertUtils.isEmpty(counselorNoInput)) {
						failMsgList.add("第" + totalRow + "行：辅导员工号为空（编码：" + classCode + "），跳过导入");
						continue;
					}
					if (oConvertUtils.isEmpty(counselorNameInput)) {
						failMsgList.add("第" + totalRow + "行：辅导员姓名为空（编码：" + classCode + "），跳过导入");
						continue;
					}

					// 5.6 按辅导员工号查询辅导员
					QueryWrapper<TCounselor> counselorWrapper = new QueryWrapper<>();
					counselorWrapper.eq("counselor_id", counselorNoInput);
					TCounselor counselor = tCounselorService.getOne(counselorWrapper);
					if (counselor == null) {
						failMsgList.add("第" + totalRow + "行：辅导员工号【" + counselorNoInput + "】不存在（编码：" + classCode
								+ "），跳过导入");
						continue;
					}

					// 5.7 验证辅导员姓名与工号是否匹配
					if (!counselor.getCounselorName().equals(counselorNameInput)) {
						failMsgList.add("第" + totalRow + "行：辅导员姓名【" + counselorNameInput + "】与工号【"
								+ counselorNoInput + "】不匹配（实际姓名：" + counselor.getCounselorName()
								+ "）（编码：" + classCode + "），跳过导入");
						continue;
					}

					// 将辅导员主键ID存入班级表
					clazz.setCounselorId(counselor.getId());

					// 5.7 检查Excel内部是否有重复的班级编码
					if (classCodesInFile.contains(classCode)) {
						failMsgList.add("第" + totalRow + "行：班级编码【" + classCode + "】在Excel中重复，跳过导入");
						continue;
					}
					classCodesInFile.add(classCode);

					// 5.8 检查Excel内部是否有重复的班级名称
					if (classNamesInFile.contains(className)) {
						failMsgList.add("第" + totalRow + "行：班级名称【" + className + "】在Excel中重复，跳过导入");
						continue;
					}
					classNamesInFile.add(className);

					// 6. 检查班级编码是否已存在（数据库中）
					QueryWrapper<TClass> codeWrapper = new QueryWrapper<>();
					codeWrapper.eq("class_code", clazz.getClassCode());
					if (tClassService.count(codeWrapper) > 0) {
						failMsgList.add("第" + totalRow + "行：班级编码【" + classCode + "】已存在，跳过导入");
						continue;
					}

					// 7. 检查班级名称是否已存在（数据库中）
					QueryWrapper<TClass> nameWrapper = new QueryWrapper<>();
					nameWrapper.eq("class_name", clazz.getClassName());
					if (tClassService.count(nameWrapper) > 0) {
						failMsgList.add("第" + totalRow + "行：班级名称【" + className + "】已存在，跳过导入");
						continue;
					}

					// 8. 加入有效列表
					clazz.setCreateTime(new Date());
					validClassList.add(clazz);
				}
			}

			// 9. 批量保存有效数据
			if (!validClassList.isEmpty()) {
				tClassService.saveBatch(validClassList);

				// 打印导入后的总数
				long totalCountAfter = tClassService.count();
				log.info("班级导入：导入后数据库中有 {} 条记录（新增 {} 条）", totalCountAfter, validClassList.size());
			}

			// 10. 构建返回结果
			String successMsg = "导入完成！成功导入【" + validClassList.size() + "】条有效数据";
			return ImportErrorExportUtil.buildErrorResult(failMsgList, successMsg, uploadPath, "班级表");

		} catch (Exception e) {
			log.error("Excel导入班级数据失败", e);
			List<String> failMsgList = new ArrayList<>(); failMsgList.add("导入异常：" + e.getMessage()); return ImportErrorExportUtil.buildErrorResult(failMsgList, "导入完成！成功导入0条有效数据", uploadPath, "班级表");
		}
	}

}
