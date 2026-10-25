package com.brix.campus.common;

import java.io.Serializable;

/**
 * 统一响应体：{@code {code, msg, data, timestamp}}。
 *
 * <p>字段名固定为 {@code msg}（不是 message）。业务失败时 HTTP 状态码仍是 200，前端靠 {@code code} 判断。</p>
 *
 * @param <T> 业务数据类型
 */
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 业务状态码，取值见 {@link ResultCode} */
    private int code;

    /** 提示文案（成功为「操作成功」，失败为具体业务文案） */
    private String msg;

    /** 业务数据，无数据时为 null */
    private T data;

    /** 服务端时间戳（毫秒） */
    private long timestamp = System.currentTimeMillis();

    public R() {
    }

    public R(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /* ------------------------------- 静态工厂 ------------------------------- */

    public static <T> R<T> ok() {
        return new R<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), null);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data);
    }

    public static <T> R<T> ok(T data, String msg) {
        return new R<>(ResultCode.SUCCESS.getCode(), msg, data);
    }

    public static <T> R<T> fail(String msg) {
        return new R<>(ResultCode.BIZ_ERROR.getCode(), msg, null);
    }

    public static <T> R<T> fail(ResultCode resultCode) {
        return new R<>(resultCode.getCode(), resultCode.getMsg(), null);
    }

    public static <T> R<T> fail(ResultCode resultCode, String msg) {
        return new R<>(resultCode.getCode(), msg, null);
    }

    public static <T> R<T> fail(int code, String msg) {
        return new R<>(code, msg, null);
    }

    /* ------------------------------- getter/setter ------------------------------- */

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
