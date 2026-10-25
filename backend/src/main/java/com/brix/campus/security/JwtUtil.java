package com.brix.campus.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具：签发与解析。
 *
 * <p>载荷只放 {@code sub}(userId) / {@code role} / {@code nickname} / {@code iat} / {@code exp}，
 * 绝不放密码与手机号。有效期 24 小时（PRD N03、架构 5.7）。</p>
 */
@Component
public class JwtUtil {

    /** 角色声明键 */
    private static final String CLAIM_ROLE = "role";
    /** 昵称声明键 */
    private static final String CLAIM_NICKNAME = "nickname";

    private final SecretKey secretKey;
    private final long expireMillis;

    public JwtUtil(@Value("${brix.jwt.secret}") String secret,
                   @Value("${brix.jwt.expire-hours:24}") long expireHours) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expireMillis = expireHours * 60L * 60L * 1000L;
    }

    /**
     * 签发 token。
     *
     * @param userId   用户 ID
     * @param role     角色码
     * @param nickname 昵称
     * @return 紧凑格式 JWT
     */
    public String generate(Long userId, String role, String nickname) {
        Date now = new Date();
        Date expireAt = new Date(now.getTime() + expireMillis);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_ROLE, role)
                .claim(CLAIM_NICKNAME, nickname)
                .issuedAt(now)
                .expiration(expireAt)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 解析 token，失败抛 {@link JwtException}（含过期、签名错、格式错）。
     *
     * @param token JWT 字符串
     * @return 载荷
     */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 解析并取 userId，任何异常返回 null（供 WS 握手与可选登录场景使用）。
     *
     * @param token JWT 字符串
     * @return userId 或 null
     */
    public Long parseUserIdQuietly(String token) {
        try {
            Claims claims = parse(token);
            return Long.valueOf(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 从载荷取角色码。
     *
     * @param claims 载荷
     * @return 角色码
     */
    public String roleOf(Claims claims) {
        Object role = claims.get(CLAIM_ROLE);
        return role == null ? null : role.toString();
    }

    /**
     * 从载荷取昵称。
     *
     * @param claims 载荷
     * @return 昵称
     */
    public String nicknameOf(Claims claims) {
        Object nickname = claims.get(CLAIM_NICKNAME);
        return nickname == null ? null : nickname.toString();
    }
}
