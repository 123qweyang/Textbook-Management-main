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
import org.jeecg.modules.demo.zbu.entity.TMajor;
import org.jeecg.modules.demo.zbu.entity.TCollege;
import org.jeecg.modules.demo.zbu.service.ITMajorService;
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
 * @Description: 专业表
 * @Author: jeecg-boot
 * @Date: 2026-01-19
 * @Version: V1.0
 */
@Tag(name = "专业表")
@RestController
@RequestMapping("/zbu/tMajor")
@Slf4j
public class TMajorController extends JeecgController<TMajor, ITMajorService> {
	@Autowired
	private ITMajorService tMajorService;
	@Autowired
	private ITCollegeService tCollegeService;
	@Value("${jeecg.path.upload}")
	private String uploadPath;

	/**
	 * 分页列表查询
	 *
	 * @param tMajor
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary = "专业表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<TMajor>> queryPageList(TMajor tMajor,
											   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
											   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
											   HttpServletRequest req) {

		QueryWrapper<TMajor> queryWrapper = QueryGenerator.initQueryWrapper(tMajor, req.getParameterMap());
		Page<TMajor> page = new Page<TMajor>(pageNo, pageSize);
		IPage<TMajor> pageList = tMajorService.page(page, queryWrapper);
		return Result.OK(pageList);
	}

	/**
	 * 添加
	 *
	 * @param tMajor
	 * @return
	 */
	@AutoLog(value = "专业表-添加")
	@Operation(summary = "专业表-添加")
	@RequiresPermissions("zbu:t_major:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody TMajor tMajor) {
		QueryWrapper<TMajor> codeWrapper = new QueryWrapper<>();
		codeWrapper.eq("major_code", tMajor.getMajorCode());
		if (tMajorService.count(codeWrapper) > 0) {
			return Result.error("添加失败：专业编码已存在！");
		}

		QueryWrapper<TMajor> nameWrapper = new QueryWrapper<>();
		nameWrapper.eq("major_name", tMajor.getMajorName());
		if (tMajorService.count(nameWrapper) > 0) {
			return Result.error("添加失败：专业名称已存在！");
		}
		tMajorService.save(tMajor);

		return Result.OK("添加成功！");
	}

	@AutoLog(value = "专业表-编辑")
	@Operation(summary = "专业表-编辑")
	@RequiresPermissions("zbu:t_major:edit")
	@RequestMapping(value = "/edit", method = { RequestMethod.PUT, RequestMethod.POST })
	public Result<String> edit(@RequestBody TMajor tMajor) {
		tMajorService.updateById(tMajor);
		return Result.OK("编辑成功!");
	}

	@AutoLog(value = "专业表-通过id删除")
	@Operation(summary = "专业表-通过id删除")
	@RequiresPermissions("zbu:t_major:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
		tMajorService.removeById(id);
		return Result.OK("删除成功!");
	}

