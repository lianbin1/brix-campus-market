package com.brix.campus.model.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 强制下架入参（K10）：原因可选，最长 200 字。
 */
@Data
public class OfflineDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 下架原因 */
    @Size(max = 200, message = "下架原因不能超过 200 个字")
    private String reason;
}
