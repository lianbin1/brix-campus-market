package com.brix.campus.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 上传结果出参（N06、N07）。
 */
@Data
public class UploadVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 可访问的图片 URL（/uploads/{bizType}/{yyyyMM}/{uuid}.{ext}） */
    private String url;

    /** 文件字节数 */
    private Long size;

    /** 文件类型 */
    private String contentType;
}
