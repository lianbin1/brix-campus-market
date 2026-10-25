package com.brix.campus.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 文件工具：扩展名白名单、路径生成、目录创建。
 *
 * <p>命名一律使用 UUID，绝不使用原文件名，防路径穿越（架构 5.9）。</p>
 */
public final class FileUtils {

    /** 允许的图片扩展名 */
    public static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    /** 允许的图片 Content-Type */
    public static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    /** 年月目录格式 */
    private static final DateTimeFormatter YEAR_MONTH = DateTimeFormatter.ofPattern("yyyyMM");

    private FileUtils() {
    }

    /**
     * 取小写扩展名，无扩展名返回空串。
     *
     * @param fileName 原文件名
     * @return 扩展名（不含点）
     */
    public static String extensionOf(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /**
     * 校验扩展名是否在白名单内。
     *
     * @param extension 扩展名
     * @return 是否允许
     */
    public static boolean isAllowedExtension(String extension) {
        return extension != null && ALLOWED_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT));
    }

    /**
     * 生成随机文件名。
     *
     * @param extension 扩展名
     * @return uuid.ext
     */
    public static String randomFileName(String extension) {
        return UUID.randomUUID().toString().replace("-", "") + "." + extension;
    }

    /**
     * 生成相对访问路径：{@code /uploads/{bizType}/{yyyyMM}/{uuid}.{ext}}。
     *
     * @param bizType   业务类型：item / avatar / review
     * @param extension 扩展名
     * @return 相对 URL
     */
    public static String buildRelativeUrl(String bizType, String extension) {
        return "/uploads/" + bizType + "/" + LocalDate.now().format(YEAR_MONTH) + "/" + randomFileName(extension);
    }

    /**
     * 把相对 URL 转成磁盘路径。
     *
     * @param uploadRoot 上传根目录
     * @param relativeUrl 相对 URL（以 /uploads/ 开头）
     * @return 磁盘路径
     */
    public static Path toDiskPath(Path uploadRoot, String relativeUrl) {
        String suffix = relativeUrl.startsWith("/uploads/")
                ? relativeUrl.substring("/uploads/".length())
                : relativeUrl;
        return uploadRoot.resolve(suffix).normalize();
    }

    /**
     * 创建父目录。
     *
     * @param file 目标文件路径
     * @throws IOException 创建失败
     */
    public static void createParentDirs(Path file) throws IOException {
        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }
}
