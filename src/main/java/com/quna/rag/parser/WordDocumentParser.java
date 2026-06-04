package com.quna.rag.parser;

import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Word 文档解析器，底层复用当前 SpringRAG Word 解析实现。
 */
@Component("standardWordDocumentParser")
public class WordDocumentParser implements DocumentParser {
    @Override
    public boolean supports(String filename) {
        return filename != null && filename.toLowerCase().endsWith(".docx");
    }

    @Override
    public ParsedDocument parse(MultipartFile file) throws Exception {
        StringBuilder text = new StringBuilder();
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                if (paragraph.getText() != null && !paragraph.getText().isBlank()) {
                    text.append(paragraph.getText()).append('\n');
                }
            }
            for (XWPFTable table : document.getTables()) {
                text.append('\n');
                for (XWPFTableRow row : table.getRows()) {
                    text.append("| ");
                    for (XWPFTableCell cell : row.getTableCells()) {
                        text.append(cell.getText().replace("\n", " ")).append(" | ");
                    }
                    text.append('\n');
                }
            }
        }
        return new ParsedDocument(file.getOriginalFilename(), "docx", normalize(text.toString()), true);
    }

    private String normalize(String text) {
        return text == null ? "" : text.replaceAll("\\r\\n?", "\n").replaceAll("\\n{3,}", "\n\n").trim();
    }
}
