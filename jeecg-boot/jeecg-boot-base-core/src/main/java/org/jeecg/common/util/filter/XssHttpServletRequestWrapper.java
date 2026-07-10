package org.jeecg.common.util.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.lang3.StringUtils;

public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    public XssHttpServletRequestWrapper(HttpServletRequest request) { super(request); }

    @Override
    public String getParameter(String name) { return cleanXss(super.getParameter(name)); }
    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) return null;
        String[] encoded = new String[values.length];
        for (int i = 0; i < values.length; i++) encoded[i] = cleanXss(values[i]);
        return encoded;
    }
    @Override
    public String getHeader(String name) { return cleanXss(super.getHeader(name)); }

    private String cleanXss(String value) {
        if (StringUtils.isBlank(value)) return value;
        return value.replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#x27;")
                .replace("(", "&#40;").replace(")", "&#41;");
    }
}
