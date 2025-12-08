package com.cshp.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 密码编码器
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 安全规则配置 - 由于网关已经处理了认证，这里允许所有请求通过
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 设置会话策略为无状态，适合REST API
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 配置请求授权规则 - 允许所有请求，因为认证在网关层完成
                .authorizeRequests()
                    .antMatchers("/**").permitAll()
                    .anyRequest().permitAll()
                .and()
                // 禁用CSRF保护，这在使用JWT的REST API中是合理的
                .csrf().disable()
                // 禁用HTTP Basic认证
                .httpBasic().disable()
                // 禁用表单登录
                .formLogin().disable()
                // 启用CORS
                .cors();

        return http.build();
    }
}