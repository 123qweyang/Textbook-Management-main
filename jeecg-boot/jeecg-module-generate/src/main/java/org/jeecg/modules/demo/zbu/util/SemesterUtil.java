package org.jeecg.modules.demo.zbu.util;

/**
 * 学期统一工具类
 * 存储统一使用字典码 "1"/"2"；展示与导出统一显示为 "第一学期"/"第二学期"。
 *
 * <pre>
 * 导入/录入：normalizeCode("1"|"一"|"第一学期") = "1"，normalizeCode("2"|"二"|"第二学期") = "2"
 * 展示/导出：toLabel("1") = "第一学期"，toLabel("2") = "第二学期"
 * </pre>
 */
public class SemesterUtil {

    private SemesterUtil() {
    }

    /**
     * 归一化为字典码：支持 "1"/"一"/"第一学期" → "1"；"2"/"二"/"第二学期" → "2"。
     * 无法识别的内容原样返回（去空格）。
     */
    public static String normalizeCode(String input) {
        if (input == null) {
            return null;
        }
        String s = input.trim();
        if ("1".equals(s) || "一".equals(s) || "第一学期".equals(s)) {
            return "1";
        }
        if ("2".equals(s) || "二".equals(s) || "第二学期".equals(s)) {
            return "2";
        }
        return s;
    }

    /**
     * 转换为中文标签："1" → "第一学期"；"2" → "第二学期"。无法识别的内容原样返回（去空格）。
     */
    public static String toLabel(String code) {
        if (code == null) {
            return "";
        }
        String s = code.trim();
        if ("1".equals(s)) {
            return "第一学期";
        }
        if ("2".equals(s)) {
            return "第二学期";
        }
        return s;
    }
}