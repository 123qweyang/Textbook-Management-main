package org.jeecg.config.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

/**
 * Swagger 文档访问权限拦截器
 * 用于拦截 Swagger 文档页面的访问，直接返回 404 错误
 *
 * @author JeecgBoot
 */
public class SwaggerAuthInterceptor implements AsyncHandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 获取当前请求的路径
        String requestUri = request.getRequestURI();

        // 检查是否访问 Swagger 相关路径
        if (requestUri.contains("/doc.html") || requestUri.contains("/swagger-ui")
                || requestUri.contains("/v3/api-docs")) {
            // 跳转到前端登录页面
            response.setStatus(302);
            response.setHeader("location", "http://localhost:3100/login");
            return false;
        }

        // 其他路径放行
        return true;
    }
}