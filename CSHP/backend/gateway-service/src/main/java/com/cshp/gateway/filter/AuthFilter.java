package com.cshp.gateway.filter;

import com.cshp.common.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
public class AuthFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    // 白名单路径，包含登录、注册等无需认证的接口
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/user-service/api/user/login",
            "/user-service/api/user/register",
            "/api/user-service/api/user/login",
            "/api/user-service/api/user/register",
            "/api/user/login",
            "/api/user/register"
    );

    private boolean isWhitelisted(String path) {
        return WHITE_LIST.stream().anyMatch(path::startsWith);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        System.out.println("处理请求路径: " + path);

        // 预检请求直接放行
        if (HttpMethod.OPTIONS.matches(request.getMethodValue())) {
            return chain.filter(exchange);
        }

        // 白名单直接放行 - 登录和注册接口无需Token验证
        if (isWhitelisted(path)) {
            System.out.println("路径在白名单中，直接放行: " + path);
            return chain.filter(exchange);
        }

        // 非白名单请求需要Token验证
        String token = request.getHeaders().getFirst("Authorization");
        System.out.println("Authorization头: " + token);
        
        // 检查Token是否存在
        if (!StringUtils.hasText(token)) {
            System.out.println("Token不存在，拒绝访问");
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        // 处理Token格式，支持带Bearer前缀和不带前缀的情况
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        System.out.println("处理后的Token: " + token);

        // 验证Token
        try {
            // 先获取Claims，验证token格式和有效性
            if (!jwtUtil.validateToken(token, "")) {
                System.out.println("Token格式无效或已过期，拒绝访问");
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
            
            // 获取studentId
            String studentId = jwtUtil.getStudentIdFromToken(token);
            System.out.println("从Token解析的studentId: " + studentId);
            
            // 检查是否成功获取到studentId
            if (studentId == null || studentId.isEmpty()) {
                System.out.println("无法从Token中获取有效studentId，拒绝访问");
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }

            // 将用户信息添加到请求头，转发给下游服务
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-Student-Id", studentId)
                    .header("X-User-Id", studentId) // 同时设置X-User-Id
                    .build();
            
            System.out.println("Token验证通过，转发请求");
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        } catch (Exception e) {
            System.out.println("Token验证异常: " + e.getMessage());
            e.printStackTrace(); // 打印完整堆栈以便调试
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }
}

