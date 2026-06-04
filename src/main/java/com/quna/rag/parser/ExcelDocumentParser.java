package com.quna.rag.parser;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Excel 文档解析器，将 Sheet 内容转换为可检索文本。
 */
@Component("standardExcelDocumentParser")
public class ExcelDocumentParser implements DocumentParser {
    @Override
    public boolean supports(String filename) {
        if (filename == null) {
            return false;
        }
        String name = filename.toLowerCase();
        return name.endsWith(".xlsx") || name.endsWith(".xls");
    }

    @Override
    public ParsedDocument parse(MultipartFile file) throws Exception {
        StringBuilder text = new StringBuilder();
        DataFormatter formatter = new DataFormatter();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                text.append("# Sheet: ").append(sheet.getSheetName()).append('\n');
                for (Row row : sheet) {
                    text.append("| ");
                    int lastCell = Math.max(row.getLastCellNum(), 0);
                    for (int c = 0; c < lastCell; c++) {
                        text.append(formatter.formatCellValue(row.getCell(c))).append(" | ");
                    }
                    text.append('\n');
                }
                text.append('\n');
            }
        }
        return new ParsedDocument(file.getOriginalFilename(), extension(file.getOriginalFilename()), text.toString().trim(), true);
    }

    private String extension(String filename) {
        int index = filename == null ? -1 : filename.lastIndexOf('.');
        return index < 0 ? "xlsx" : filename.substring(index + 1).toLowerCase();
    }
}
