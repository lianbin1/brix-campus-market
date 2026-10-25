package com.brix.campus.service.impl;

import com.brix.campus.common.BusinessException;
import com.brix.campus.config.WebMvcConfig;
import com.brix.campus.model.vo.UploadVO;
import com.brix.campus.service.FileStorageService;
import com.brix.campus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;

/**
 * 本地文件存储实现。
 *
 * <p>校验顺序（架构 5.9）：扩展名白名单 → Content-Type 白名单 → 大小 ≤ 5MB → 落盘。</p>
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageServiceImpl.class);

    /** 允许的业务类型 */
    private static final Set<String> BIZ_TYPES = Set.of("item", "avatar", "review");

    private final WebMvcConfig webMvcConfig;
    private final long maxSize;

    public FileStorageServiceImpl(WebMvcConfig webMvcConfig,
                                  @Value("${brix.upload.max-size:5242880}") long maxSize) {
        this.webMvcConfig = webMvcConfig;
        this.maxSize = maxSize;
    }

    @Override
    public UploadVO store(MultipartFile file, String bizType) {
        if (file == null || file.isEmpty()) {
            throw BusinessException.param("请选择要上传的图片");
        }
        String type = (bizType == null || bizType.isBlank()) ? "item" : bizType.trim();
        if (!BIZ_TYPES.contains(type)) {
            throw BusinessException.param("不支持的业务类型");
        }

        String extension = FileUtils.extensionOf(file.getOriginalFilename());
        if (!FileUtils.isAllowedExtension(extension)) {
            throw BusinessException.param("仅支持 jpg/jpeg/png/webp 格式");
        }

        String contentType = file.getContentType();
        if (contentType != null && !FileUtils.ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw BusinessException.param("仅支持 jpg/jpeg/png/webp 格式");
        }

        if (file.getSize() > maxSize) {
            throw BusinessException.param("图片大小不能超过 5MB");
        }

        String relativeUrl = FileUtils.buildRelativeUrl(type, extension);
        Path target = FileUtils.toDiskPath(webMvcConfig.resolveUploadRoot(), relativeUrl);
        try {
            FileUtils.createParentDirs(target);
            try (var inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error("保存上传文件失败：{}", target, e);
            throw BusinessException.param("图片上传失败，请重试");
        }

        UploadVO vo = new UploadVO();
        vo.setUrl(relativeUrl);
        vo.setSize(file.getSize());
        vo.setContentType(contentType);
        return vo;
    }

    @Override
    public void deleteByUrl(String url) {
        if (url == null || url.isBlank() || !url.startsWith("/uploads/")) {
            return;
        }
        Path target = FileUtils.toDiskPath(webMvcConfig.resolveUploadRoot(), url);
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("删除物理文件失败：{}", target, e);
        }
    }
}
