package com.quna.rag.springrag.simple.splitter;

import com.quna.rag.springrag.splitter.RagChunk;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 简单字符切分器：主体按固定字符数切分，每段附带前后冗余上下文。
 */
@Component
public class CharacterOverlapTextSplitter {
    private static final int DEFAULT_CHUNK_SIZE = 1000;
    private static final int DEFAULT_OVERLAP = 100;

    public List<RagChunk> split(String text, Integer chunkSize, Integer overlap) {
        String content = text == null ? "" : text.trim();
        if (content.isBlank()) {
            return List.of();
        }
        int size = Math.max(chunkSize == null ? DEFAULT_CHUNK_SIZE : chunkSize, 100);
        int redundancy = Math.max(overlap == null ? DEFAULT_OVERLAP : overlap, 0);
        List<RagChunk> chunks = new ArrayList<>();
        int index = 1;
        for (int start = 0; start < content.length(); start += size) {
            int end = Math.min(start + size, content.length());
            int contextStart = Math.max(0, start - redundancy);
            int contextEnd = Math.min(content.length(), end + redundancy);
            String chunk = content.substring(contextStart, contextEnd).trim();
            if (!chunk.isBlank()) {
                chunks.add(new RagChunk(index++, "字符切片 " + indexTitle(start, end), chunk));
            }
            if (end >= content.length()) {
                break;
            }
        }
        return chunks;
    }

    private String indexTitle(int start, int end) {
        return start + "-" + end;
    }
}
