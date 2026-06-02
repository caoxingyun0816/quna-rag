package com.quna.rag.springrag.parser;

import com.quna.rag.springrag.model.ParsedDocument;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * PDF 文档解析器，使用 PDFBox 抽取正文文本，避免 Tika PDF 版本冲突影响上传。
 */
@Component
public class PdfDocumentParser extends AbstractTextDocumentParser {
    @Override
    public boolean supports(String filename) {
        return filename.toLowerCase().endsWith(".pdf");
    }

    @Override
    public ParsedDocument parse(MultipartFile file) throws Exception {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            String text = new PDFTextStripper().getText(document);
            return fromText(file, text, false);
        }
    }
}
