package com.quna.rag.springrag.parser;

import com.quna.rag.common.QunaRuntimeException;
import com.quna.rag.springrag.model.ParsedDocument;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.extractor.POITextExtractor;
import org.apache.tika.exception.TikaException;
import org.apache.tika.extractor.EmbeddedDocumentExtractor;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.pdf.PDFParserConfig;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Tika 自动解析器，POI 自动解析器 通用解析兜底实现。
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CommonDocumentParser extends AbstractTextDocumentParser {

    @Autowired
    private TextCleaningService textCleaningService;

    private static final int MAX_TEXT_LENGTH = 50 * 1024 * 1024;

    private static final Set<String> DOC_SUFFIX = new HashSet<>(Arrays.asList(".txt",".md",".pdf"));

    private static final Set<String> FILE_SUFFIX = new HashSet<>(Arrays.asList(".docx", ".doc", ".pptx", ".ppt", ".xlsx", ".xls"));


    @Override
    public boolean supports(String filename) {
        String name = filename == null ? "" : filename.toLowerCase(Locale.ROOT);
        return FILE_SUFFIX.stream().anyMatch(name::endsWith) || DOC_SUFFIX.stream().anyMatch(name::endsWith);
    }


    @Override
    public ParsedDocument parse(MultipartFile file) throws Exception {

        String fileName = file.getOriginalFilename();
        log.info("开始解析文件: {}", fileName);
        // 处理空文件
        if (file.isEmpty() || file.getSize() == 0) {
            log.warn("文件为空: {}", fileName);
            throw new QunaRuntimeException("文件为空");
        }

        String text;
        if (DOC_SUFFIX.stream().anyMatch(fileName::endsWith)) {
            text = parseDocContent(file.getInputStream());
        } else {
            text = parsePoiDocument(file);
        }
        return fromText(file, text, false);
    }


    /**
     * 使用 POI 自动解析器解析文件
     *
     * @param file 文件对象
     * @return 解析结果
     */
    private String parsePoiDocument(MultipartFile file) {
        String text;
        try (InputStream inputStream = file.getInputStream()) {
            POITextExtractor extractor = ExtractorFactory.createExtractor(inputStream);
            String content = extractor.getText();

            String cleanedContent = textCleaningService.cleanText(content);
            log.info("文件解析成功，提取文本长度: {} 字符", cleanedContent.length());
            text = cleanedContent;
        } catch (Exception e) {
            log.error("文件解析失败: {}", e.getMessage(), e);
            text = "";
        }
        return text;
    }

    /**
     * 核心解析方法：使用显式 Parser + Context 方式解析文档
     * <p>
     * 优化点：
     * 1. 使用 BodyContentHandler 只提取正文内容
     * 2. 禁用 EmbeddedDocumentExtractor，不解析嵌入资源（图片、附件）
     * 3. 配置 PDFParserConfig，关闭图片和注释提取
     * 4. 显式指定 Parser 到 Context，增强健壮性
     *
     * @param inputStream 文件输入流
     * @return 提取的文本内容
     * @throws IOException     IO 异常
     * @throws TikaException   Tika 解析异常
     * @throws SAXException    SAX 解析异常
     */
    private String parseDocContent(InputStream inputStream) throws IOException, TikaException, SAXException {
        // 1. 创建自动检测解析器
        AutoDetectParser parser = new AutoDetectParser();

        // 2. 创建内容处理器，只接收正文，限制最大长度为 5MB
        BodyContentHandler handler = new BodyContentHandler(MAX_TEXT_LENGTH);

        // 3. 创建元数据对象
        Metadata metadata = new Metadata();

        // 4. 创建解析上下文
        ParseContext context = new ParseContext();

        // 5. 显式指定 Parser 到 Context（增强健壮性）
        context.set(Parser.class, parser);

        // 6. 禁用嵌入文档解析（关键：避免提取图片引用和临时文件路径）
        context.set(EmbeddedDocumentExtractor.class, new NoOpEmbeddedDocumentExtractor());

        // 7. PDF 专用配置：关闭图片提取，按位置排序文本
        PDFParserConfig pdfConfig = new PDFParserConfig();
        pdfConfig.setExtractInlineImages(false);
        pdfConfig.setSortByPosition(true); // 按 x/y 坐标排序文本，改善多栏布局解析顺序
        // 注意：Tika 2.9.2 中 setExtractAnnotations 方法可能不存在，关闭图片提取已足够
        context.set(PDFParserConfig.class, pdfConfig);

        // 8. 执行解析
        parser.parse(inputStream, handler, metadata, context);

        // 9. 返回提取的文本内容
        return handler.toString();
    }


}
