package com.quna.rag.springrag.parser;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
/**
 * Excel 文档解析器，将工作簿中的 Sheet 和单元格内容转换为 Markdown 表格风格文本。
 */

@Component
public class ExcelDocumentParser extends AbstractTextDocumentParser {
    @Override
    public boolean supports(String filename) {
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
                    short lastCell = row.getLastCellNum();
                    for (int c = 0; c < Math.max(lastCell, 0); c++) {
                        Cell cell = row.getCell(c);
                        text.append(formatter.formatCellValue(cell)).append(" | ");
                    }
                    text.append('\n');
                }
                text.append('\n');
            }
        }
        return fromText(file, text.toString(), true);
    }
}
