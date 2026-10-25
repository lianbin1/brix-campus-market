package com.brix.campus.common;

/**
 * 统一响应码枚举 —— 全项目 code 的唯一来源，业务代码禁止出现魔法数字。
 */
public enum ResultCode {

    /** 成功 */
    SUCCESS(200, "操作成功"),
    /** 参数校验失败 / 业务规则不满足 */
    PARAM_ERROR(400, "请求参数错误"),
    /** 未认证或登录已过期 */
    UNAUTHORIZED(401, "未认证或登录已过期"),
    /** 已认证但无权限 */
    FORBIDDEN(403, "没有访问权限"),
    /** 资源不存在 */
    NOT_FOUND(404, "请求资源不存在"),
    /** 请求方法不支持 */
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    /** 限流（AI 每日 20 次） */
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    /** 业务处理失败 */
    BIZ_ERROR(500, "业务处理失败"),
    /** 系统异常 */
    SYSTEM_ERROR(500, "系统繁忙，请稍后再试");

    private final int code;
    private final String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
