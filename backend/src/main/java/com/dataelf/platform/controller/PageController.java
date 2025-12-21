package com.dataelf.platform.controller;

import com.dataelf.platform.service.HtmlGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 页面控制器 - 处理用户页面和内容页面的HTML渲染
 * 
 * 验证需求: 6.4
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class PageController {
    
    private final HtmlGenerationService htmlGenerationService;
    
    /**
     * 用户页面 - 带robots noindex标签
     * 
     * 验证需求: 6.4
     */
    @GetMapping(value = {"/user/**", "/account/**", "/profile/**", "/settings/**"}, 
                produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getUserPage(HttpServletRequest request) {
        String path = request.getRequestURI();
        log.info("Serving user page: {}", path);
        
        // Generate HTML with robots noindex tag
        String html = htmlGenerationService.generateUserPageHtml(
            "用户页面",
            "<h1>用户页面</h1><p>此页面不对AI开放</p>",
            true // Block AI
        );
        
        return ResponseEntity.ok(html);
    }
}
