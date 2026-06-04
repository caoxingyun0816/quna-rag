package com.quna.rag.parser;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * PDF 文档解析器，底层复用当前 SpringRAG PDF 解析实现。
 */
@Component("standardPdfDocumentParser")
public class PdfDocumentParser implements DocumentParser {
    @Override
    public boolean supports(String filename) {
        return filename != null && filename.toLowerCase().endsWith(".pdf");
    }

    @Override
    public ParsedDocument parse(MultipartFile file) throws Exception {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            String text = new PDFTextStripper().getText(document);
            return new ParsedDocument(file.getOriginalFilename(), "pdf", normalize(text), false);
        }
    }

    private String normalize(String text) {
        return text == null ? "" : text.replaceAll("\\r\\n?", "\n").replaceAll("\\n{3,}", "\n\n").trim();
    }
}