	@AutoLog(value = "专业表-批量删除")
	@Operation(summary = "专业表-批量删除")
	@RequiresPermissions("zbu:t_major:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
		this.tMajorService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 * 专业表批量删除（POST请求，支持大数据量）
	 *
	 * @param requestBody 请求体，格式为 {"ids":["id1","id2",...]} 或 "id1,id2,id3"
	 * @return
	 */
	@AutoLog(value = "专业表-批量删除")
	@Operation(summary = "专业表-批量删除")
	@RequiresPermissions("zbu:t_major:deleteBatch")
	@PostMapping(value = "/deleteBatch")
	public Result<String> deleteBatchPost(@RequestBody String requestBody) {
		if (oConvertUtils.isEmpty(requestBody)) {
			return Result.error("删除参数不能为空");
		}

		List<String> idList = new ArrayList<>();

		// 尝试解析 JSON 数组格式 {"ids":["id1","id2",...]}
		if (requestBody.contains("[")) {
			try {
				int startIdx = requestBody.indexOf("[");
				int endIdx = requestBody.indexOf("]");
				if (startIdx >= 0 && endIdx > startIdx) {
					String idsArray = requestBody.substring(startIdx + 1, endIdx);
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

		log.info("专业表批量删除，IDs数量：{}", idList.size());
		this.tMajorService.removeByIds(idList);
		return Result.OK("批量删除成功!");
	}

	@Operation(summary = "专业表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<TMajor> queryById(@RequestParam(name = "id", required = true) String id) {
		TMajor tMajor = tMajorService.getById(id);
		if (tMajor == null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(tMajor);
	}

	@RequiresPermissions("zbu:t_major:exportXls")
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request, TMajor tMajor) {
		return super.exportXls(request, tMajor, TMajor.class, "专业表");
	}

	@RequiresPermissions("zbu:t_major:importExcel")
	@RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
		try {
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
			Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
			if (fileMap.isEmpty()) {
				return Result.error("请选择要导入的Excel文件！");
			}

			ImportParams importParams = new ImportParams();
			importParams.setTitleRows(2);
			importParams.setHeadRows(1);
			importParams.setNeedSave(false);

			List<TMajor> validMajorList = new ArrayList<>();
			List<String> failMsgList = new ArrayList<>();
			int totalRow = 0;

			for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
				MultipartFile file = entry.getValue();
				if (file.isEmpty()) {
					continue;
				}

				List<TMajor> tempList = ExcelImportUtil.importExcel(
						file.getInputStream(),
						TMajor.class,
						importParams);

				for (int i = 0; i < tempList.size(); i++) {
					totalRow = i + 2;
					TMajor major = tempList.get(i);

					String majorCode = major.getMajorCode();
					if (oConvertUtils.isEmpty(majorCode) || majorCode.trim().isEmpty()) {
						failMsgList.add("第" + totalRow + "行：专业编码为空，跳过导入");
						continue;
					}
					major.setMajorCode(majorCode.trim());

					String majorName = major.getMajorName();
					if (oConvertUtils.isEmpty(majorName) || majorName.trim().isEmpty()) {
						failMsgList.add("第" + totalRow + "行：专业名称为空（编码：" + majorCode + "），跳过导入");
						continue;
					}
					major.setMajorName(majorName.trim());

					if (oConvertUtils.isEmpty(major.getCollegeId())) {
						failMsgList.add("第" + totalRow + "行：所属学院为空（编码：" + majorCode + "），跳过导入");
						continue;
					}

					TCollege college = tCollegeService.getById(major.getCollegeId());
					if (college == null) {
						failMsgList.add("第" + totalRow + "行：所属学院ID【" + major.getCollegeId() + "】不存在（编码：" + majorCode + "），跳过导入");
						continue;
					}

					QueryWrapper<TMajor> codeWrapper = new QueryWrapper<>();
					codeWrapper.eq("major_code", major.getMajorCode());
					if (tMajorService.count(codeWrapper) > 0) {
						failMsgList.add("第" + totalRow + "行：专业编码【" + majorCode + "】已存在，跳过导入");
						continue;
					}

					QueryWrapper<TMajor> nameWrapper = new QueryWrapper<>();
					nameWrapper.eq("major_name", major.getMajorName());
					if (tMajorService.count(nameWrapper) > 0) {
						failMsgList.add("第" + totalRow + "行：专业名称【" + majorName + "】已存在，跳过导入");
						continue;
					}

					major.setCreateTime(new Date());
					validMajorList.add(major);
				}
			}

			if (!validMajorList.isEmpty()) {
				tMajorService.saveBatch(validMajorList);
			}

			String successMsg = "导入完成！成功导入【" + validMajorList.size() + "】条有效数据";
			return ImportErrorExportUtil.buildErrorResult(failMsgList, successMsg, uploadPath, "专业表");

		} catch (Exception e) {
			log.error("Excel导入专业数据失败", e);
			return Result.error("导入失败：" + e.getMessage());
		}

	}

}