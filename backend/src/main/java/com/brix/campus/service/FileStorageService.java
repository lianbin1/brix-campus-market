package com.brix.campus.service;

import com.brix.campus.model.vo.UploadVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 本地文件存储服务（不使用对象存储，PRD N07）。
 */
public interface FileStorageService {

    /**
     * 保存图片并返回访问信息。
     *
     * @param file    上传文件
     * @param bizType 业务类型：item / avatar / review
     * @return 上传结果（含可直接访问的 URL）
     */
    UploadVO store(MultipartFile file, String bizType);

    /**
     * 删除某一 URL 对应的物理文件（本期未在业务中调用，预留给换图/删商品的二期清理）。
     *
     * @param url 相对 URL
     */
    void deleteByUrl(String url);
}
