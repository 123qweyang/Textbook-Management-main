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

/**
 * 导入错误导出工具：将错误信息生成 Excel 文件写入 upload 目录，返回下载 URL
 */
@Slf4j
public class ImportErrorExportUtil {

    private static final String ERROR_DIR = "import_error";

    /**
     * 生成错误 Excel 并返回带有 code=201 的 Result
     *
     * @param errorMessages  错误信息列表（如 "第2行，学号为空，请检查。"）
     * @param successMsg     成功摘要（如 "成功导入10条有效数据"）
     * @param uploadPath     上传根目录（取自配置 jeecg.path.upload）
     * @param tableName      表名（用于生成文件名）
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
            String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String timeStr = new SimpleDateFormat("HHmmss").format(new Date());
            String dirPath = uploadPath + File.separator + ERROR_DIR + File.separator + dateStr;
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = tableName + "_导入错误_" + dateStr + "_" + timeStr + ".xlsx";
            String filePath = dirPath + File.separator + fileName;

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

                // 单列表头
                Row headerRow = sheet.createRow(0);
                Cell headerCell = headerRow.createCell(0);
                headerCell.setCellValue("错误原因");
                headerCell.setCellStyle(headerStyle);

                // 数据样式
                CellStyle dataStyle = wb.createCellStyle();
                dataStyle.setBorderBottom(BorderStyle.THIN);
                dataStyle.setBorderTop(BorderStyle.THIN);
                dataStyle.setBorderLeft(BorderStyle.THIN);
                dataStyle.setBorderRight(BorderStyle.THIN);

                // 填充数据（完整错误信息，已包含行号）
                for (int i = 0; i < errorMessages.size(); i++) {
                    Row row = sheet.createRow(i + 1);
                    Cell cell = row.createCell(0);
                    cell.setCellValue(errorMessages.get(i));
                    cell.setCellStyle(dataStyle);
                }

                sheet.setColumnWidth(0, 20000);

                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    wb.write(fos);
                }
            }

            String relativeUrl = "/sys/common/static/" + ERROR_DIR + "/" + dateStr + "/" + fileName;

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
