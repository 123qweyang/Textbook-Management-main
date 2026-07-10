package org.jeecg.modules.system.util;

import org.apache.commons.lang3.StringUtils;

/**
 * XSS 防护工具类
 */
public class XssUtils {

    public static String scriptXss(String value) {
        if (StringUtils.isBlank(value)) return value;
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;")
                .replace("/", "&#x2F;");
    }

    public static String unEscape(String value) {
        if (StringUtils.isBlank(value)) return value;
        return value
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#x27;", "'")
                .replace("&#x2F;", "/")
                .replace("&amp;", "&");
    }
}
