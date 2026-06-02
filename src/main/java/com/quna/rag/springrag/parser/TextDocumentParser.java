package com.quna.rag.springrag.parser;

import com.quna.rag.springrag.model.ParsedDocument;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 纯文本解析器，处理 .txt/.log 等文本文件并按标题规则尝试拆分段落。
 */
@Component
public class TextDocumentParser extends AbstractTextDocumentParser {
    @Override
    public boolean supports(String filename) {
        String name = filename.toLowerCase();
        return name.endsWith(".txt");
    }

    @Override
    public ParsedDocument parse(MultipartFile file) throws Exception {
        return fromText(file, readUtf8(file), false);
    }
}
