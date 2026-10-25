package com.brix.campus.config;

import com.brix.campus.security.JwtAuthFilter;
import com.brix.campus.security.RestAccessDeniedHandler;
import com.brix.campus.security.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security 配置（Boot 3 / Security 6 写法，无 WebSecurityConfigurerAdapter）。
 *
 * <p>权限分三层（架构 5.6）：URL 层（本类）→ 数据归属层（Service）→ 前端路由守卫（仅体验）。</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          RestAuthenticationEntryPoint authenticationEntryPoint,
                          RestAccessDeniedHandler accessDeniedHandler,
                          CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    /** 密码编码器：库中密码全部为 BCrypt 密文（PRD N04） */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .exceptionHandling(handler -> handler
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(registry -> registry
                        // 1. 完全公开的入口
                        .requestMatchers("/auth/register", "/auth/login", "/admin/login").permitAll()
                        .requestMatchers("/uploads/**", "/ws/**", "/error").permitAll()
                        // 2. 公开的只读接口（注意 /items/mine 必须先于 /items/** 声明）
                        .requestMatchers(HttpMethod.GET, "/categories").permitAll()
                        .requestMatchers(HttpMethod.GET, "/items/mine").authenticated()
                        .requestMatchers(HttpMethod.GET, "/items/*/comments").permitAll()
                        .requestMatchers(HttpMethod.GET, "/items/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/*/reviews").permitAll()
                        // 3. 后台细粒度权限：运营 + 审核员均可查看的只读取证接口
                        //    注意：Spring Security 按声明顺序「先匹配先生效」，这两条必须早于
                        //    /admin/orders/** 等运营专属规则，否则审核员会被前者拦掉（K13）。
                        .requestMatchers(HttpMethod.GET, "/admin/orders/*/chat").hasAnyRole("REVIEWER", "OPERATOR")
                        .requestMatchers(HttpMethod.GET, "/admin/chats").hasAnyRole("REVIEWER", "OPERATOR")
                        // 3.1 后台细粒度权限：运营专属
                        .requestMatchers("/admin/dashboard/**", "/admin/users/**",
                                "/admin/orders/**", "/admin/categories/**").hasRole("OPERATOR")
                        // 4. 后台细粒度权限：审核员专属
                        .requestMatchers("/admin/items/**", "/admin/reports/**").hasRole("REVIEWER")
                        // 5. 其余后台接口：任一后台角色
                        .requestMatchers("/admin/**").hasAnyRole("REVIEWER", "OPERATOR")
                        // 6. 其余全部需要登录
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
