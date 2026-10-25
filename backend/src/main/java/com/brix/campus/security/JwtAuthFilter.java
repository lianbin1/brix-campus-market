package com.brix.campus.security;

import com.brix.campus.common.R;
import com.brix.campus.common.ResultCode;
import com.brix.campus.mapper.UserMapper;
import com.brix.campus.model.User;
import com.brix.campus.model.enums.UserStatusEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 认证过滤器。
 *
 * <p>三条硬规则（架构 5.7）：</p>
 * <ol>
 *   <li>无 token → 直接放行（由 Security 的 URL 规则决定是否需要登录）；</li>
 *   <li>有 token 但解析失败 → 直写 401 并 return，<b>不能继续过滤链</b>，否则会被 permitAll 误放行；</li>
 *   <li>每次请求按主键查库，{@code status=DISABLED} → 直写 403「账号已被禁用」。</li>
 * </ol>
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ROLE_PREFIX = "ROLE_";

    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    public JwtAuthFilter(JwtUtil jwtUtil, UserMapper userMapper, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // 预检请求直接放行，交给 CORS 过滤器
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader(AUTH_HEADER);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }

        Claims claims;
        try {
            claims = jwtUtil.parse(token);
        } catch (Exception e) {
            log.debug("JWT 解析失败：{}", e.getMessage());
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, R.fail(ResultCode.UNAUTHORIZED));
            return;
        }

        Long userId;
        try {
            userId = Long.valueOf(claims.getSubject());
        } catch (Exception e) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, R.fail(ResultCode.UNAUTHORIZED));
            return;
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, R.fail(ResultCode.UNAUTHORIZED));
            return;
        }
        if (UserStatusEnum.DISABLED.getCode().equals(user.getStatus())) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN,
                    R.fail(ResultCode.FORBIDDEN, "账号已被禁用"));
            return;
        }

        LoginUser loginUser = new LoginUser(user.getId(), user.getUsername(), user.getNickname(), user.getRole());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                loginUser, null, List.of(new SimpleGrantedAuthority(ROLE_PREFIX + user.getRole())));
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        chain.doFilter(request, response);
    }

    /**
     * 直写 JSON 错误响应（业务失败仍用 HTTP 200 的规则不适用于 Spring Security 的 401/403）。
     *
     * @param response 响应
     * @param status   HTTP 状态码
     * @param body     响应体
     * @throws IOException 写流异常
     */
    private void writeError(HttpServletResponse response, int status, R<Void> body) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }
}
