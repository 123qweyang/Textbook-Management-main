package org.jeecg.common.util.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@Order(1)
public class XssFilter implements Filter {

    private static final List<String> EXCLUDE_URLS = Arrays.asList(
            "/sys/login", "/sys/mLogin", "/sys/phoneLogin", "/sys/cas/client/validateLogin"
    );

    @Override
    public void init(FilterConfig filterConfig) { log.info("XssFilter initialized"); }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        for (String exclude : EXCLUDE_URLS) {
            if (httpRequest.getRequestURI().contains(exclude)) { chain.doFilter(request, response); return; }
        }
        chain.doFilter(new XssHttpServletRequestWrapper(httpRequest), response);
    }

    @Override
    public void destroy() { log.info("XssFilter destroyed"); }
}
