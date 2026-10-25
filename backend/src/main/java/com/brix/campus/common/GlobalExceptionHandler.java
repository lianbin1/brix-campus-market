package com.brix.campus.common;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器。
 *
 * <p>关键语义：返回的是 {@link R} 对象，Spring MVC 默认以 <b>HTTP 200</b> 输出，
 * 因此业务失败时 HTTP 状态码依旧是 200，前端必须先判 {@code code} 字段（架构 5.3）。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 业务异常：code 取枚举，文案取异常 message */
    @ExceptionHandler(BusinessException.class)
    public R<Void> handleBusiness(BusinessException e) {
        log.info("业务异常：code={}, msg={}", e.getResultCode().getCode(), e.getMessage());
        return R.fail(e.getResultCode(), e.getMessage());
    }

    /** 请求体字段校验失败（@Valid / @Validated 方法级） */
    @ExceptionHandler(BindException.class)
    public R<Void> handleBind(BindException e) {
        String msg = ResultCode.PARAM_ERROR.getMsg();
        ObjectError first = e.getBindingResult().getAllErrors().stream().findFirst().orElse(null);
        if (first != null && first.getDefaultMessage() != null && !first.getDefaultMessage().isEmpty()) {
            msg = first.getDefaultMessage();
        } else if (first instanceof FieldError fieldError) {
            msg = fieldError.getField() + "不合法";
        }
        return R.fail(ResultCode.PARAM_ERROR, msg);
    }

    /** 路径变量 / 查询参数校验失败（类上 @Validated + @Min / @NotBlank） */
    @ExceptionHandler(ConstraintViolationException.class)
    public R<Void> handleConstraint(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .findFirst()
                .map(jakarta.validation.ConstraintViolation::getMessage)
                .orElse(ResultCode.PARAM_ERROR.getMsg());
        return R.fail(ResultCode.PARAM_ERROR, msg);
    }

    /** 路径参数类型不匹配（如 id 传 abc） */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public R<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return R.fail(ResultCode.PARAM_ERROR, "参数格式错误：" + e.getName());
    }

    /** 缺少必填查询参数 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public R<Void> handleMissingParam(MissingServletRequestParameterException e) {
        return R.fail(ResultCode.PARAM_ERROR, "缺少必要参数：" + e.getParameterName());
    }

    /** 请求体 JSON 解析失败 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R<Void> handleNotReadable(HttpMessageNotReadableException e) {
        return R.fail(ResultCode.PARAM_ERROR, "请求体格式错误");
    }

    /** 上传文件超出大小限制 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public R<Void> handleUploadSize(MaxUploadSizeExceededException e) {
        return R.fail(ResultCode.PARAM_ERROR, "图片大小不能超过 5MB");
    }

    /** 访问不存在的路径 */
    @ExceptionHandler(NoResourceFoundException.class)
    public R<Void> handleNoResource(NoResourceFoundException e) {
        return R.fail(ResultCode.NOT_FOUND);
    }

    /** 请求方法不支持 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return R.fail(ResultCode.METHOD_NOT_ALLOWED);
    }

    /** 过滤器链内的权限拒绝（未进入 Security 的 AccessDeniedHandler 时走这里） */
    @ExceptionHandler(AccessDeniedException.class)
    public R<Void> handleAccessDenied(AccessDeniedException e) {
        return R.fail(ResultCode.FORBIDDEN);
    }

    /** 唯一索引冲突：兜底为友好的重复提示 */
    @ExceptionHandler(DuplicateKeyException.class)
    public R<Void> handleDuplicateKey(DuplicateKeyException e) {
        log.warn("唯一索引冲突：{}", e.getMessage());
        return R.fail(ResultCode.PARAM_ERROR, "数据已存在，请勿重复操作");
    }

    /** 兜底：不向响应暴露堆栈 */
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("未捕获异常", e);
        return R.fail(ResultCode.SYSTEM_ERROR);
    }
}
