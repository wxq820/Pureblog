package com.pureblog.upload.controller;

import com.pureblog.common.context.LoginUserHolder;
import com.pureblog.common.enums.ErrorCode;
import com.pureblog.common.exception.BusinessException;
import com.pureblog.common.result.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private static final long MAX_SIZE = 5L * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_EXT = new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "gif", "webp"));
    private static final List<String> ALLOWED_MIME = Arrays.asList("image/jpeg", "image/png", "image/gif", "image/webp");
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyyMM");

    @PostMapping("/image")
    public ApiResponse<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (LoginUserHolder.getUserId() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "文件为空");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE, "文件超过 5MB");
        }

        String original = file.getOriginalFilename();
        String ext = extractExt(original);
        if (ext == null || !ALLOWED_EXT.contains(ext)) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_SUPPORTED, "仅支持 jpg/png/gif/webp");
        }
        if (file.getContentType() != null && !ALLOWED_MIME.contains(file.getContentType().toLowerCase())) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_SUPPORTED, "不支持的 MIME 类型");
        }

        String monthDir = LocalDate.now().format(MONTH_FMT);
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path target = Paths.get("uploads", monthDir, filename).toAbsolutePath().normalize();
        Path baseDir = Paths.get("uploads").toAbsolutePath().normalize();
        if (!target.startsWith(baseDir)) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "非法路径");
        }

        try {
            Files.createDirectories(target.getParent());
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Upload failed: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "保存失败");
        }

        String url = "/static/uploads/" + monthDir + "/" + filename;
        return ApiResponse.success(Map.of(
                "url", url,
                "name", original != null ? original : filename,
                "size", file.getSize()
        ));
    }

    private String extractExt(String filename) {
        if (filename == null) return null;
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) return null;
        return filename.substring(dot + 1).toLowerCase();
    }
}
