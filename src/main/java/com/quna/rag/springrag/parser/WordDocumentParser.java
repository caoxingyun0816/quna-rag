package com.quna.rag.springrag.parser;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Word 文档解析器，使用 POI 提取段落和表格内容，保留表格的文本结构。
 */
@Component
public class WordDocumentParser extends AbstractTextDocumentParser {
    @Override
    public boolean supports(String filename) {
        return filename.toLowerCase().endsWith(".docx");
    }

    @Override
    public ParsedDocument parse(MultipartFile file) throws Exception {
        StringBuilder text = new StringBuilder();
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String value = paragraph.getText();
                if (value != null && !value.isBlank()) {
                    text.append(value).append('\n');
                }
            }
            for (XWPFTable table : document.getTables()) {
                appendTable(text, table);
            }
        }
        return fromText(file, text.toString(), true);
    }

    private void appendTable(StringBuilder text, XWPFTable table) {
        text.append('\n').append("表格：").append('\n');
        for (XWPFTableRow row : table.getRows()) {
            text.append("| ");
            for (XWPFTableCell cell : row.getTableCells()) {
                text.append(cell.getText().replace("\n", " ")).append(" | ");
            }
            text.append('\n');
        }
    }
}
