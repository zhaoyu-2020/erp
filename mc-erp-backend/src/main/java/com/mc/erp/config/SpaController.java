package com.mc.erp.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.io.InputStream;

/**
 * SPA Fallback Controller
 * 将所有非 API、非静态资源的请求直接响应 index.html 内容
 * 以支持 Vue Router 的 history 模式
 */
@Controller
public class SpaController {

    @RequestMapping(value = {"/{path:^(?!api|uploads|assets|swagger-ui|v3)[^\\.]*}/**",
            "/{path:^(?!api|uploads|assets|swagger-ui|v3)[^\\.]*}"})
    public void forward(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ClassPathResource resource = new ClassPathResource("static/index.html");
        response.setContentType(MediaType.TEXT_HTML_VALUE + ";charset=UTF-8");
        try (InputStream in = resource.getInputStream()) {
            StreamUtils.copy(in, response.getOutputStream());
        }
    }
}
