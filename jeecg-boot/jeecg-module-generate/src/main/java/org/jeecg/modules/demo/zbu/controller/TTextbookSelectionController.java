package org.jeecg.modules.demo.zbu.controller;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.query.QueryRuleEnum;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.demo.zbu.entity.*;
import org.jeecg.modules.demo.zbu.service.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.modules.demo.zbu.vo.ImportErrorExportUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 * @Description: 教材选用表
 * @Author: jeecg-boot
 * @Date: 2026-01-19
 * @Version: V1.0
 */
@Tag(name = "教材选用表")
@RestController
@RequestMapping("/zbu/tTextbookSelection")
@Slf4j
public class TTextbookSelectionController extends JeecgController<TTextbookSelection, ITTextbookSelectionService> {
	@Autowired
	private ITTextbookSelectionService tTextbookSelectionService;
	@Autowired
	private ITStudentService tStudentService;
	@Autowired
	private ITSubscriptionService tSubscriptionService;
	@Autowired
	private ITMajorService tMajorService;
	@Autowired
	private ITClassService tClassService;
	@Autowired
	private ITTextbookService tTextbookService;
	@Autowired
	private ITReceiveService tReceiveService;
	@Autowired
	private IStudentBillService tStudentBillService;
	@Autowired
	private StudentAllBillSummaryController studentAllBillSummaryController;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Value("${jeecg.path.upload}")
	private String uploadPath;

