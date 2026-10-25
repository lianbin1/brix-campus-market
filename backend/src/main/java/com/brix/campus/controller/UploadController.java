package com.brix.campus.controller;

import com.brix.campus.common.R;
import com.brix.campus.model.vo.UploadVO;
import com.brix.campus.service.FileStorageService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 图片上传接口（N06、N07）。
 */
@RestController
@Validated
public class UploadController {

    private final FileStorageService fileStorageService;

    public UploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /**
     * 上传单张图片。
     *
     * @param file    图片文件（jpg/jpeg/png/webp，≤5MB）
     * @param bizType 业务类型：item / avatar / review
     * @return 上传结果（含可访问 URL）
     */
    @PostMapping("/upload/image")
    public R<UploadVO> upload(@RequestParam("file") MultipartFile file,
                              @RequestParam(value = "bizType", required = false, defaultValue = "item")
                              String bizType) {
        return R.ok(fileStorageService.store(file, bizType), "上传成功");
    }
}
