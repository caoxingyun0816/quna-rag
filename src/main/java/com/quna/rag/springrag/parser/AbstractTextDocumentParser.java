package com.quna.rag.springrag.parser;

import com.quna.rag.springrag.model.ParsedDocument;
import com.quna.rag.springrag.model.ParsedSection;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 文本类解析器基类，封装 UTF-8 读取、文件基础信息填充和按标题拆出解析段落的公共逻辑。
 */
public abstract class AbstractTextDocumentParser implements DocumentParser {
    protected ParsedDocument fromText(MultipartFile file, String text, boolean parseHeadings) {
        ParsedDocument doc = new ParsedDocument();
        String filename = file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename();
        doc.setFilename(filename);
        doc.setFileType(extension(filename));
        doc.setTitle(removeExtension(filename));
        doc.setText(text == null ? "" : text);
        doc.setSections(parseHeadings ? splitByHeadings(doc.getText()) : List.of(new ParsedSection(doc.getTitle(), doc.getText())));
        return doc;
    }

    protected String readUtf8(MultipartFile file) throws Exception {
        return new String(file.getBytes(), StandardCharsets.UTF_8);
    }

    protected List<ParsedSection> splitByHeadings(String text) {
        List<ParsedSection> sections = new ArrayList<>();
        String[] lines = text.split("\\R");
        List<String> titleStack = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        String currentTitle = "正文";

        for (String line : lines) {
            Heading heading = parseHeading(line);
            if (heading != null) {
                flushSection(sections, currentTitle, current);
                while (titleStack.size() >= heading.level) {
                    titleStack.remove(titleStack.size() - 1);
                }
                titleStack.add(heading.title);
                currentTitle = String.join(" / ", titleStack);
                current.append(line).append('\n');
            } else {
                current.append(line).append('\n');
            }
        }
        flushSection(sections, currentTitle, current);
        return sections.isEmpty() ? List.of(new ParsedSection("正文", text)) : sections;
    }

    private void flushSection(List<ParsedSection> sections, String title, StringBuilder current) {
        String content = current.toString().trim();
        if (!content.isEmpty()) {
            sections.add(new ParsedSection(title, content));
            current.setLength(0);
        }
    }

    private Heading parseHeading(String line) {
        String trimmed = line.trim();
        if (trimmed.startsWith("#")) {
            int level = 0;
            while (level < trimmed.length() && trimmed.charAt(level) == '#') level++;
            if (level > 0 && level <= 6 && trimmed.length() > level && Character.isWhitespace(trimmed.charAt(level))) {
                return new Heading(level, trimmed.substring(level).trim());
            }
        }
        if (trimmed.startsWith("=")) {
            int level = 0;
            while (level < trimmed.length() && trimmed.charAt(level) == '=') level++;
            if (level > 0 && level <= 6 && trimmed.length() > level && Character.isWhitespace(trimmed.charAt(level))) {
                return new Heading(level, trimmed.substring(level).trim());
            }
        }
        return null;
    }

    protected String extension(String filename) {
        int index = filename.lastIndexOf('.');
        return index < 0 ? "" : filename.substring(index + 1).toUpperCase();
    }

    protected String removeExtension(String filename) {
        int index = filename.lastIndexOf('.');
        return index < 0 ? filename : filename.substring(0, index);
    }

    private record Heading(int level, String title) {
    }
}
