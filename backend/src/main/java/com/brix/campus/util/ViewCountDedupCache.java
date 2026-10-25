package com.brix.campus.util;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 浏览量去重缓存：同一访问者对同一商品在 TTL 内只计一次浏览（PRD B03）。
 *
 * <p>进程内内存实现即可满足演示需求（PRD 明确不使用 Redis）；重启后计数窗口清空，
 * 只会导致「重启后第一次访问再 +1」，不影响业务正确性。</p>
 */
@Component
public class ViewCountDedupCache {

    /** 去重窗口：10 分钟 */
    public static final Duration DEFAULT_TTL = Duration.ofMinutes(10);

    /** key → 过期时间戳（毫秒） */
    private final Map<String, Long> expireAtMap = new ConcurrentHashMap<>();

    /**
     * 尝试占用一次计数机会。
     *
     * @param viewerKey 访问者标识（登录用户取 userId，游客取 IP）
     * @param itemId    商品 ID
     * @return true 表示首次访问（应当 +1），false 表示窗口内重复访问（不再 +1）
     */
    public boolean tryAcquire(String viewerKey, Long itemId) {
        return tryAcquire(viewerKey, itemId, DEFAULT_TTL);
    }

    /**
     * 尝试占用一次计数机会（自定义窗口）。
     *
     * @param viewerKey 访问者标识
     * @param itemId    商品 ID
     * @param ttl       去重窗口
     * @return true 表示首次访问
     */
    public boolean tryAcquire(String viewerKey, Long itemId, Duration ttl) {
        if (viewerKey == null || itemId == null) {
            return true;
        }
        long now = System.currentTimeMillis();
        cleanExpired(now);
        String key = viewerKey + ":" + itemId;
        Long previous = expireAtMap.putIfAbsent(key, now + ttl.toMillis());
        if (previous == null) {
            return true;
        }
        if (previous < now) {
            // 窗口已过期，刷新过期时间并计为一次新浏览
            expireAtMap.put(key, now + ttl.toMillis());
            return true;
        }
        return false;
    }

    /**
     * 清理过期条目，避免 Map 无限增长。
     *
     * @param now 当前时间戳
     */
    private void cleanExpired(long now) {
        if (expireAtMap.size() < 512) {
            return;
        }
        expireAtMap.entrySet().removeIf(entry -> entry.getValue() < now);
    }
}
