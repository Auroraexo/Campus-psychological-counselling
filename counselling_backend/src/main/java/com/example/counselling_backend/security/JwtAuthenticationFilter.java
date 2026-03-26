package com.example.counselling_backend.security;

import com.example.counselling_backend.util.JwtTokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        
        // 跳过对公开端点的JWT验证，如登录、注册等
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        // 处理OPTIONS预检请求
        if ("OPTIONS".equalsIgnoreCase(method)) {
            chain.doFilter(request, response);
            return;
        }
        
        // 确保路径检查包含/api前缀
        if (path.startsWith("/api/auth/login") || path.startsWith("/api/auth/register") || 
            path.startsWith("/api/users/register") || path.startsWith("/api/test/") || 
            path.equals("/api/error") || path.equals("/error")) {
            chain.doFilter(request, response);
            return;
        }
        
        logger.debug("JWT过滤器处理请求: " + method + " " + path);
        
        final String requestTokenHeader = request.getHeader("Authorization");
        
        String username = null;
        String jwtToken = null;
        
        // JWT Token格式为 "Bearer token"
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
            } catch (Exception e) {
                logger.error("无法获取JWT Token中的用户名", e);
            }
        }
        
        // 验证token
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                
                // 如果token有效，配置Spring Security手动设置认证
                if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = 
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
                // 用户不存在或已被禁用，返回401错误
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                try {
                    String errorMessage = e.getMessage();
                    if (errorMessage != null && errorMessage.contains("已被禁用")) {
                        response.getWriter().write("{\"error\":\"用户已被禁用，请重新登录\",\"code\":401}");
                    } else {
                        response.getWriter().write("{\"error\":\"用户不存在或已失效，请重新登录\",\"code\":401}");
                    }
                } catch (IOException ioException) {
                    logger.error("写入错误响应失败", ioException);
                }
                return; // 不继续执行过滤器链
            } catch (Exception e) {
                logger.error("JWT认证过程中发生错误", e);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                try {
                    response.getWriter().write("{\"error\":\"认证失败，请重新登录\",\"code\":401}");
                } catch (IOException ioException) {
                    logger.error("写入错误响应失败", ioException);
                }
                return; // 不继续执行过滤器链
            }
        }
        chain.doFilter(request, response);
    }
}