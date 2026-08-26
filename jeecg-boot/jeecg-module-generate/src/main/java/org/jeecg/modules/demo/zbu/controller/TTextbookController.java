package org.jeecg.modules.demo.zbu.controller;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.query.QueryRuleEnum;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.demo.zbu.entity.StudentBill;
import org.jeecg.modules.demo.zbu.entity.TTextbook;
import org.jeecg.modules.demo.zbu.service.IStudentBillService;
import org.jeecg.modules.demo.zbu.service.ITTextbookService;

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
import org.jeecg.modules.demo.zbu.util.SemesterUtil;
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
 * @Description: 教材表
 * @Author: jeecg-boot
 * @Date: 2026-01-19
 * @Version: V1.0
 */
@Tag(name = "教材表")
@RestController
@RequestMapping("/zbu/tTextbook")
@Slf4j
public class TTextbookController extends JeecgController<TTextbook, ITTextbookService> {
	@Autowired
	private ITTextbookService tTextbookService;
	@Autowired
	private IStudentBillService studentBillService;
	@Value("${jeecg.path.upload}")
	private String uploadPath;

	/**
	 * 分页列表查询
	 *
	 * @param tTextbook
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	// @AutoLog(value = "教材表-分页列表查询")
	@Operation(summary = "教材表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<TTextbook>> queryPageList(TTextbook tTextbook,
												  @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
												  @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
												  HttpServletRequest req) {

		QueryWrapper<TTextbook> queryWrapper = new QueryWrapper<>();

		// 启用学年筛选（可选）：有值则过滤，无值则查全部
		String enableYear = req.getParameter("enableYear");
		if (oConvertUtils.isNotEmpty(enableYear)) {
			queryWrapper.eq("enable_year", enableYear);
		}
		// 启用学期筛选（可选）
		String enableSemester = req.getParameter("enableSemester");
		if (oConvertUtils.isNotEmpty(enableSemester)) {
			queryWrapper.eq("enable_semester", enableSemester);
		}

		// 其他可选条件
		if (oConvertUtils.isNotEmpty(tTextbook.getSectionCode())) {
			queryWrapper.like("section_code", tTextbook.getSectionCode());
		}
		if (oConvertUtils.isNotEmpty(tTextbook.getBusinessCode())) {
			queryWrapper.like("business_code", tTextbook.getBusinessCode());
		}
		if (oConvertUtils.isNotEmpty(tTextbook.getIsbn())) {
			queryWrapper.like("isbn", tTextbook.getIsbn());
		}
		if (oConvertUtils.isNotEmpty(tTextbook.getTextbookName())) {
			queryWrapper.like("textbook_name", tTextbook.getTextbookName());
		}

	// 动态排序：支持前端点击列头排序（前端传驼峰，转为下划线）
		String column = req.getParameter("column");
		String order = req.getParameter("order");
		if (oConvertUtils.isNotEmpty(column) && oConvertUtils.isNotEmpty(order)) {
				// 去掉 _dictText 后缀（前端字典列 dataIndex 以 _dictText 结尾，如 status_dictText → status）
				if (column.endsWith("_dictText")) {
					column = column.substring(0, column.length() - "_dictText".length());
				}
			Set<String> allowedColumns = new HashSet<>(Arrays.asList(
					"sectionCode", "businessCode", "isbn", "textbookName",
					"enableYear", "enableSemester", "price", "discount",
					"publisher", "author", "status", "createTime", "updateTime"));
			if (allowedColumns.contains(column)) {
				String dbColumn = column.replaceAll("([A-Z])", "_$1").toLowerCase();
				if ("asc".equalsIgnoreCase(order)) {
					queryWrapper.orderByAsc(dbColumn);
				} else {
					queryWrapper.orderByDesc(dbColumn);
				}
			} else {
				queryWrapper.orderByDesc("create_time");
			}
		} else {
			queryWrapper.orderByDesc("create_time");
		}
		log.info("教材表查询 - 启用学年: {}", enableYear);

		Page<TTextbook> page = new Page<TTextbook>(pageNo, pageSize);
		IPage<TTextbook> pageList = tTextbookService.page(page, queryWrapper);
		return Result.OK(pageList);
	}

	/**
	 * 全选：获取当前筛选条件下所有教材ID
	 */
	@Operation(summary = "教材表-获取全部ID")
	@GetMapping(value = "/getAllIds")
	public Result<List<String>> getAllIds(TTextbook tTextbook, HttpServletRequest req) {
		QueryWrapper<TTextbook> queryWrapper = new QueryWrapper<>();

		// 启用学年筛选（可选）：有值则过滤，无值则导出全部
		String enableYear = req.getParameter("enableYear");
		if (oConvertUtils.isNotEmpty(enableYear)) {
			queryWrapper.eq("enable_year", enableYear);
		}

		if (oConvertUtils.isNotEmpty(tTextbook.getIsbn())) {
			queryWrapper.like("isbn", tTextbook.getIsbn());
		}
		if (oConvertUtils.isNotEmpty(tTextbook.getTextbookName())) {
			queryWrapper.like("textbook_name", tTextbook.getTextbookName());
		}

		List<TTextbook> list = tTextbookService.list(queryWrapper);
		List<String> ids = list.stream().map(TTextbook::getId).collect(Collectors.toList());
		return Result.OK(ids);
	}

