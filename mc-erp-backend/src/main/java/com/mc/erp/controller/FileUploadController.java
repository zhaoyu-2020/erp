package com.mc.erp.controller;

import com.mc.erp.common.OperLog;
import com.mc.erp.common.Result;
import com.mc.erp.enums.OperationType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files")
public class FileUploadController {

    /** 允许的文件扩展名 */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp",
            ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".csv",
            ".zip", ".rar", ".7z"
    );

    /** 单文件最大 20MB */
    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.base-url:http://localhost:8080/uploads}")
    private String baseUrl;

    @OperLog(module = "文件管理", type = OperationType.OTHER, description = "上传文件", saveParams = false)
    @PostMapping("/upload")
    public Result<List<String>> upload(@RequestParam("files") List<MultipartFile> files) throws IOException {
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            // 校验文件大小
            if (file.getSize() > MAX_FILE_SIZE) {
                return Result.error(400, "文件 " + file.getOriginalFilename() + " 超过最大限制 20MB");
            }
            String originalName = file.getOriginalFilename();
            // Sanitize: strip any path separators from the original filename
            if (originalName != null) {
                originalName = Paths.get(originalName).getFileName().toString();
            } else {
                originalName = "file";
            }
            String ext = "";
            int dot = originalName.lastIndexOf('.');
            if (dot >= 0) {
                ext = originalName.substring(dot).toLowerCase();
            }
            // 校验文件扩展名
            if (!ext.isEmpty() && !ALLOWED_EXTENSIONS.contains(ext)) {
                return Result.error(400, "不支持的文件类型: " + ext);
            }
            String storedName = UUID.randomUUID() + ext;
            Path target = Paths.get(uploadDir, storedName).toAbsolutePath().normalize();
            // Verify target is inside upload dir (prevent path traversal)
            if (!target.startsWith(Paths.get(uploadDir).toAbsolutePath().normalize())) {
                return Result.error(400, "非法文件名");
            }
            Files.copy(file.getInputStream(), target);
            urls.add(baseUrl + "/" + storedName);
        }
        return Result.success(urls);
    }
}
