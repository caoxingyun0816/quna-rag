package com.quna.rag.parser;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

/**
 * 普通文本和代码文件解析器。
 */
@Component("standardTextDocumentParser")
public class TextDocumentParser implements DocumentParser {
    @Override
    public boolean supports(String filename) {
        if (filename == null) {
            return false;
        }
        String name = filename.toLowerCase();
        return name.endsWith(".txt") || name.endsWith(".java") || name.endsWith(".xml")
                || name.endsWith(".yml") || name.endsWith(".yaml") || name.endsWith(".json")
                || name.endsWith(".js") || name.endsWith(".vue") || name.endsWith(".html");
    }

    @Override
    public ParsedDocument parse(MultipartFile file) throws Exception {
        String text = new String(file.getBytes(), StandardCharsets.UTF_8);
        return new ParsedDocument(file.getOriginalFilename(), extension(file.getOriginalFilename()), normalize(text), true);
    }

    private String extension(String filename) {
        int index = filename == null ? -1 : filename.lastIndexOf('.');
        return index < 0 ? "txt" : filename.substring(index + 1).toLowerCase();
    }

    private String normalize(String text) {
        return text == null ? "" : text.replaceAll("\\r\\n?", "\n").replaceAll("\\n{4,}", "\n\n\n").trim();
    }
}
