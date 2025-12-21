package com.dataelf.platform.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security配置
 * 实现以下安全功能：
 * - CORS策略配置
 * - HTTPS强制（生产环境）
 * - XSS防护（通过安全头部）
 * - SQL注入防护（通过JPA参数化查询）
 * - 安全头部配置
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;
    
    @Value("${app.security.require-https:false}")
    private boolean requireHttps;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CORS配置
            .cors().and()
            
            // CSRF禁用（因为使用JWT，不需要CSRF保护）
            .csrf().disable()
            
            // 会话管理：无状态
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            
            // 安全头部配置
            .headers()
                // XSS防护
                .xssProtection()
                    .and()
                // 内容类型嗅探防护
                .contentTypeOptions()
                    .and()
                // 点击劫持防护
                .frameOptions()
                    .deny()
            .and()
            
            // 授权配置
            .authorizeRequests()
                // 公开端点
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers("/api/ai/**").permitAll()
                .antMatchers("/api/public/**").permitAll()
                .antMatchers("/api/categories/**").permitAll()
                .antMatchers("/api/admin/**").permitAll() // TODO: Add proper admin authentication
                .antMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .antMatchers("/actuator/**").permitAll()
                .antMatchers("/robots.txt", "/static/**", "/uploads/**").permitAll()
                // 需要认证的端点
                .antMatchers("/api/user/**").authenticated()
                .antMatchers("/api/notifications/**").authenticated()
                .antMatchers("/api/content/**").authenticated()
                .antMatchers("/api/templates/**").authenticated()
                .antMatchers("/api/interactions/**").authenticated()
                .antMatchers("/api/share/**").authenticated()
                .antMatchers("/api/comments/**").authenticated()
                .antMatchers("/api/upload/**").authenticated()
                // 其他端点需要认证
                .anyRequest().authenticated()
            .and()
            
            // JWT认证过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        // HTTPS强制（生产环境）
        if (requireHttps) {
            http.requiresChannel()
                .anyRequest()
                .requiresSecure();
        }
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 从配置文件读取允许的源
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);
        
        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // 暴露的响应头
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials",
            "Authorization"
        ));
        
        // 允许凭证
        configuration.setAllowCredentials(true);
        
        // 预检请求缓存时间（1小时）
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
