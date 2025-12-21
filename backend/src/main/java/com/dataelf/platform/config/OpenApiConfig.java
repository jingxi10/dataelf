package com.dataelf.platform.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger配置
 * 提供完整的API文档，包括认证要求和接口描述
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("数流精灵 API 文档")
                        .version("1.0.0")
                        .description("""
                                # 数流精灵 - AI优化的结构化数据平台
                                
                                ## 概述
                                数流精灵是一个专为AI优化的结构化数据平台，通过模板化编辑器将任意内容转化为AI易于理解和引用的标准化格式。
                                
                                ## API分类
                                
                                ### AI API (/api/ai/*)
                                - **无需认证**
                                - 提供纯净的结构化数据
                                - 专为AI系统优化
                                - 返回JSON-LD格式数据
                                
                                ### 用户API (/api/user/*)
                                - **需要JWT认证**
                                - 提供完整的用户交互功能
                                - 包含个性化内容和操作
                                
                                ### 管理员API (/api/admin/*)
                                - **需要JWT认证 + 管理员权限**
                                - 用户审核和管理
                                - 内容审核和管理
                                - 模板管理
                                
                                ### 公共API (/api/public/*)
                                - **无需认证**
                                - 公开内容浏览
                                - 分类和标签查询
                                
                                ## 认证说明
                                
                                需要认证的接口需要在请求头中包含JWT令牌：
                                ```
                                Authorization: Bearer <your-jwt-token>
                                ```
                                
                                获取令牌：
                                1. 使用 POST /api/auth/login 登录
                                2. 从响应中获取 token
                                3. 在后续请求中使用该 token
                                
                                ## 数据格式
                                
                                所有请求和响应均使用JSON格式，Content-Type为 application/json
                                
                                ## 错误响应
                                
                                统一错误响应格式：
                                ```json
                                {
                                  "success": false,
                                  "error": {
                                    "code": "ERROR_CODE",
                                    "message": "错误描述",
                                    "details": []
                                  },
                                  "timestamp": "2024-01-01T12:00:00Z"
                                }
                                ```
                                """)
                        .contact(new Contact()
                                .name("数流精灵团队")
                                .email("support@dataelf.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("本地开发环境"),
                        new Server()
                                .url("https://api.dataelf.com")
                                .description("生产环境")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT认证令牌。格式: Bearer <token>")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
