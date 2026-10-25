package com.brix.campus.util;

import java.security.SecureRandom;

/**
 * 面交确认码生成器：6 位纯数字，<b>保留前导 0</b>（返回 String，绝不转 int）。
 */
public final class TradeCodeGenerator {

    /** 6 位码的模 */
    private static final int MOD = 1000000;
    private static final SecureRandom RANDOM = new SecureRandom();

    private TradeCodeGenerator() {
    }

    /**
     * 生成 6 位确认码。
     *
     * @return 形如 "042317" 的字符串
     */
    public static String generate() {
        return String.format("%06d", RANDOM.nextInt(MOD));
    }
}
