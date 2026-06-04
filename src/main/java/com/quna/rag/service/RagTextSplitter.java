package com.quna.rag.service;

import com.quna.rag.parser.ParsedDocument;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * RAG 文本切片服务。
 * Markdown 优先按标题切分，普通文本按字符窗口切分，并保留少量重叠上下文。
 */
@Service
public class RagTextSplitter {
    private final int chunkSize;
    private final int overlap;

    public RagTextSplitter(@Value("${rag.standard.chunk-size:1200}") int chunkSize,
                           @Value("${rag.standard.chunk-overlap:100}") int overlap) {
        this.chunkSize = chunkSize;
        this.overlap = overlap;
    }

    public List<Chunk> split(ParsedDocument document) {
        String text = document.getText() == null ? "" : document.getText().trim();
        if (text.isBlank()) {
            return List.of();
        }
        if (document.isMarkdownLike()) {
            List<Chunk> chunks = splitMarkdown(text);
            if (!chunks.isEmpty()) {
                return chunks;
            }
        }
        return splitByCharacters(text, "正文");
    }

    private List<Chunk> splitMarkdown(String text) {
        String[] lines = text.split("\\n");
        List<Section> sections = new ArrayList<>();
        String currentTitle = "正文";
        StringBuilder current = new StringBuilder();
        for (String line : lines) {
            if (line.matches("^#{1,3}\\s+.+")) {
                flush(sections, currentTitle, current);
                currentTitle = line.replaceFirst("^#{1,3}\\s+", "").trim();
            }
            current.append(line).append('\n');
        }
        flush(sections, currentTitle, current);

        List<Chunk> chunks = new ArrayList<>();
        StringBuilder merged = new StringBuilder();
        StringBuilder title = new StringBuilder();
        for (Section section : sections) {
            if (section.content().length() > chunkSize * 3) {
                flushMerged(chunks, title, merged);
                chunks.addAll(splitByCharacters(section.content(), section.title()));
                continue;
            }
            if (merged.length() > 0 && merged.length() + section.content().length() > chunkSize * 2) {
                flushMerged(chunks, title, merged);
            }
            if (title.length() > 0) {
                title.append(" / ");
            }
            title.append(section.title());
            merged.append(section.content()).append("\n\n");
        }
        flushMerged(chunks, title, merged);
        return reindex(chunks);
    }

    private List<Chunk> splitByCharacters(String text, String title) {
        List<Chunk> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            int actualStart = Math.max(0, start - overlap);
            int actualEnd = Math.min(text.length(), end + overlap);
            chunks.add(new Chunk(chunks.size() + 1, title, text.substring(actualStart, actualEnd).trim()));
            if (end >= text.length()) {
                break;
            }
            start = end;
        }
        return chunks;
    }

    private void flush(List<Section> sections, String title, StringBuilder current) {
        String content = current.toString().trim();
        if (!content.isBlank()) {
            sections.add(new Section(title, content));
        }
        current.setLength(0);
    }

    private void flushMerged(List<Chunk> chunks, StringBuilder title, StringBuilder merged) {
        String content = merged.toString().trim();
        if (!content.isBlank()) {
            chunks.add(new Chunk(chunks.size() + 1, title.toString(), content));
        }
        title.setLength(0);
        merged.setLength(0);
    }

    private List<Chunk> reindex(List<Chunk> chunks) {
        for (int i = 0; i < chunks.size(); i++) {
            chunks.get(i).setIndex(i + 1);
        }
        return chunks;
    }

    private record Section(String title, String content) {
    }

    @Data
    @AllArgsConstructor
    public static class Chunk {
        private int index;
        private String title;
        private String content;
    }
}
