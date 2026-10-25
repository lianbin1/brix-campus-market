package com.brix.campus.common;

/**
 * 业务异常：所有业务规则失败一律抛出此异常，由 {@link GlobalExceptionHandler} 统一兜底。
 *
 * <p>不使用返回值混合错误码，保证 Controller 只返回 {@code R} 成功分支。</p>
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 业务状态码（决定响应体 code） */
    private final ResultCode resultCode;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.resultCode = resultCode;
    }

    public BusinessException(ResultCode resultCode, String msg) {
        super(msg);
        this.resultCode = resultCode;
    }

    public ResultCode getResultCode() {
        return resultCode;
    }

    /* ---------------------------- 常用快捷构造 ---------------------------- */

    public static BusinessException param(String msg) {
        return new BusinessException(ResultCode.PARAM_ERROR, msg);
    }

    public static BusinessException forbidden(String msg) {
        return new BusinessException(ResultCode.FORBIDDEN, msg);
    }

    public static BusinessException unauthorized(String msg) {
        return new BusinessException(ResultCode.UNAUTHORIZED, msg);
    }

    public static BusinessException notFound(String msg) {
        return new BusinessException(ResultCode.NOT_FOUND, msg);
    }
}
