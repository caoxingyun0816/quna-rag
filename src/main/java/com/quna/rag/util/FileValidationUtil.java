package com.quna.rag.util;

import com.quna.rag.common.QunaExCode;
import com.quna.rag.common.QunaRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * 文件验证服务
 * 提供通用的文件验证功能
 */
@Slf4j
@Service
public class FileValidationUtil {

    private static final Set<String> FILE_SUFFIX = new HashSet<>(Arrays.asList(".txt",".md",".pdf", ".docx", ".doc", ".pptx", ".ppt", ".xlsx", ".xls"));

    public static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

    /**
     * 验证文件基本属性（是否为空、文件大小, 文件类型）
     *
     * @param file 上传的文件
     * @param maxSizeBytes 最大文件大小（字节）
     * @param fileTypeName 文件类型名称（用于错误消息，如"简历"、"知识库"）
     */
    public static void validateFile(MultipartFile file, long maxSizeBytes, String fileTypeName) {
        if (file.isEmpty()) {
            throw new QunaRuntimeException(QunaExCode.ILLEGAL_REQ_PARAM,
                String.format("请选择要上传的%s文件", fileTypeName));
        }
        
        if (file.getSize() > maxSizeBytes) {
            throw new QunaRuntimeException(QunaExCode.ILLEGAL_REQ_PARAM, "文件大小超过限制");
        }

        validateFileType(file.getOriginalFilename());
    }
    
    /**
     * 验证文件类型（基于MIME类型）
     *
     * @param contentType 文件的MIME类型
     * @param allowedTypes 允许的MIME类型列表（支持部分匹配，如"pdf"会匹配"application/pdf"）
     * @param errorMessage 验证失败时的错误消息
     */
    public void validateContentTypeByList(String contentType, List<String> allowedTypes, String errorMessage) {
        if (!isAllowedType(contentType, allowedTypes)) {
            throw new QunaRuntimeException(QunaExCode.ILLEGAL_REQ_PARAM,
                    errorMessage != null ? errorMessage : "不支持的文件类型: " + contentType);
        }
    }
    
    /**
     * 验证文件类型（基于MIME类型和文件扩展名）
     *
     * @param contentType 文件的MIME类型
     * @param fileName 文件名（用于扩展名检查）
     * @param mimeTypeChecker MIME类型检查器
     * @param extensionChecker 文件扩展名检查器
     * @param errorMessage 验证失败时的错误消息
     */
    public void validateContentType(String contentType, String fileName,
                                   Predicate<String> mimeTypeChecker,
                                   Predicate<String> extensionChecker,
                                   String errorMessage) {
        // 先检查MIME类型
        if (mimeTypeChecker.test(contentType)) {
            return;
        }
        
        // 如果MIME类型不支持，再检查文件扩展名
        if (fileName != null && extensionChecker.test(fileName)) {
            return;
        }

        throw new QunaRuntimeException(QunaExCode.ILLEGAL_REQ_PARAM,
                errorMessage != null ? errorMessage : "不支持的文件类型: " + contentType);
    }
    
    /**
     * 检查文件类型是否在允许列表中
     */
    private boolean isAllowedType(String contentType, List<String> allowedTypes) {
        if (contentType == null || allowedTypes == null || allowedTypes.isEmpty()) {
            return false;
        }
        
        String lowerContentType = contentType.toLowerCase();
        return allowedTypes.stream()
            .anyMatch(allowed -> {
                String lowerAllowed = allowed.toLowerCase();
                return lowerContentType.contains(lowerAllowed) || lowerAllowed.contains(lowerContentType);
            });
    }
    
    /**
     * 检查文件扩展名是否为Markdown格式
     */
    public boolean isMarkdownExtension(String fileName) {
        if (fileName == null) {
            return false;
        }
        
        String lowerFileName = fileName.toLowerCase();
        return lowerFileName.endsWith(".md") ||
               lowerFileName.endsWith(".markdown") ||
               lowerFileName.endsWith(".mdown");
    }
    
    /**
     * 检查MIME类型是否为知识库支持的格式
     */
    public boolean isKnowledgeBaseMimeType(String contentType) {
        if (contentType == null) {
            return false;
        }
        
        String lowerContentType = contentType.toLowerCase();
        return lowerContentType.contains("pdf") ||
               lowerContentType.contains("msword") ||
               lowerContentType.contains("wordprocessingml") ||
               lowerContentType.contains("text/plain") ||
               lowerContentType.contains("text/markdown") ||
               lowerContentType.contains("text/x-markdown") ||
               lowerContentType.contains("text/x-web-markdown") ||
               lowerContentType.contains("application/rtf");
    }

    /**
     * 验证文件类型
     */
    public static void validateFileType(String fileName) {
        String suffix = extension(fileName);

        if (!FILE_SUFFIX.contains(suffix)) {
            throw new QunaRuntimeException(QunaExCode.ILLEGAL_REQ_PARAM,
                    String.format(String.format("不支持的文件类型[%s]，仅支持：%s", suffix, FILE_SUFFIX)));
        }
    }

    public static String extension(String filename) {
        int index = filename == null ? -1 : filename.lastIndexOf('.');
        return index < 0 ? "" : filename.substring(index).toLowerCase();
    }

}

