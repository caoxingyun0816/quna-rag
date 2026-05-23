package com.quna.rag.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

@Component
public class FileParseUtil {
    public String parse(MultipartFile file) throws Exception {
        return parseFile(file.getInputStream(), file.getOriginalFilename());
    }
    
    public String parseFile(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file)) {
            return parseFile(fis, file.getName());
        }
    }
    
    private String parseFile(InputStream in, String filename) throws Exception {
        String name = filename.toLowerCase();
        if (name.endsWith(".pdf")) return pdf(in);
        if (name.endsWith(".docx")) return word(in);
        if (name.endsWith(".txt") || name.endsWith(".md") || name.endsWith(".adoc")) {
            return new String(in.readAllBytes());
        }
        return new String(in.readAllBytes());
    }

    private String pdf(InputStream in) throws Exception {
        PDDocument doc = PDDocument.load(in);
        String text = new PDFTextStripper().getText(doc);
        doc.close();
        return text;
    }

    private String word(InputStream in) {
        try (XWPFDocument doc = new XWPFDocument(in)) {
            StringBuilder sb = new StringBuilder();
            List<XWPFParagraph> list = doc.getParagraphs();
            for (XWPFParagraph p : list) sb.append(p.getText()).append("\n");
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}