	/**
	 * 添加
	 *
	 * @param tTextbook
	 * @return
	 */
	@AutoLog(value = "教材表-添加")
	@Operation(summary = "教材表-添加")
	@RequiresPermissions("zbu:t_textbook:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody TTextbook tTextbook) {
		tTextbookService.save(tTextbook);

		return Result.OK("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param tTextbook
	 * @return
	 */
	@AutoLog(value = "教材表-编辑")
	@Operation(summary = "教材表-编辑")
	@RequiresPermissions("zbu:t_textbook:edit")
	@RequestMapping(value = "/edit", method = { RequestMethod.PUT, RequestMethod.POST })
	public Result<String> edit(@RequestBody TTextbook tTextbook) {
		TTextbook oldTextbook = tTextbookService.getById(tTextbook.getId());
		boolean priceChanged = false;
		boolean discountChanged = false;
		if (oldTextbook != null) {
			if (tTextbook.getPrice() != null && !tTextbook.getPrice().equals(oldTextbook.getPrice())) {
				priceChanged = true;
			}
			if (tTextbook.getDiscount() != null && !tTextbook.getDiscount().equals(oldTextbook.getDiscount())) {
				discountChanged = true;
			}
		}
		tTextbookService.updateById(tTextbook);

		if (priceChanged || discountChanged) {
			syncBillPriceByTextbook(tTextbook.getId(), tTextbook);
		}
		return Result.OK("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "教材表-通过id删除")
	@Operation(summary = "教材表-通过id删除")
	@RequiresPermissions("zbu:t_textbook:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
		tTextbookService.removeById(id);
		return Result.OK("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "教材表-批量删除")
	@Operation(summary = "教材表-批量删除")
	@RequiresPermissions("zbu:t_textbook:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
		this.tTextbookService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 * 教材表批量删除（POST请求，支持大数据量）
	 *
	 * @param requestBody 请求体，格式为 {"ids":["id1","id2",...]} 或 "id1,id2,id3"
	 * @return
	 */
	@AutoLog(value = "教材表-批量删除")
	@Operation(summary = "教材表-批量删除")
	@RequiresPermissions("zbu:t_textbook:deleteBatch")
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

		log.info("教材表批量删除，IDs数量：{}", idList.size());
		this.tTextbookService.removeByIds(idList);
		return Result.OK("批量删除成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	// @AutoLog(value = "教材表-通过id查询")
	@Operation(summary = "教材表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<TTextbook> queryById(@RequestParam(name = "id", required = true) String id) {
		TTextbook tTextbook = tTextbookService.getById(id);
		if (tTextbook == null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(tTextbook);
	}

	/**
	 * 导出excel
	 *
	 * @param request
	 * @param tTextbook
	 */
	@RequiresPermissions("zbu:t_textbook:exportXls")
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request, TTextbook tTextbook) {
		return super.exportXls(request, tTextbook, TTextbook.class, "教材表");
	}

	/**
	 * 通过excel导入数据
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	@RequiresPermissions("zbu:t_textbook:importExcel")
	@RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		MultipartFile file = multipartRequest.getFile("file");
		if (file == null || file.isEmpty()) {
			return Result.error("导入失败：文件不能为空");
		}

		try {
			ImportParams importParams = new ImportParams();
			importParams.setTitleRows(2);
			importParams.setHeadRows(1);
			importParams.setNeedSave(false);

			List<TTextbook> textbookList = ExcelImportUtil.importExcel(file.getInputStream(), TTextbook.class,
					importParams);

			if (textbookList == null || textbookList.isEmpty()) {
				return Result.error("导入失败：Excel内容为空");
			}

			List<TTextbook> toSaveList = new ArrayList<>();
			List<String> failMsgList = new ArrayList<>();

			for (int i = 0; i < textbookList.size(); i++) {
				TTextbook textbook = textbookList.get(i);
				int rowNum = i + 3;
				if (oConvertUtils.isEmpty(textbook.getIsbn())) {
					failMsgList.add("第" + rowNum + "行：ISBN为空，跳过导入");
					continue;
				}
				// 清洗ISBN：去掉空格和横线
				textbook.setIsbn(textbook.getIsbn().replaceAll("[\\s-]", ""));
				if (oConvertUtils.isEmpty(textbook.getIsbn())) {
					failMsgList.add("第" + rowNum + "行：ISBN清洗后为空，跳过导入");
					continue;
				}
				if (oConvertUtils.isEmpty(textbook.getEnableYear())) {
					failMsgList.add("第" + rowNum + "行：启用学年为空（ISBN：" + textbook.getIsbn() + "），跳过导入");
					continue;
				}
				if (oConvertUtils.isEmpty(textbook.getEnableSemester())) {
				failMsgList.add("第" + rowNum + "行：启用学期为空（ISBN：" + textbook.getIsbn() + "），跳过导入");
				continue;
			}
			// 统一学期格式为字典码（兼容导入Excel中的"1/一/第一学期"等写法）
			textbook.setEnableSemester(SemesterUtil.normalizeCode(textbook.getEnableSemester()));

			QueryWrapper<TTextbook> queryWrapper = new QueryWrapper<>();
				queryWrapper.eq("isbn", textbook.getIsbn());
				queryWrapper.eq("enable_year", textbook.getEnableYear());
				queryWrapper.eq("enable_semester", textbook.getEnableSemester());
				TTextbook existTextbook = tTextbookService.getOne(queryWrapper);

				if (existTextbook != null) {
					failMsgList.add("第" + rowNum + "行：ISBN【" + textbook.getIsbn() + "】已存在（学年："
							+ textbook.getEnableYear() + "，学期：" + textbook.getEnableSemester() + "），跳过导入");
					continue;
				}

				toSaveList.add(textbook);
			}

			if (!toSaveList.isEmpty()) {
				tTextbookService.saveBatch(toSaveList);
			}

			String successMsg = "导入完成！成功导入【" + toSaveList.size() + "】条有效数据";
			log.info("【教材导入】" + successMsg);
			return ImportErrorExportUtil.buildErrorResult(failMsgList, successMsg, uploadPath, "教材表");

		} catch (Exception e) {
			log.error("【教材导入】失败", e);
			List<String> failMsgList = new ArrayList<>();
			failMsgList.add("导入异常：" + e.getMessage());
			return ImportErrorExportUtil.buildErrorResult(failMsgList, "导入完成！成功导入0条有效数据", uploadPath, "教材表");
		}
	}

	/**
	 * 通过Excel更新教材信息（按ISBN+启用学年+启用学期匹配）
	 */
	@RequiresPermissions("zbu:t_textbook:importExcel")
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

				List<TTextbook> list = ExcelImportUtil.importExcel(file.getInputStream(), TTextbook.class, params);

				for (int i = 0; i < list.size(); i++) {
					TTextbook tb = list.get(i);
					int rowNum = i + 3;

					// 清洗ISBN
					if (oConvertUtils.isNotEmpty(tb.getIsbn())) {
						tb.setIsbn(tb.getIsbn().replaceAll("[\\s-]", ""));
					}

					// 必填校验
					if (oConvertUtils.isEmpty(tb.getIsbn())) {
						failMsgList.add("第" + rowNum + "行：ISBN为空，跳过更新");
						continue;
					}
					if (oConvertUtils.isEmpty(tb.getEnableYear())) {
						failMsgList.add("第" + rowNum + "行：启用学年为空（ISBN：" + tb.getIsbn() + "），跳过更新");
						continue;
					}
					if (oConvertUtils.isEmpty(tb.getEnableSemester())) {
					failMsgList.add("第" + rowNum + "行：启用学期为空（ISBN：" + tb.getIsbn() + "），跳过更新");
					continue;
				}
				// 统一学期格式为字典码（兼容导入Excel中的"1/一/第一学期"等写法）
				tb.setEnableSemester(SemesterUtil.normalizeCode(tb.getEnableSemester()));

				// 按ISBN+启用学年+启用学期查找已有记录
					QueryWrapper<TTextbook> queryWrapper = new QueryWrapper<>();
					queryWrapper.eq("isbn", tb.getIsbn());
					queryWrapper.eq("enable_year", tb.getEnableYear());
					queryWrapper.eq("enable_semester", tb.getEnableSemester());
					TTextbook exist = tTextbookService.getOne(queryWrapper);

					if (exist == null) {
						failMsgList.add("第" + rowNum + "行：ISBN【" + tb.getIsbn() + "】（学年：" + tb.getEnableYear()
								+ "，学期：" + tb.getEnableSemester() + "）不存在，无法更新");
						continue;
					}

					// 更新标段、编号、折扣
					boolean updated = false;
					if (oConvertUtils.isNotEmpty(tb.getSectionCode())) {
						exist.setSectionCode(tb.getSectionCode());
						updated = true;
					}
					if (oConvertUtils.isNotEmpty(tb.getBusinessCode())) {
						exist.setBusinessCode(tb.getBusinessCode());
						updated = true;
					}
					if (tb.getDiscount() != null) {
						exist.setDiscount(tb.getDiscount());
						updated = true;
					}

					if (updated) {
						tTextbookService.updateById(exist);
						updateCount++;
					} else {
						failMsgList.add("第" + rowNum + "行：ISBN【" + tb.getIsbn() + "】未提供任何要更新的字段，跳过");
					}
				}
			}

			String successMsg = "更新完成！成功更新【" + updateCount + "】条记录";
			return ImportErrorExportUtil.buildErrorResult(failMsgList, successMsg, uploadPath, "教材表");

		} catch (Exception e) {
			log.error("通过Excel更新教材失败", e);
			return Result.error("更新失败：" + e.getMessage());
		}
	}

	/**
	 * 批量修改
	 *
	 * @param params 包含ids列表和要修改的字段
	 * @return
	 */
	@AutoLog(value = "教材表-批量修改")
	@Operation(summary = "教材表-批量修改")
	@PostMapping(value = "/editBatch")
	public Result<String> editBatch(@RequestBody Map<String, Object> params) {
		try {
			// 1. 基础参数校验：ID列表
			String idsStr = (String) params.get("ids");
			if (oConvertUtils.isEmpty(idsStr)) {
				return Result.error("批量修改失败：未选择要修改的记录ID");
			}
			List<String> ids = Arrays.asList(idsStr.split(","));
			if (ids.isEmpty()) {
				return Result.error("批量修改失败：ID列表不能为空");
			}

			// 2. 定义【仅允许批量修改的字段白名单】（驼峰名，对应实体类）
			List<String> ALLOW_UPDATE_FIELDS = Arrays.asList("sectionCode", "businessCode", "discount", "price");
			Map<String, Object> updateFields = new HashMap<>();

			// 3. 过滤参数：仅提取白名单内的字段，忽略其他所有字段
			for (String allowField : ALLOW_UPDATE_FIELDS) {
				Object value = params.get(allowField);
				// 字段值非空才加入修改（空值/空字符串不处理）
				if (value != null && !oConvertUtils.isEmpty(value.toString().trim())) {
					// 特殊处理：折扣(discount) 转换为BigDecimal并校验
					if ("discount".equals(allowField)) {
						try {
							// 转换为BigDecimal（支持小数，如0.85、1.0）
							BigDecimal discount = new BigDecimal(value.toString().trim());
							// 折扣校验：必须大于0，且不大于1（0 < discount ≤ 1）
							if (discount.compareTo(BigDecimal.ZERO) <= 0
									|| discount.compareTo(new BigDecimal("1")) > 0) {
								return Result.error("批量修改失败：折扣必须是0-1之间的小数（如0.85=85折，1=原价）");
							}
							updateFields.put(allowField, discount);
						} catch (NumberFormatException e) {
							return Result.error("批量修改失败：折扣必须是有效数字（如0.85、1.0）");
						}
					} else if ("price".equals(allowField)) {
						try {
							BigDecimal price = new BigDecimal(value.toString().trim());
							if (price.compareTo(BigDecimal.ZERO) < 0) {
								return Result.error("批量修改失败：价格不能为负数");
							}
							updateFields.put(allowField, price);
						} catch (NumberFormatException e) {
							return Result.error("批量修改失败：价格必须是有效数字");
						}
					} else {
						// 标段/编号：直接存字符串
						updateFields.put(allowField, value.toString().trim());
					}
				}
			}

			// 4. 校验：是否选择了要修改的字段
			if (updateFields.isEmpty()) {
				return Result.error("批量修改失败：仅支持修改【标段(sectionCode)、编号(businessCode)、折扣(discount)、价格(price)】，请传入有效字段值");
			}

			// 5. 构造批量更新条件，执行修改
			UpdateWrapper<TTextbook> updateWrapper = new UpdateWrapper<>();
			updateWrapper.in("id", ids); // 仅修改选中ID的记录

			// 核心修复：驼峰字段名转数据库下划线字段名，再设置到UpdateWrapper
			for (Map.Entry<String, Object> entry : updateFields.entrySet()) {
				String camelField = entry.getKey();
				String dbField = camelToUnderline(camelField); // 驼峰转下划线
				updateWrapper.set(dbField, entry.getValue()); // 用数据库字段名设置
			}

			updateWrapper.set("update_time", new Date());

			boolean priceOrDiscountChanged = updateFields.containsKey("price") || updateFields.containsKey("discount");
			boolean updateResult = tTextbookService.update(updateWrapper);
			if (updateResult) {
				log.info("教材表批量修改成功，修改ID：{}，修改字段：{}", idsStr, updateFields);
				if (priceOrDiscountChanged) {
					for (String textbookId : ids) {
						TTextbook textbook = tTextbookService.getById(textbookId);
						if (textbook != null) {
							syncBillPriceByTextbook(textbookId, textbook);
						}
					}
					log.info("教材价格/折扣修改后，已同步更新相关个人账单");
				}
				return Result.OK("批量修改成功！共修改" + ids.size() + "条记录，修改字段：" + updateFields.keySet());
			} else {
				return Result.error("批量修改失败：无记录被更新（请检查ID是否有效）");
			}

		} catch (Exception e) {
			log.error("教材表批量修改异常", e);
			return Result.error("批量修改失败：" + e.getMessage());
		}
	}

	/**
	 * 工具方法：驼峰命名转下划线命名
	 *
	 * @param camelStr 驼峰字符串
	 * @return 下划线字符串
	 */
	private String camelToUnderline(String camelStr) {
		if (StringUtils.isBlank(camelStr)) {
			return camelStr;
		}
		// 第一个字符小写
		camelStr = String.valueOf(camelStr.charAt(0)).toLowerCase() + camelStr.substring(1);
		// 遍历字符，大写字母前加下划线，再转小写
		StringBuilder sb = new StringBuilder();
		for (char c : camelStr.toCharArray()) {
			if (Character.isUpperCase(c)) {
				sb.append("_");
				sb.append(Character.toLowerCase(c));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	private void syncBillPriceByTextbook(String textbookId, TTextbook textbook) {
		try {
			BigDecimal price = textbook.getPrice() != null ? textbook.getPrice() : BigDecimal.ZERO;
			BigDecimal discount = textbook.getDiscount() != null ? textbook.getDiscount() : new BigDecimal("1");
			BigDecimal discountPrice = price.multiply(discount).setScale(2, java.math.RoundingMode.HALF_UP);

			UpdateWrapper<StudentBill> billUpdateWrapper = new UpdateWrapper<>();
			billUpdateWrapper.eq("textbook_name", textbook.getTextbookName());
			billUpdateWrapper.set("price", price);
			billUpdateWrapper.set("discount_price", discountPrice);
			billUpdateWrapper.set("update_time", new Date());

			boolean updateSuccess = studentBillService.update(billUpdateWrapper);
			if (updateSuccess) {
				log.info("【同步账单价格】教材ID={}，教材名称={}，新定价={}，新折后价={}，已同步更新相关个人账单",
						textbookId, textbook.getTextbookName(), price, discountPrice);
			} else {
				log.info("【同步账单价格】教材ID={}，教材名称={}，无匹配的个人账单需要更新",
						textbookId, textbook.getTextbookName());
			}
		} catch (Exception e) {
			log.error("【同步账单价格失败】教材ID={}，错误：{}", textbookId, e.getMessage());
		}
	}

}