	/**
	 * 分页列表查询
	 *
	 * @param tTextbookSelection
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary = "教材选用表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(TTextbookSelection tTextbookSelection,
								   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
								   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
								   HttpServletRequest req) {

		// 构建基础SQL（使用视图）
		String baseSql = "SELECT * FROM v_textbook_selection_with_isbn WHERE 1=1";
		String countSql = "SELECT COUNT(*) FROM v_textbook_selection_with_isbn WHERE 1=1";

		// 添加查询条件
		if (oConvertUtils.isNotEmpty(tTextbookSelection.getMajorId())) {
			baseSql += " AND majorId = '" + tTextbookSelection.getMajorId() + "'";
			countSql += " AND majorId = '" + tTextbookSelection.getMajorId() + "'";
		}
		if (oConvertUtils.isNotEmpty(tTextbookSelection.getMajorName())) {
			baseSql += " AND majorName LIKE '%" + tTextbookSelection.getMajorName() + "%'";
			countSql += " AND majorName LIKE '%" + tTextbookSelection.getMajorName() + "%'";
		}
		if (oConvertUtils.isNotEmpty(tTextbookSelection.getClassId())) {
			baseSql += " AND classId = '" + tTextbookSelection.getClassId() + "'";
			countSql += " AND classId = '" + tTextbookSelection.getClassId() + "'";
		}
		if (oConvertUtils.isNotEmpty(tTextbookSelection.getClassName())) {
			baseSql += " AND className LIKE '%" + tTextbookSelection.getClassName() + "%'";
			countSql += " AND className LIKE '%" + tTextbookSelection.getClassName() + "%'";
		}
		if (oConvertUtils.isNotEmpty(tTextbookSelection.getTextbookId())) {
			baseSql += " AND textbookId = '" + tTextbookSelection.getTextbookId() + "'";
			countSql += " AND textbookId = '" + tTextbookSelection.getTextbookId() + "'";
		}
		if (oConvertUtils.isNotEmpty(tTextbookSelection.getTextbookName())) {
			baseSql += " AND textbookName LIKE '%" + tTextbookSelection.getTextbookName() + "%'";
			countSql += " AND textbookName LIKE '%" + tTextbookSelection.getTextbookName() + "%'";
		}
		if (oConvertUtils.isNotEmpty(tTextbookSelection.getIsbn())) {
			baseSql += " AND isbn LIKE '%" + tTextbookSelection.getIsbn() + "%'";
			countSql += " AND isbn LIKE '%" + tTextbookSelection.getIsbn() + "%'";
		}
		if (oConvertUtils.isNotEmpty(tTextbookSelection.getSchoolYear())) {
			baseSql += " AND schoolYear = '" + tTextbookSelection.getSchoolYear() + "'";
			countSql += " AND schoolYear = '" + tTextbookSelection.getSchoolYear() + "'";
		}
		if (oConvertUtils.isNotEmpty(tTextbookSelection.getSemester())) {
			baseSql += " AND semester LIKE '%" + tTextbookSelection.getSemester() + "%'";
			countSql += " AND semester LIKE '%" + tTextbookSelection.getSemester() + "%'";
		}
		if (oConvertUtils.isNotEmpty(tTextbookSelection.getSelectionStatus())) {
			baseSql += " AND selectionStatus LIKE '%" + tTextbookSelection.getSelectionStatus() + "%'";
			countSql += " AND selectionStatus LIKE '%" + tTextbookSelection.getSelectionStatus() + "%'";
		}

		// 添加排序（支持动态排序）
		String column = req.getParameter("column");
		String order = req.getParameter("order");
		if (oConvertUtils.isNotEmpty(column) && oConvertUtils.isNotEmpty(order)) {
			// 白名单校验，防止SQL注入
			Set<String> allowedColumns = new HashSet<>(Arrays.asList(
					"majorId", "majorName", "classId", "className",
					"textbookId", "textbookName", "isbn", "schoolYear",
					"semester", "selectionStatus", "createTime", "updateTime",
					"id", "quantity", "price", "totalPrice"));
			if (allowedColumns.contains(column)) {
				// 只允许asc或desc
				String orderUpper = order.toUpperCase();
				if ("ASC".equals(orderUpper) || "DESC".equals(orderUpper)) {
					baseSql += " ORDER BY " + column + " " + orderUpper;
				} else {
					baseSql += " ORDER BY createTime DESC";
				}
			} else {
				baseSql += " ORDER BY createTime DESC";
			}
		} else {
			baseSql += " ORDER BY createTime DESC";
		}

		// 添加分页
		int offset = (pageNo - 1) * pageSize;
		baseSql += " LIMIT " + offset + ", " + pageSize;

		log.info("【教材选用列表】执行SQL：{}", baseSql);

		// 执行查询
		List<Map<String, Object>> records = jdbcTemplate.queryForList(baseSql);

		// 转换字典值
		for (Map<String, Object> record : records) {
			// 转换学期
			String semester = (String) record.get("semester");
			if ("1".equals(semester)) {
				record.put("semester", "第一学期");
			} else if ("2".equals(semester)) {
				record.put("semester", "第二学期");
			}
			// 转换生效状态
			String selectionStatus = (String) record.get("selectionStatus");
			if ("1".equals(selectionStatus)) {
				record.put("selectionStatus", "生效");
			} else if ("0".equals(selectionStatus)) {
				record.put("selectionStatus", "未生效");
			}
		}

		// 获取总数
		int total = jdbcTemplate.queryForObject(countSql, Integer.class);

		// 构建分页结果
		Map<String, Object> result = new HashMap<>();
		result.put("records", records);
		result.put("total", total);
		result.put("size", pageSize);
		result.put("current", pageNo);
		result.put("pages", (total + pageSize - 1) / pageSize);

		log.info("【教材选用列表】查询结果总数：{}", total);
		return Result.OK(result);
	}

	/**
	 * 添加
	 *
	 * @param tTextbookSelection
	 * @return
	 */
	@AutoLog(value = "教材选用表-添加")
	@Operation(summary = "教材选用表-添加")
	@RequiresPermissions("zbu:t_textbook_selection:add")
	@PostMapping(value = "/add")
	@Transactional(rollbackFor = Exception.class)
	public Result<String> add(@RequestBody TTextbookSelection tTextbookSelection) {
		// 1. 保存教材选用记录
		tTextbookSelectionService.save(tTextbookSelection);
		log.info("新增教材选用记录成功，ID：{}", tTextbookSelection.getId());

		// 2. 自动生成征订记录
		try {
			generateSubscriptionFromSelection(tTextbookSelection.getId());
			log.info("为教材选用记录{}生成征订记录成功", tTextbookSelection.getId());
			StudentBill dummyBill = new StudentBill();
			dummyBill.setMajorName(tMajorService.getById(tTextbookSelection.getMajorId()).getMajorName());
			dummyBill.setSubscriptionYear(tTextbookSelection.getSchoolYear());
			dummyBill.setSubscriptionSemester(tTextbookSelection.getSemester());
			studentAllBillSummaryController.autoSummarySubscriptionData();
			log.info("新增教材选用后，触发总账单重新汇总");
		} catch (Exception e) {
			log.error("生成征订记录失败", e);
			throw new RuntimeException("新增教材选用成功，但生成征订记录失败：" + e.getMessage());
		}

		return Result.OK("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param tTextbookSelection
	 * @return
	 */
	@AutoLog(value = "教材选用表-编辑")
	@Operation(summary = "教材选用表-编辑")
	@RequiresPermissions("zbu:t_textbook_selection:edit")
	@RequestMapping(value = "/edit", method = { RequestMethod.PUT, RequestMethod.POST })
	@Transactional(rollbackFor = Exception.class)
	public Result<String> edit(@RequestBody TTextbookSelection tTextbookSelection) {
		try {
			// 1. 获取修改前的教材选用记录（旧数据）
			String selectionId = tTextbookSelection.getId();
			TTextbookSelection oldSelection = tTextbookSelectionService.getById(selectionId);
			if (oldSelection == null) {
				return Result.error("未找到待编辑的教材选用记录，ID：" + selectionId);
			}

			String newTextbookId = tTextbookSelection.getTextbookId();
			if (oConvertUtils.isNotEmpty(newTextbookId)) {
				TTextbook newTextbook = tTextbookService.getById(newTextbookId);
				if (newTextbook == null) {
					return Result.error("当前选择的教材ID不存在(若您重新导入了教材记录，也要重新导入教材选用记录)");
				}
			}

			// 2. 先保存教材选用主记录的修改
			boolean isUpdateSuccess = tTextbookSelectionService.updateById(tTextbookSelection);
			if (!isUpdateSuccess) {
				return Result.error("教材选用记录编辑失败");
			}
			log.info("编辑教材选用记录成功，ID：{}", selectionId);

			// 3. 查询该教材选用记录关联的所有征订记录（核心关联：selection_id）
			QueryWrapper<TSubscription> subWrapper = new QueryWrapper<>();
			subWrapper.eq("selection_id", selectionId);
			List<TSubscription> subList = tSubscriptionService.list(subWrapper);
			if (subList.isEmpty()) {
				log.info("教材选用记录{}无关联的征订记录，无需级联修改", selectionId);
				return Result.OK("编辑成功！无关联的征订/领取/账单记录需级联修改");
			}
			List<String> subIdList = subList.stream().map(TSubscription::getId).collect(Collectors.toList());

			// ========== 第一步：级联修改征订记录 ==========
			List<TSubscription> updateSubList = new ArrayList<>();
			for (TSubscription sub : subList) {
				boolean needUpdateSub = false;
				TSubscription updateSub = new TSubscription();
				updateSub.setId(sub.getId()); // 征订记录ID

				// 对比并更新：专业ID
				if (!Objects.equals(tTextbookSelection.getMajorId(), oldSelection.getMajorId())) {
					updateSub.setMajorId(tTextbookSelection.getMajorId());
					needUpdateSub = true;
				}
				// 对比并更新：教材ID
				if (!Objects.equals(tTextbookSelection.getTextbookId(), oldSelection.getTextbookId())) {
					updateSub.setTextbookId(tTextbookSelection.getTextbookId());
					needUpdateSub = true;
				}
				// 对比并更新：征订学年
				if (!Objects.equals(tTextbookSelection.getSchoolYear(), oldSelection.getSchoolYear())) {
					updateSub.setSubscriptionYear(tTextbookSelection.getSchoolYear());
					needUpdateSub = true;
				}
				// 对比并更新：征订学期
				if (!Objects.equals(tTextbookSelection.getSemester(), oldSelection.getSemester())) {
					updateSub.setSubscriptionSemester(tTextbookSelection.getSemester());
					needUpdateSub = true;
				}
				// 可选：生效状态关联征订状态（根据你的业务调整）
				if (!Objects.equals(tTextbookSelection.getSelectionStatus(), oldSelection.getSelectionStatus())) {
					String newSubscribeStatus = "1".equals(tTextbookSelection.getSelectionStatus()) ? "已生效" : "未生效";
					updateSub.setSubscribeStatus(newSubscribeStatus);
					needUpdateSub = true;
				}

				// 有需要更新的字段才加入批量更新列表
				if (needUpdateSub) {
					updateSub.setUpdateTime(new Date()); // 更新时间
					updateSubList.add(updateSub);
				}
			}
			// 批量更新征订记录
			if (!updateSubList.isEmpty()) {
				tSubscriptionService.updateBatchById(updateSubList);
				log.info("级联修改{}条征订记录", updateSubList.size());
			}

			// ========== 第二步：级联修改个人账单记录 ==========
			List<StudentBill> updateBillList = new ArrayList<>();
			for (TSubscription sub : subList) {
				// 重新查询最新的教材/专业信息（适配可能修改的教材ID/专业ID）
				TMajor newMajor = tMajorService.getById(tTextbookSelection.getMajorId());
				TTextbook newTextbook = tTextbookService.getById(tTextbookSelection.getTextbookId());

				// 计算最新的折扣价
				BigDecimal price = newTextbook != null ? newTextbook.getPrice() : BigDecimal.ZERO;
				BigDecimal discount = newTextbook != null && newTextbook.getDiscount() != null
						? newTextbook.getDiscount()
						: new BigDecimal("1");
				BigDecimal discountPrice = price.multiply(discount).setScale(2, BigDecimal.ROUND_HALF_UP);

				// 查询该征订记录关联的账单记录
				QueryWrapper<StudentBill> billWrapper = new QueryWrapper<>();
				billWrapper.eq("student_id", sub.getStudentId())
						.eq("subscription_year", oldSelection.getSchoolYear()) // 先按旧学年查
						.eq("subscription_semester", oldSelection.getSemester()) // 先按旧学期查
						.eq("textbook_name", tTextbookService.getById(oldSelection.getTextbookId()).getTextbookName()); // 按旧教材名称查
				List<StudentBill> billList = tStudentBillService.list(billWrapper);

				for (StudentBill bill : billList) {
					boolean needUpdateBill = false;
					StudentBill updateBill = new StudentBill();
					updateBill.setId(bill.getId()); // 账单ID

					// 对比并更新：专业名称
					String newMajorName = newMajor != null ? newMajor.getMajorName() : "";
					if (!Objects.equals(newMajorName, bill.getMajorName())) {
						updateBill.setMajorName(newMajorName);
						needUpdateBill = true;
					}
					// 对比并更新：教材名称
					String newTextbookName = newTextbook != null ? newTextbook.getTextbookName() : "";
					if (!Objects.equals(newTextbookName, bill.getTextbookName())) {
						updateBill.setTextbookName(newTextbookName);
						needUpdateBill = true;
					}
					// 对比并更新：征订学年
					if (!Objects.equals(tTextbookSelection.getSchoolYear(), bill.getSubscriptionYear())) {
						updateBill.setSubscriptionYear(tTextbookSelection.getSchoolYear());
						needUpdateBill = true;
					}
					// 对比并更新：征订学期
					if (!Objects.equals(tTextbookSelection.getSemester(), bill.getSubscriptionSemester())) {
						updateBill.setSubscriptionSemester(tTextbookSelection.getSemester());
						needUpdateBill = true;
					}
					// 对比并更新：教材定价
					if (!Objects.equals(price, bill.getPrice())) {
						updateBill.setPrice(price);
						needUpdateBill = true;
					}
					// 对比并更新：折扣后费用
					if (!Objects.equals(discountPrice, bill.getDiscountPrice())) {
						updateBill.setDiscountPrice(discountPrice);
						needUpdateBill = true;
					}
					// 对比并更新：征订状态（关联生效状态）
					String newSubscribeStatus = "1".equals(tTextbookSelection.getSelectionStatus()) ? "已生效" : "未生效";
					if (!Objects.equals(newSubscribeStatus, bill.getSubscribeStatus())) {
						updateBill.setSubscribeStatus(newSubscribeStatus);
						needUpdateBill = true;
					}

					// 有需要更新的字段才加入批量更新列表
					if (needUpdateBill) {
						updateBill.setUpdateTime(new Date()); // 更新时间
						updateBillList.add(updateBill);
					}
				}
			}
			// 批量更新个人账单记录
			if (!updateBillList.isEmpty()) {
				tStudentBillService.updateBatchById(updateBillList);
				log.info("级联修改{}条个人账单记录", updateBillList.size());
			}

			// ========== 第三步：级联修改领取记录（可选，根据业务调整） ==========
			List<TReceive> updateReceiveList = new ArrayList<>();
			QueryWrapper<TReceive> receiveWrapper = new QueryWrapper<>();
			receiveWrapper.in("subscription_id", subIdList);
			List<TReceive> receiveList = tReceiveService.list(receiveWrapper);

			for (TReceive receive : receiveList) {
				boolean needUpdateReceive = false;
				TReceive updateReceive = new TReceive();
				updateReceive.setId(receive.getId()); // 领取记录ID

				// 生效状态关联领取状态（比如失效后领取状态改为"已作废"）
				if (!Objects.equals(tTextbookSelection.getSelectionStatus(), oldSelection.getSelectionStatus())) {
					String newReceiveStatus = "1".equals(tTextbookSelection.getSelectionStatus()) ? "未领取" : "已作废";
					updateReceive.setReceiveStatus(newReceiveStatus);
					needUpdateReceive = true;
				}

				if (needUpdateReceive) {
					updateReceive.setUpdateTime(new Date()); // 更新时间
					updateReceiveList.add(updateReceive);
				}
			}
			// 批量更新领取记录
			if (!updateReceiveList.isEmpty()) {
				tReceiveService.updateBatchById(updateReceiveList);
				log.info("级联修改{}条领取记录", updateReceiveList.size());
			}

			StudentBill dummyBill = new StudentBill();
			dummyBill.setMajorName(tMajorService.getById(tTextbookSelection.getMajorId()).getMajorName());
			dummyBill.setSubscriptionYear(tTextbookSelection.getSchoolYear());
			dummyBill.setSubscriptionSemester(tTextbookSelection.getSemester());
			studentAllBillSummaryController.autoSummarySubscriptionData();
			log.info("编辑教材选用后，触发总账单重新汇总");

			return Result.OK("编辑成功！已级联修改关联的征订/领取/账单记录（如有）");
		} catch (Exception e) {
			log.error("编辑教材选用记录失败", e);
			throw new RuntimeException("编辑失败：" + e.getMessage());
		}
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "教材选用表-通过id删除")
	@Operation(summary = "教材选用表-通过id删除")
	@RequiresPermissions("zbu:t_textbook_selection:delete")
	@DeleteMapping(value = "/delete")
	@Transactional(rollbackFor = Exception.class)
	public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
		try {
			// 1. 查询关联的征订记录
			QueryWrapper<TSubscription> subWrapper = new QueryWrapper<>();
			subWrapper.eq("selection_id", id);
			List<TSubscription> subList = tSubscriptionService.list(subWrapper);
			if (!subList.isEmpty()) {
				List<String> subIdList = subList.stream().map(TSubscription::getId).collect(Collectors.toList());

				// ========== 核心修复：批量处理个人账单删除 ==========
				List<StudentBill> billList = new ArrayList<>();
				for (TSubscription sub : subList) {
					// 修复1：通过征订表的studentId（学生主键）查询学生，获取业务学号
					TStudent student = tStudentService.getById(sub.getStudentId());
					if (student == null) {
						log.warn("征订记录{}的学生主键ID{}不存在，跳过该账单删除", sub.getId(), sub.getStudentId());
						continue;
					}
					String studentNo = student.getStudentId(); // 业务学号（账单表存储的是这个）
					if (oConvertUtils.isEmpty(studentNo)) {
						log.warn("征订记录{}的学生主键ID{}无业务学号，跳过该账单删除", sub.getId(), sub.getStudentId());
						continue;
					}

					// 修复2：查询教材名称（增加空指针防护）
					String textbookName = "";
					TTextbook textbook = tTextbookService.getById(sub.getTextbookId());
					if (textbook != null) {
						textbookName = textbook.getTextbookName();
					}
					if (oConvertUtils.isEmpty(textbookName)) {
						log.warn("征订记录{}的教材ID{}未查询到教材名称，跳过该账单删除", sub.getId(), sub.getTextbookId());
						continue;
					}

					// 修复3：账单查询条件（业务学号 + 学年 + 学期 + 教材名称）
					// 兼容新旧学年/学期：用征订表的最新学年/学期，而非旧的选用记录
					QueryWrapper<StudentBill> billWrapper = new QueryWrapper<>();
					billWrapper.eq("student_id", studentNo) // 关键：用业务学号匹配
							.eq("subscription_year", sub.getSubscriptionYear())
							.eq("subscription_semester", sub.getSubscriptionSemester())
							.eq("textbook_name", textbookName);

					// 打印查询条件（方便排查）
					log.info("【账单删除】查询条件：studentNo={}, 学年={}, 学期={}, 教材={}",
							studentNo, sub.getSubscriptionYear(), sub.getSubscriptionSemester(), textbookName);

					List<StudentBill> subBillList = tStudentBillService.list(billWrapper);
					log.info("征订记录{}匹配到{}条个人账单记录（业务学号：{}，教材名称：{}）",
							sub.getId(), subBillList.size(), studentNo, textbookName);
					billList.addAll(subBillList);
				}

				// 1.3 删除账单记录（有匹配到才删）
				if (!billList.isEmpty()) {
					List<String> billIdList = billList.stream().map(StudentBill::getId).collect(Collectors.toList());
					boolean isBillDelSuccess = tStudentBillService.removeByIds(billIdList);
					log.info("级联删除{}条个人账单记录，删除结果：{}", billIdList.size(), isBillDelSuccess);
				} else {
					log.info("未匹配到关联的个人账单记录，跳过账单删除");
				}

				// 2. 删除关联的领取记录
				QueryWrapper<TReceive> receiveWrapper = new QueryWrapper<>();
				receiveWrapper.in("subscription_id", subIdList);
				long receiveDelCount = tReceiveService.count(receiveWrapper);
				if (receiveDelCount > 0) {
					tReceiveService.remove(receiveWrapper);
					log.info("级联删除{}条领取记录", receiveDelCount);
				}

				// 3. 删除关联的征订记录
				boolean isSubDelSuccess = tSubscriptionService.removeByIds(subIdList);
				log.info("级联删除{}条征订记录，删除结果：{}", subIdList.size(), isSubDelSuccess);
			}

			// 4. 删除教材选用主记录
			boolean isSelectionDelSuccess = tTextbookSelectionService.removeById(id);
			log.info("删除教材选用记录{}，删除结果：{}", id, isSelectionDelSuccess);

			TTextbookSelection delSelection = tTextbookSelectionService.getById(id);
			if (delSelection != null) {
				studentAllBillSummaryController.batchIncrementSummary(
						tMajorService.getById(delSelection.getMajorId()).getMajorName(),
						delSelection.getSchoolYear(),
						delSelection.getSemester());
				log.info("删除教材选用后，触发【增量汇总】总账单（而非全量）");
			}

			return Result.OK("删除成功！已级联删除关联的征订/领取/账单记录（如有）");
		} catch (Exception e) {
			log.error("删除教材选用记录失败", e);
			throw new RuntimeException("删除失败：" + e.getMessage());
		}
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "教材选用表-批量删除")
	@Operation(summary = "教材选用表-批量删除")
	@RequiresPermissions("zbu:t_textbook_selection:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	@Transactional(rollbackFor = Exception.class)
	public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
		try {
			List<String> selectionIdList = Arrays.asList(ids.split(","));
			if (selectionIdList.isEmpty()) {
				return Result.error("删除失败：未选择有效记录");
			}

			// 1. 查询关联的征订记录
			QueryWrapper<TSubscription> subWrapper = new QueryWrapper<>();
			subWrapper.in("selection_id", selectionIdList);
			List<TSubscription> subList = tSubscriptionService.list(subWrapper);
			if (!subList.isEmpty()) {
				List<String> subIdList = subList.stream().map(TSubscription::getId).collect(Collectors.toList());

				// ========== 核心修复：批量处理个人账单删除 ==========
				List<StudentBill> billList = new ArrayList<>();
				for (TSubscription sub : subList) {
					// 修复1：获取学生业务学号
					TStudent student = tStudentService.getById(sub.getStudentId());
					if (student == null || oConvertUtils.isEmpty(student.getStudentId())) {
						log.warn("征订记录{}的学生ID{}无业务学号，跳过账单删除", sub.getId(), sub.getStudentId());
						continue;
					}
					String studentNo = student.getStudentId();

					// 修复2：安全获取教材名称
					String textbookName = "";
					TTextbook textbook = tTextbookService.getById(sub.getTextbookId());
					if (textbook != null) {
						textbookName = textbook.getTextbookName();
					}
					if (oConvertUtils.isEmpty(textbookName)) {
						log.warn("征订记录{}的教材ID{}无名称，跳过账单删除", sub.getId(), sub.getTextbookId());
						continue;
					}

					// 修复3：精准查询账单（业务学号 + 征订表的学年/学期 + 教材名称）
					QueryWrapper<StudentBill> billWrapper = new QueryWrapper<>();
					billWrapper.eq("student_id", studentNo)
							.eq("subscription_year", sub.getSubscriptionYear())
							.eq("subscription_semester", sub.getSubscriptionSemester())
							.eq("textbook_name", textbookName);

					List<StudentBill> subBillList = tStudentBillService.list(billWrapper);
					log.info("征订记录{}匹配到{}条账单（业务学号：{}，教材：{}）",
							sub.getId(), subBillList.size(), studentNo, textbookName);
					billList.addAll(subBillList);
				}

				// 删除账单
				if (!billList.isEmpty()) {
					List<String> billIdList = billList.stream().map(StudentBill::getId).collect(Collectors.toList());
					boolean isBillDelSuccess = tStudentBillService.removeByIds(billIdList);
					log.info("批量级联删除{}条个人账单记录，删除结果：{}", billIdList.size(), isBillDelSuccess);
				} else {
					log.info("未匹配到关联的个人账单记录，跳过账单删除");
				}

				// 2. 删除领取记录
				QueryWrapper<TReceive> receiveWrapper = new QueryWrapper<>();
				receiveWrapper.in("subscription_id", subIdList);
				long receiveDelCount = tReceiveService.count(receiveWrapper);
				if (receiveDelCount > 0) {
					tReceiveService.remove(receiveWrapper);
					log.info("批量级联删除{}条领取记录", receiveDelCount);
				}

				// 3. 删除征订记录
				boolean isSubDelSuccess = tSubscriptionService.removeByIds(subIdList);
				log.info("批量级联删除{}条征订记录，删除结果：{}", subIdList.size(), isSubDelSuccess);
			}

			// 4. 批量删除教材选用主记录
			boolean isSelectionDelSuccess = this.tTextbookSelectionService.removeByIds(selectionIdList);
			log.info("批量删除{}条教材选用记录，删除结果：{}", selectionIdList.size(), isSelectionDelSuccess);

			if (!selectionIdList.isEmpty()) {
				TTextbookSelection firstDel = tTextbookSelectionService.getById(selectionIdList.get(0));
				if (firstDel != null) {
					studentAllBillSummaryController.batchIncrementSummary(
							tMajorService.getById(firstDel.getMajorId()).getMajorName(),
							firstDel.getSchoolYear(),
							firstDel.getSemester());
					log.info("批量删除教材选用后，触发【增量汇总】总账单（而非全量）");
				}
			}

			return Result.OK("批量删除成功！已级联删除关联的征订/领取/账单记录（如有）");
		} catch (Exception e) {
			log.error("批量删除教材选用记录失败", e);
			throw new RuntimeException("批量删除失败：" + e.getMessage());
		}
	}

	/**
	 * 教材选用表批量删除（POST请求，支持大数据量）
	 *
	 * @param requestBody 请求体，格式为 {"ids":["id1","id2",...]} 或 "id1,id2,id3"
	 * @return
	 */
	@AutoLog(value = "教材选用表-批量删除")
	@Operation(summary = "教材选用表-批量删除")
	@RequiresPermissions("zbu:t_textbook_selection:deleteBatch")
	@PostMapping(value = "/deleteBatch")
	@Transactional(rollbackFor = Exception.class)
	public Result<String> deleteBatchPost(@RequestBody String requestBody) {
		try {
			if (oConvertUtils.isEmpty(requestBody)) {
				return Result.error("删除参数不能为空");
			}

			List<String> selectionIdList = new ArrayList<>();

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
								selectionIdList.add(id);
							}
						}
					}
				} catch (Exception e) {
					log.warn("解析JSON格式失败，尝试按逗号分割：{}", requestBody);
				}
			}

			// 如果不是 JSON 格式，按逗号分割
			if (selectionIdList.isEmpty()) {
				String[] ids = requestBody.replace("\"", "").replace(" ", "").split(",");
				for (String id : ids) {
					if (oConvertUtils.isNotEmpty(id)) {
						selectionIdList.add(id);
					}
				}
			}

			if (selectionIdList.isEmpty()) {
				return Result.error("删除失败：未选择有效记录");
			}

			log.info("教材选用表批量删除，IDs数量：{}", selectionIdList.size());

			// 1. 查询关联的征订记录
			QueryWrapper<TSubscription> subWrapper = new QueryWrapper<>();
			subWrapper.in("selection_id", selectionIdList);
			List<TSubscription> subList = tSubscriptionService.list(subWrapper);
			if (!subList.isEmpty()) {
				List<String> subIdList = subList.stream().map(TSubscription::getId).collect(Collectors.toList());

				// 批量处理个人账单删除
				List<StudentBill> billList = new ArrayList<>();
				for (TSubscription sub : subList) {
					TStudent student = tStudentService.getById(sub.getStudentId());
					if (student == null || oConvertUtils.isEmpty(student.getStudentId())) {
						log.warn("征订记录{}的学生ID{}无业务学号，跳过账单删除", sub.getId(), sub.getStudentId());
						continue;
					}
					String studentNo = student.getStudentId();

					String textbookName = "";
					TTextbook textbook = tTextbookService.getById(sub.getTextbookId());
					if (textbook != null) {
						textbookName = textbook.getTextbookName();
					}
					if (oConvertUtils.isEmpty(textbookName)) {
						log.warn("征订记录{}的教材ID{}无名称，跳过账单删除", sub.getId(), sub.getTextbookId());
						continue;
					}

					QueryWrapper<StudentBill> billWrapper = new QueryWrapper<>();
					billWrapper.eq("student_id", studentNo)
							.eq("subscription_year", sub.getSubscriptionYear())
							.eq("subscription_semester", sub.getSubscriptionSemester())
							.eq("textbook_name", textbookName);

					List<StudentBill> subBillList = tStudentBillService.list(billWrapper);
					log.info("征订记录{}匹配到{}条账单（业务学号：{}，教材：{}）",
							sub.getId(), subBillList.size(), studentNo, textbookName);
					billList.addAll(subBillList);
				}

				if (!billList.isEmpty()) {
					List<String> billIdList = billList.stream().map(StudentBill::getId).collect(Collectors.toList());
					boolean isBillDelSuccess = tStudentBillService.removeByIds(billIdList);
					log.info("批量级联删除{}条个人账单记录，删除结果：{}", billIdList.size(), isBillDelSuccess);
				} else {
					log.info("未匹配到关联的个人账单记录，跳过账单删除");
				}

				QueryWrapper<TReceive> receiveWrapper = new QueryWrapper<>();
				receiveWrapper.in("subscription_id", subIdList);
				long receiveDelCount = tReceiveService.count(receiveWrapper);
				if (receiveDelCount > 0) {
					tReceiveService.remove(receiveWrapper);
					log.info("批量级联删除{}条领取记录", receiveDelCount);
				}

				boolean isSubDelSuccess = tSubscriptionService.removeByIds(subIdList);
				log.info("批量级联删除{}条征订记录，删除结果：{}", subIdList.size(), isSubDelSuccess);
			}

			boolean isSelectionDelSuccess = this.tTextbookSelectionService.removeByIds(selectionIdList);
			log.info("批量删除{}条教材选用记录，删除结果：{}", selectionIdList.size(), isSelectionDelSuccess);

			if (!selectionIdList.isEmpty()) {
				TTextbookSelection firstDel = tTextbookSelectionService.getById(selectionIdList.get(0));
				if (firstDel != null) {
					studentAllBillSummaryController.batchIncrementSummary(
							tMajorService.getById(firstDel.getMajorId()).getMajorName(),
							firstDel.getSchoolYear(),
							firstDel.getSemester());
					log.info("批量删除教材选用后，触发【增量汇总】总账单（而非全量）");
				}
			}

			return Result.OK("批量删除成功！已级联删除关联的征订/领取/账单记录（如有）");
		} catch (Exception e) {
			log.error("批量删除教材选用记录失败", e);
			throw new RuntimeException("批量删除失败：" + e.getMessage());
		}
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	// @AutoLog(value = "教材选用表-通过id查询")
	@Operation(summary = "教材选用表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<TTextbookSelection> queryById(@RequestParam(name = "id", required = true) String id) {
		TTextbookSelection tTextbookSelection = tTextbookSelectionService.getById(id);
		if (tTextbookSelection == null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(tTextbookSelection);
	}

	/**
	 * 导出excel
	 *
	 * @param request
	 * @param tTextbookSelection
	 */
	@RequiresPermissions("zbu:t_textbook_selection:exportXls")
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request, TTextbookSelection tTextbookSelection) {
		return super.exportXls(request, tTextbookSelection, TTextbookSelection.class, "教材选用表");
	}

	/**
	 * 通过excel导入数据
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	@RequiresPermissions("zbu:t_textbook_selection:importExcel")
	@RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	@Transactional(rollbackFor = Exception.class)
	public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
		try {
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
			Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();

			for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
				MultipartFile file = entity.getValue();
				ImportParams params = new ImportParams();
				params.setTitleRows(2); // 2行标题行（第1、2行）
				params.setHeadRows(1); // 1行表头行（第3行）
				params.setNeedSave(false);

				// 1. 读取Excel数据
				List<TTextbookSelection> list = ExcelImportUtil.importExcel(
						file.getInputStream(), TTextbookSelection.class, params);
				// 调试日志：打印读取到的总行数
				log.info("读取到Excel数据总行数：{}", list.size());

				// 打印每行数据的majorId字段内容
				for (int i = 0; i < list.size(); i++) {
					TTextbookSelection s = list.get(i);
					log.info("第{}行 majorId原始值=[{}], textbookId=[{}], schoolYear=[{}]",
							i + 1, s.getMajorId(), s.getTextbookId(), s.getSchoolYear());
				}

				// 2. 手动解析文本→ID + 校验必填
				List<TTextbookSelection> validList = new ArrayList<>();
				List<String> errorMsgList = new ArrayList<>();
				// 存储原始的专业/班级输入内容，使用行索引作为key（因为修改selection字段会改变hashCode导致Map找不到）
				Map<Integer, String> rawMajorInputMap = new HashMap<>();

				for (int i = 0; i < list.size(); i++) {
					TTextbookSelection selection = list.get(i);
					// 关键修正：行号计算（2标题+1表头 + i+1 = i+4）
					int rowNum = i + 4;
					boolean isValid = true;

					// ===== 专业解析（支持分号分隔的多个专业/班级） =====
					String majorContent = selection.getMajorId();
					log.info("第{}行 处理专业字段,原始值=[{}], isEmpty={}", rowNum, majorContent,
							oConvertUtils.isEmpty(majorContent));
					if (oConvertUtils.isEmpty(majorContent)) {
						errorMsgList.add("第" + rowNum + "行：专业不能为空");
						isValid = false;
					} else {
						// 保存原始输入内容，使用行索引作为key
						rawMajorInputMap.put(rowNum, majorContent.trim());
						selection.setMajorId(""); // 临时设置为空
					}

					// ===== 教材解析（兼容ID/名称） =====
					String textbookContent = selection.getTextbookId();
					String textbookIsbn = null;
					if (oConvertUtils.isEmpty(textbookContent)) {
						errorMsgList.add("第" + rowNum + "行：教材不能为空");
						isValid = false;
					} else {
						if (textbookContent.matches("^\\d{16,}$")) {
							selection.setTextbookId(textbookContent.trim());
							QueryWrapper<TTextbook> textbookIdWrapper = new QueryWrapper<>();
							textbookIdWrapper.eq("id", textbookContent.trim());
							TTextbook textbook = tTextbookService.getOne(textbookIdWrapper);
							if (textbook == null) {
								errorMsgList.add("第" + rowNum + "行：教材ID「" + textbookContent + "」不存在，请检查");
								isValid = false;
							} else {
								textbookIsbn = textbook.getIsbn();
							}
						} else {
							QueryWrapper<TTextbook> textbookWrapper = new QueryWrapper<>();
							textbookWrapper.eq("textbook_name", textbookContent.trim());
							TTextbook textbook = tTextbookService.getOne(textbookWrapper);
							if (textbook == null) {
								errorMsgList.add("第" + rowNum + "行：教材名称「" + textbookContent + "」不存在，请检查");
								isValid = false;
							} else {
								selection.setTextbookId(textbook.getId());
								textbookIsbn = textbook.getIsbn();
							}
						}
					}

					// ===== 补充默认值 =====
					if (isValid) {
						if (oConvertUtils.isEmpty(selection.getSchoolYear())) {
							selection.setSchoolYear("");
						}
						if (oConvertUtils.isEmpty(selection.getSemester())) {
							selection.setSemester("1"); // 默认第一学期
						}
						if (oConvertUtils.isEmpty(selection.getSelectionStatus())) {
							selection.setSelectionStatus("1"); // 默认启用
						}
						selection.setCreateTime(new Date());
						selection.setUpdateTime(new Date());
						// 将行号附加到selection上，用于后续查找原始输入
						selection.setRemark(rowNum + ""); // 临时用remark存储行号
						validList.add(selection);
					}
				}

				// 3. 如果有错误，记录警告但不阻止导入（只要还有有效数据）
				if (!errorMsgList.isEmpty()) {
					log.warn("导入时发现{}处错误，将跳过这些记录", errorMsgList.size());
				}

				if (validList.isEmpty()) {
					return Result.error("未检测到有效数据，请检查Excel内容。\n错误信息：\n" + String.join("\n", errorMsgList));
				}

				// 4. 保存数据 + 生成征订记录（根据专业/班级自动拆分）
				List<TTextbookSelection> finalSaveList = new ArrayList<>();
				List<String> skipMsgList = new ArrayList<>(); // 记录跳过的行及原因

				// 为每个有效记录查找对应的原始专业输入（通过remark中存储的行号）
				for (int idx = 0; idx < validList.size(); idx++) {
					TTextbookSelection selection = validList.get(idx);
					// 从remark中获取行号（之前临时存储的）
					Integer rowNum = Integer.parseInt(selection.getRemark());
					String rawMajorContent = rawMajorInputMap.getOrDefault(rowNum, "");
					log.info("开始处理第{}行原始专业输入：{}", rowNum, rawMajorContent);

					// 解析专业/班级（支持分号、顿号等多种分隔符）
					List<TClass> matchedClasses = parseMajorAndClassContent(rawMajorContent, rowNum);

					if (matchedClasses.isEmpty()) {
						String skipMsg = "第" + rowNum + "行：专业/班级「" + rawMajorContent + "」不存在，已跳过";
						skipMsgList.add(skipMsg);
						log.warn(skipMsg);
						continue;
					}

					// 去重
					matchedClasses = matchedClasses.stream().distinct().collect(Collectors.toList());

					// 为每个班级创建一条教材选用记录
					for (TClass clazz : matchedClasses) {
						TTextbookSelection newSelection = new TTextbookSelection();
						newSelection.setMajorId(clazz.getMajorId());
						newSelection.setClassId(clazz.getId());
						newSelection.setTextbookId(selection.getTextbookId());
						newSelection.setSchoolYear(selection.getSchoolYear());
						newSelection.setSemester(selection.getSemester());
						newSelection.setSelectionStatus(selection.getSelectionStatus());
						newSelection.setRemark(selection.getRemark());
						newSelection.setCreateTime(new Date());
						newSelection.setUpdateTime(new Date());

						// 查重：学年+学期+教材+专业+班级
						QueryWrapper<TTextbookSelection> dupWrapper = new QueryWrapper<>();
						dupWrapper.eq("school_year", newSelection.getSchoolYear())
								.eq("semester", newSelection.getSemester())
								.eq("textbook_id", newSelection.getTextbookId())
								.eq("major_id", newSelection.getMajorId())
								.eq("class_id", newSelection.getClassId());
						long dupCount = tTextbookSelectionService.count(dupWrapper);
						if (dupCount == 0) {
							finalSaveList.add(newSelection);
						} else {
							log.info("跳过重复记录：学年={}, 学期={}, 教材={}, 专业={}, 班级={}",
									newSelection.getSchoolYear(), newSelection.getSemester(),
									newSelection.getTextbookId(), newSelection.getMajorId(), newSelection.getClassId());
						}
					}
				}


				// 构建返回信息 - 提前合并错误列表
				List<String> allFailMsgList = new ArrayList<>();
				if (!errorMsgList.isEmpty()) {
					allFailMsgList.addAll(errorMsgList);
				}
				if (!skipMsgList.isEmpty()) {
					allFailMsgList.addAll(skipMsgList);
				}
				String successMsg = "导入成功！共导入" + finalSaveList.size() + "条教材选用记录（已按专业自动拆分班级）";

				if (!finalSaveList.isEmpty()) {
					tTextbookSelectionService.saveBatch(finalSaveList);
					for (TTextbookSelection selection : finalSaveList) {
						generateSubscriptionFromSelection(selection.getId());
						StudentBill dummyBill = new StudentBill();
						dummyBill.setMajorName(tMajorService.getById(selection.getMajorId()).getMajorName());
						dummyBill.setSubscriptionYear(selection.getSchoolYear());
						dummyBill.setSubscriptionSemester(selection.getSemester());
						studentAllBillSummaryController.incrementSummary(dummyBill, false);
					}
	
					log.info("导入教材选用后，触发总账单重新汇总");
			}

				return ImportErrorExportUtil.buildErrorResult(allFailMsgList, successMsg, uploadPath, "教材选用表");
			}
		} catch (Exception e) {
			log.error("Excel导入失败", e);
			return Result.error("导入失败：" + e.getMessage());
		}
		return Result.error("导入失败！");
	}

	/**
	 * 根据班级名称查找班级
	 */
	private TClass findClassByName(String className) {
		QueryWrapper<TClass> classWrapper = new QueryWrapper<>();
		classWrapper.eq("class_name", className.trim());
		return tClassService.getOne(classWrapper);
	}

	/**
	 * 解析专业/班级内容（支持分号、顿号等多种分隔符）
	 * @param content 原始内容
	 * @param rowNum 行号（用于日志）
	 * @return 匹配到的班级列表
	 */
	private List<TClass> parseMajorAndClassContent(String content, int rowNum) {
		List<TClass> result = new ArrayList<>();
		log.info("第{}行：开始解析专业/班级内容：{}", rowNum, content);

		// 第一步：按分号分割（处理独立组）
		String[] segments = content.split("[;；]+");

		for (String segment : segments) {
			segment = segment.trim();
			if (segment.isEmpty()) continue;

			// 第二步：先尝试将整个段作为班级模式解析（支持 "24计科1、2、3班" 等）
			List<TClass> segmentClasses = tryExpandClassNumbers(segment);

			if (!segmentClasses.isEmpty()) {
				// 成功解析出班级
				result.addAll(segmentClasses);
			} else {
				// 没匹配到班级模式，尝试按顿号/逗号分割（处理 "24工商、24财管、24营销" 等）
				String[] subItems = segment.split("[、,，]+");

				if (subItems.length > 1) {
					// 有多个子项，分别处理每个
					for (String subItem : subItems) {
						subItem = subItem.trim();
						if (subItem.isEmpty()) continue;
						List<TClass> subClasses = resolveClassOrMajor(subItem, rowNum);
						result.addAll(subClasses);
					}
				} else {
					// 单个项，直接解析
					List<TClass> subClasses = resolveClassOrMajor(segment, rowNum);
					result.addAll(subClasses);
				}
			}
		}

		// 去重
		result = result.stream().distinct().collect(Collectors.toList());
		log.info("第{}行：解析完成，共匹配{}个班级", rowNum, result.size());

		return result;
	}

	/**
	 * 解析单个专业/班级项
	 */
	private List<TClass> resolveClassOrMajor(String item, int rowNum) {
		List<TClass> result = new ArrayList<>();

		// 尝试1：匹配 "前缀+班号列表+班" 模式，如 "24计科1、2、3班"
		List<TClass> expanded = tryExpandClassNumbers(item);
		if (!expanded.isEmpty()) {
			result.addAll(expanded);
			return result;
		}

		// 尝试2：直接作为班级名称匹配
		TClass directClass = findClassByName(item);
		if (directClass != null) {
			result.add(directClass);
			return result;
		}

		// 尝试3：作为专业名称精确匹配
		QueryWrapper<TMajor> majorWrapper = new QueryWrapper<>();
		majorWrapper.eq("major_name", item);
		TMajor major = tMajorService.getOne(majorWrapper);
		if (major != null) {
			QueryWrapper<TClass> classWrapper = new QueryWrapper<>();
			classWrapper.eq("major_id", major.getId());
			List<TClass> majorClasses = tClassService.list(classWrapper);
			result.addAll(majorClasses);
			log.info("第{}行：专业「{}」下找到{}个班级", rowNum, item, majorClasses.size());
			return result;
		}

		// 尝试4：模糊匹配专业名称
		List<TClass> fuzzyClasses = tryFuzzyMatchMajor(item);
		result.addAll(fuzzyClasses);

		return result;
	}

	/**
	 * 尝试展开班号列表模式
	 * 支持格式：
	 * - "24计科1、2、3班"
	 * - "24计科专升本1、2、3班"
	 * - "24计科1,2,3,4班"
	 */
	private List<TClass> tryExpandClassNumbers(String segment) {
		List<TClass> result = new ArrayList<>();

		// 匹配模式：前缀文字+数字分隔列表+可选"班"字
		// 例如：24计科1、2、3班 → prefix="24计科", numbers=[1,2,3], suffix="班"
		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
				"^(.+?)(\\d+(?:[、,，]\\d+)+)(班)?$"
		);
		java.util.regex.Matcher matcher = pattern.matcher(segment);

		if (!matcher.find()) {
			return result;
		}

		String prefix = matcher.group(1);  // 例如："24计科" 或 "24计科专升本"
		String numbersStr = matcher.group(2);  // 例如："1、2、3"
		String suffix = matcher.group(3) != null ? matcher.group(3) : "班";  // "班" 或空

		// 解析班号列表
		String[] numberParts = numbersStr.split("[、,，]+");

		for (String numPart : numberParts) {
			numPart = numPart.trim();
			if (numPart.isEmpty()) continue;

			// 构造完整班级名称
			String className = prefix + numPart + suffix;
			TClass clazz = findClassByName(className);
			if (clazz != null) {
				result.add(clazz);
				log.debug("匹配到班级：{}", className);
			} else {
				log.debug("未找到班级：{}", className);
			}
		}

		return result;
	}

	/**
	 * 尝试模糊匹配专业名称
	 * 例如："24计科" 可能匹配 "24计算机科学与技术"
	 */
	private List<TClass> tryFuzzyMatchMajor(String segment) {
		List<TClass> result = new ArrayList<>();

		// 提取专业部分（去掉年级前缀）
		String majorPart = segment.replaceFirst("^\\d+", "");

		if (majorPart.isEmpty()) {
			return result;
		}

		// 查询所有专业，尝试模糊匹配
		QueryWrapper<TMajor> majorWrapper = new QueryWrapper<>();
		majorWrapper.like("major_name", majorPart);
		List<TMajor> matchedMajors = tMajorService.list(majorWrapper);

		for (TMajor major : matchedMajors) {
			// 构造完整的专业名称（带年级）
			String fullMajorName = segment;

			// 尝试直接匹配完整专业名称
			QueryWrapper<TClass> classWrapper = new QueryWrapper<>();
			classWrapper.eq("major_id", major.getId());
			List<TClass> classes = tClassService.list(classWrapper);
			result.addAll(classes);
			log.info("模糊匹配专业「{}」→「{}」，找到{}个班级", segment, major.getMajorName(), classes.size());
		}

		return result;
	}

	/**
	 * 根据教材选用记录ID，为对应班级的所有学生生成征订记录+领取记录+个人账单记录
	 *
	 * @param selectionId 教材选用记录ID
	 */
	private void generateSubscriptionFromSelection(String selectionId) {
		// 1. 查询教材选用记录详情（原有逻辑，不变）
		TTextbookSelection selection = tTextbookSelectionService.getById(selectionId);
		if (selection == null) {
			throw new RuntimeException("教材选用记录不存在，ID：" + selectionId);
		}

		// 校验关键字段（原有逻辑，不变）
		if (oConvertUtils.isEmpty(selection.getClassId())) {
			throw new RuntimeException("教材选用记录" + selectionId + "未设置班级，无法生成征订记录");
		}
		if (oConvertUtils.isEmpty(selection.getTextbookId())) {
			throw new RuntimeException("教材选用记录" + selectionId + "未设置教材，无法生成征订记录");
		}
		if (oConvertUtils.isEmpty(selection.getMajorId())) {
			throw new RuntimeException("教材选用记录" + selectionId + "未设置专业，无法生成征订记录");
		}

		// 2. 查询该班级下的所有学生（原有逻辑，不变）
		QueryWrapper<TStudent> studentWrapper = new QueryWrapper<>();
		studentWrapper.eq("class_id", selection.getClassId());
		studentWrapper.eq("status", "1"); // 只查询状态为启用的学生
		List<TStudent> studentList = tStudentService.list(studentWrapper);
		if (studentList.isEmpty()) {
			log.warn("班级{}下无有效学生，跳过征订/领取/账单记录生成", selection.getClassId());
			return;
		}

		// 3. 批量生成征订记录（原有逻辑，不变）
		List<TSubscription> subscriptionList = studentList.stream().map(student -> {
					// 先判断是否已存在该学生+该教材+该学年学期的征订记录（避免重复）
					QueryWrapper<TSubscription> existWrapper = new QueryWrapper<>();
					existWrapper.eq("student_id", student.getId())
							.eq("textbook_id", selection.getTextbookId())
							.eq("subscription_year", selection.getSchoolYear())
							.eq("subscription_semester", selection.getSemester());
					if (tSubscriptionService.count(existWrapper) > 0) {
						log.warn("学生{}已存在该教材的征订记录，跳过", student.getStudentId());
						return null;
					}

					// 构建征订记录
					TSubscription subscription = new TSubscription();
					subscription.setStudentId(student.getId()); // 征订表仍存学生主键ID（无需改）
					subscription.setTextbookId(selection.getTextbookId()); // 教材ID
					subscription.setSelectionId(selectionId); // 教材选用记录ID
					subscription.setMajorId(selection.getMajorId()); // 专业ID
					subscription.setSubscriptionYear(selection.getSchoolYear()); // 征订学年
					subscription.setSubscriptionSemester(selection.getSemester()); // 征订学期
					subscription.setSubscribeStatus("待确认"); // 征订状态：待确认（学生同意后才会创建领取记录）
					subscription.setRemark(""); // 备注
					subscription.setCreateTime(new Date()); // 创建时间
					subscription.setUpdateTime(new Date()); // 更新时间
					return subscription;
				}).filter(subscription -> subscription != null) // 过滤掉重复的记录
				.collect(Collectors.toList());

		// 4. 批量保存征订记录 + 生成账单记录（领取记录在学生同意征订后创建）
		if (!subscriptionList.isEmpty()) {
			tSubscriptionService.saveBatch(subscriptionList);
			log.info("为班级{}生成{}条征订记录", selection.getClassId(), subscriptionList.size());
			log.info("领取记录和个人账单将在学生同意征订后创建");
		}
	}
}
