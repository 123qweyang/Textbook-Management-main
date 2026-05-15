package org.jeecg.modules.demo.zbu.vo;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jeecg.common.api.vo.Result;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 导入错误导出工具：将错误信息生成 Excel 文件写入 upload 目录，返回下载 URL
 */
@Slf4j
public class ImportErrorExportUtil {

    private static final String ERROR_DIR = "import_error";
    private static final Pattern ROW_PATTERN = Pattern.compile("^第(\\d+)行[：:]");

    /**
     * 生成错误 Excel 并返回带有 code=201 的 Result
     *
     * @param errorMessages  错误信息列表（如 "第2行：学号为空"）
     * @param successMsg     成功摘要（如 "成功导入10条有效数据"）
     * @param uploadPath     上传根目录（取自配置 jeecg.path.upload）
     * @return Result (code=201) 包含 fileUrl 和 fileName
     */
    public static Result<?> buildErrorResult(
            List<String> errorMessages,
            String successMsg,
            String uploadPath,
            String tableName) {

        if (errorMessages == null || errorMessages.isEmpty()) {
            return Result.OK(successMsg);
        }

        try {
            // 生成带日期的子目录
            String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String timeStr = new SimpleDateFormat("HHmmss").format(new Date());
            String dirPath = uploadPath + File.separator + ERROR_DIR + File.separator + dateStr;
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成含表名和时间的文件名
            String fileName = tableName + "_导入错误_" + dateStr + "_" + timeStr + ".xlsx";
            String filePath = dirPath + File.separator + fileName;

            // 创建 Excel
            try (Workbook wb = new XSSFWorkbook()) {
                Sheet sheet = wb.createSheet("导入错误详情");

                // 表头样式
                CellStyle headerStyle = wb.createCellStyle();
                Font headerFont = wb.createFont();
                headerFont.setBold(true);
                headerFont.setFontHeightInPoints((short) 11);
                headerStyle.setFont(headerFont);
                headerStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                headerStyle.setBorderBottom(BorderStyle.THIN);
                headerStyle.setBorderTop(BorderStyle.THIN);
                headerStyle.setBorderLeft(BorderStyle.THIN);
                headerStyle.setBorderRight(BorderStyle.THIN);
                headerStyle.setAlignment(HorizontalAlignment.CENTER);

                // 表头
                Row headerRow = sheet.createRow(0);
                String[] headers = {"行号", "错误描述"};
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                // 数据样式
                CellStyle dataStyle = wb.createCellStyle();
                dataStyle.setBorderBottom(BorderStyle.THIN);
                dataStyle.setBorderTop(BorderStyle.THIN);
                dataStyle.setBorderLeft(BorderStyle.THIN);
                dataStyle.setBorderRight(BorderStyle.THIN);

                // 填充数据
                for (int i = 0; i < errorMessages.size(); i++) {
                    Row row = sheet.createRow(i + 1);
                    String msg = errorMessages.get(i);

                    // 尝试从消息中解析行号
                    int rowNum = 0;
                    Matcher m = ROW_PATTERN.matcher(msg);
                    if (m.find()) {
                        try {
                            rowNum = Integer.parseInt(m.group(1));
                        } catch (NumberFormatException ignored) {}
                    }

                    Cell c0 = row.createCell(0);
                    c0.setCellValue(rowNum);
                    c0.setCellStyle(dataStyle);

                    Cell c1 = row.createCell(1);
                    c1.setCellValue(msg);
                    c1.setCellStyle(dataStyle);
                }

                // 自动列宽
                sheet.setColumnWidth(0, 3000);
                sheet.setColumnWidth(1, 18000);

                // 写入文件
                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    wb.write(fos);
                }
            }

            // 构造下载 URL（相对于 upload 根目录）
            String relativeUrl = "/sys/common/static/" + ERROR_DIR + "/" + dateStr + "/" + fileName;

            // 统计错误数
            String msg = successMsg + "；失败" + errorMessages.size() + "条（错误详情已自动下载）";
            String detailMsg = successMsg + "，其中失败" + errorMessages.size() + "条。";

            ImportResultVO resultVO = new ImportResultVO(detailMsg, relativeUrl, fileName);

            Result<ImportResultVO> result = new Result<>();
            result.setSuccess(true);
            result.setCode(201);
            result.setMessage(msg);
            result.setResult(resultVO);
            return result;

        } catch (IOException e) {
            log.error("生成导入错误Excel失败", e);
            return Result.error("导入完成，但生成错误详情文件失败: " + e.getMessage());
        }
    }
}
