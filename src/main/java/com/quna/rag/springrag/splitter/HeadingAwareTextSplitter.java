package com.quna.rag.springrag.splitter;

import com.quna.rag.springrag.config.SpringRagProperties;
import com.quna.rag.springrag.model.RagCollectionType;
import com.quna.rag.springrag.parser.ParsedDocument;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * 标题感知切片器，优先按 Markdown 标题和接口文档结构保持语义完整，再对超长内容做兜底切分。
 */

@Component
public class HeadingAwareTextSplitter implements RagDocumentSplitter {
    private static final Pattern MARKDOWN_HEADING = Pattern.compile("^(#{1,6})\\s+(.+?)\\s*$");
    private static final Pattern LEADING_NUMBER = Pattern.compile("^(\\d+)(?:[.、\\s]|$).*");

    private final SpringRagProperties properties;

    public HeadingAwareTextSplitter(SpringRagProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<RagChunk> split(ParsedDocument document, RagCollectionType collectionType, Map<String, Object> metadata) {
        String content = clean(document.getText());
        if (content.isBlank()) {
            return List.of();
        }
        // Markdown 技术文档优先走标题感知切片，普通文本再退回 Spring AI 的 token 切分。
        if (isMarkdown(document)) {
            return splitMarkdown(document, collectionType, metadata, content);
        }
        return splitByToken(document, collectionType, metadata, content, value(document.getTitle()));
    }

    private List<RagChunk> splitMarkdown(ParsedDocument document, RagCollectionType collectionType,
                                         Map<String, Object> metadata, String content) {
        List<MarkdownSection> sections = parseMarkdownSections(document, content);
        List<MarkdownSection> merged = mergeSmallSections(sections, resolveMaxLength(collectionType));
        List<RagChunk> chunks = new ArrayList<>();
        for (MarkdownSection section : merged) {
            String text = clean(section.content());
            if (text.length() < properties.getChunk().getMinLength()) {
                continue;
            }
            // 接口说明、代码块、表格等语义单元尽量整段保留；只有明显超长时才继续细分。
            if (shouldKeepWhole(section, collectionType)) {
                chunks.add(new RagChunk(chunks.size() + 1, section.titlePath(), text));
                continue;
            }
            for (String blockChunk : splitOversizeMarkdownSection(text, resolveHardMaxLength(collectionType))) {
                String blockText = clean(blockChunk);
                if (blockText.length() < properties.getChunk().getMinLength()) {
                    continue;
                }
                if (blockText.length() <= resolveHardMaxLength(collectionType)) {
                    chunks.add(new RagChunk(chunks.size() + 1, section.titlePath(), blockText));
                    continue;
                }
                for (RagChunk chunk : splitByToken(document, collectionType, metadata, blockText, section.titlePath())) {
                    chunks.add(new RagChunk(chunks.size() + 1, chunk.getTitlePath(), chunk.getContent()));
                }
            }
        }
        return chunks;
    }

    private List<MarkdownSection> parseMarkdownSections(ParsedDocument document, String content) {
        List<MarkdownSection> sections = new ArrayList<>();
        List<String> titleStack = new ArrayList<>();
        String currentTitle = value(document.getTitle()).isBlank() ? "正文" : value(document.getTitle());
        StringBuilder current = new StringBuilder();
        boolean inCodeBlock = false;

        // 代码块中的 # 可能只是注释或示例内容，不能当作 Markdown 标题切开。
        for (String line : content.split("\\R", -1)) {
            String trimmed = line.trim();
            if (trimmed.startsWith("```") || trimmed.startsWith("~~~")) {
                inCodeBlock = !inCodeBlock;
                current.append(line).append('\n');
                continue;
            }

            Heading heading = inCodeBlock ? null : parseHeading(line);
            if (heading != null) {
                flushSection(sections, currentTitle, current);
                while (titleStack.size() >= heading.level()) {
                    titleStack.remove(titleStack.size() - 1);
                }
                titleStack.add(heading.title());
                currentTitle = String.join(" / ", titleStack);
            }
            current.append(line).append('\n');
        }
        flushSection(sections, currentTitle, current);
        return sections.isEmpty()
                ? List.of(new MarkdownSection(currentTitle, content, 1, topNumber(currentTitle), 1, parentTitle(currentTitle)))
                : sections;
    }

    private List<MarkdownSection> mergeSmallSections(List<MarkdownSection> sections, int maxLength) {
        List<MarkdownSection> merged = new ArrayList<>();
        MarkdownSection pending = null;
        for (MarkdownSection section : sections) {
            if (pending == null) {
                pending = section;
                continue;
            }
            if (shouldMerge(pending, section, maxLength)) {
                pending = pending.merge(section);
            } else {
                merged.add(pending);
                pending = section;
            }
        }
        if (pending != null) {
            merged.add(pending);
        }
        return merged;
    }

    private boolean shouldMerge(MarkdownSection left, MarkdownSection right, int maxLength) {
        int minLength = properties.getChunk().getMinLength();
        int combinedLength = left.content().length() + right.content().length() + 2;
        if (left.sectionCount() >= properties.getChunk().getMarkdownMaxMergeSections() || combinedLength > maxLength) {
            return false;
        }
        if (left.content().length() >= minLength && right.content().length() >= minLength) {
            return false;
        }
        return sameParent(left, right) || sameTopGroup(left, right) || adjacentTopGroup(left, right);
    }

    private boolean sameParent(MarkdownSection left, MarkdownSection right) {
        return !left.parentTitlePath().isBlank() && left.parentTitlePath().equals(right.parentTitlePath());
    }

    private boolean sameTopGroup(MarkdownSection left, MarkdownSection right) {
        return left.topNumber() > 0 && left.topNumber() == right.topNumber();
    }

    private boolean adjacentTopGroup(MarkdownSection left, MarkdownSection right) {
        return left.topNumber() > 0 && right.topNumber() > 0 && right.topNumber() - left.topNumber() == 1;
    }

    private List<RagChunk> splitByToken(ParsedDocument document, RagCollectionType collectionType,
                                        Map<String, Object> metadata, String content, String fallbackTitle) {
        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(resolveChunkSize(collectionType))
                .withMinChunkSizeChars(properties.getChunk().getMinLength())
                .withMinChunkLengthToEmbed(properties.getChunk().getMinLength())
                .withKeepSeparator(true)
                .build();

        List<Document> splitDocuments = splitter.apply(List.of(new Document(content, metadata)));
        List<RagChunk> chunks = new ArrayList<>();
        for (Document splitDocument : splitDocuments) {
            String text = clean(splitDocument.getText());
            if (text.length() < properties.getChunk().getMinLength()) {
                continue;
            }
            chunks.add(new RagChunk(chunks.size() + 1, resolveTitlePath(document, text, fallbackTitle), text));
        }
        return chunks;
    }

    private void flushSection(List<MarkdownSection> sections, String title, StringBuilder current) {
        String content = clean(current.toString());
        if (!content.isBlank()) {
            sections.add(new MarkdownSection(title, content, headingLevel(title), topNumber(title), 1, parentTitle(title)));
            current.setLength(0);
        }
    }

    private boolean shouldKeepWhole(MarkdownSection section, RagCollectionType collectionType) {
        int hardMaxLength = resolveHardMaxLength(collectionType);
        if (section.content().length() <= hardMaxLength) {
            return true;
        }
        return section.level() >= properties.getChunk().getMarkdownAtomicHeadingLevel()
                && section.content().length() <= hardMaxLength * 2;
    }

    private List<String> splitOversizeMarkdownSection(String content, int maxLength) {
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        StringBuilder block = new StringBuilder();
        boolean inCodeBlock = false;
        boolean inTable = false;
        String tableHeader = "";
        String tableSeparator = "";

        for (String line : content.split("\\R", -1)) {
            String trimmed = line.trim();
            boolean codeFence = trimmed.startsWith("```") || trimmed.startsWith("~~~");
            if (codeFence) {
                inCodeBlock = !inCodeBlock;
                block.append(line).append('\n');
                continue;
            }

            boolean tableLine = !inCodeBlock && trimmed.startsWith("|") && trimmed.endsWith("|");
            if (tableLine) {
                if (!inTable) {
                    flushBlock(current, block, chunks, maxLength);
                    inTable = true;
                    tableHeader = line;
                    tableSeparator = "";
                } else if (tableSeparator.isBlank() && trimmed.matches("^\\|\\s*:?-{3,}:?\\s*(\\|\\s*:?-{3,}:?\\s*)+\\|?$")) {
                    tableSeparator = line;
                }
                if (current.length() + block.length() + line.length() > maxLength && block.length() > 0) {
                    flushBlock(current, block, chunks, maxLength);
                    if (!tableHeader.isBlank()) {
                        block.append(tableHeader).append('\n');
                    }
                    if (!tableSeparator.isBlank()) {
                        block.append(tableSeparator).append('\n');
                    }
                }
                block.append(line).append('\n');
                continue;
            }

            if (inTable) {
                flushBlock(current, block, chunks, maxLength);
                inTable = false;
                tableHeader = "";
                tableSeparator = "";
            }

            if (!inCodeBlock && trimmed.isBlank()) {
                flushBlock(current, block, chunks, maxLength);
                current.append('\n');
                if (current.length() > maxLength) {
                    flushChunk(current, chunks);
                }
                continue;
            }
            block.append(line).append('\n');
        }
        flushBlock(current, block, chunks, maxLength);
        flushChunk(current, chunks);
        return chunks.isEmpty() ? List.of(content) : chunks;
    }

    private void flushBlock(StringBuilder current, StringBuilder block, List<String> chunks, int maxLength) {
        if (block.isEmpty()) {
            return;
        }
        if (current.length() + block.length() > maxLength && !current.isEmpty()) {
            flushChunk(current, chunks);
        }
        current.append(block);
        block.setLength(0);
    }

    private void flushChunk(StringBuilder current, List<String> chunks) {
        String chunk = clean(current.toString());
        if (!chunk.isBlank()) {
            chunks.add(chunk);
        }
        current.setLength(0);
    }

    private Heading parseHeading(String line) {
        Matcher matcher = MARKDOWN_HEADING.matcher(line.trim());
        if (!matcher.matches()) {
            return null;
        }
        return new Heading(matcher.group(1).length(), matcher.group(2).trim());
    }

    private int resolveChunkSize(RagCollectionType collectionType) {
        int chunkSize = collectionType == RagCollectionType.TECH_DOC
                ? properties.getChunk().getTechSize()
                : properties.getChunk().getBusinessSize();
        return Math.max(chunkSize, 200);
    }

    private int resolveMaxLength(RagCollectionType collectionType) {
        return resolveChunkSize(collectionType) * Math.max(properties.getChunk().getMarkdownSoftMultiplier(), 1);
    }

    private int resolveHardMaxLength(RagCollectionType collectionType) {
        return resolveChunkSize(collectionType) * Math.max(properties.getChunk().getMarkdownHardMultiplier(), 1);
    }

    private String resolveTitlePath(ParsedDocument document, String content, String fallbackTitle) {
        String heading = firstHeading(content);
        if (!heading.isBlank()) {
            return heading;
        }
        if (!value(fallbackTitle).isBlank()) {
            return value(fallbackTitle);
        }
        return value(document.getTitle());
    }

    private String firstHeading(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        boolean inCodeBlock = false;
        for (String line : content.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("```") || trimmed.startsWith("~~~")) {
                inCodeBlock = !inCodeBlock;
                continue;
            }
            Heading heading = inCodeBlock ? null : parseHeading(trimmed);
            if (heading != null) {
                return heading.title();
            }
            if (!trimmed.isBlank()) {
                break;
            }
        }
        return "";
    }

    private boolean isMarkdown(ParsedDocument document) {
        String fileType = value(document.getFileType()).toLowerCase();
        String filename = value(document.getFilename()).toLowerCase();
        return fileType.equals("md") || fileType.equals("markdown")
                || filename.endsWith(".md") || filename.endsWith(".markdown");
    }

    private int headingLevel(String titlePath) {
        if (titlePath == null || titlePath.isBlank()) {
            return 1;
        }
        return titlePath.split("\\s*/\\s*").length;
    }

    private String parentTitle(String titlePath) {
        if (titlePath == null || titlePath.isBlank()) {
            return "";
        }
        String[] parts = titlePath.split("\\s*/\\s*");
        if (parts.length <= 1) {
            return "";
        }
        List<String> parentParts = new ArrayList<>();
        for (int i = 0; i < parts.length - 1; i++) {
            parentParts.add(parts[i]);
        }
        return String.join(" / ", parentParts);
    }

    private int topNumber(String titlePath) {
        if (titlePath == null || titlePath.isBlank()) {
            return -1;
        }
        String first = titlePath.split("\\s*/\\s*")[0].trim();
        Matcher matcher = LEADING_NUMBER.matcher(first);
        if (!matcher.matches()) {
            return -1;
        }
        return Integer.parseInt(matcher.group(1));
    }

    private String clean(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("[\\u0000-\\u0008\\u000B\\u000C\\u000E-\\u001F]", " ")
                .replaceAll("(?m)^\\s*[-*_]{3,}\\s*$", "")
                .replaceAll("[ \\t]+\\R", "\n")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }

    private String value(String value) {
        return value == null ? "" : value.trim();
    }

    private record Heading(int level, String title) {
    }

    private record MarkdownSection(String titlePath, String content, int level, int topNumber, int sectionCount,
                                   String parentTitlePath) {
        MarkdownSection merge(MarkdownSection next) {
            return new MarkdownSection(titlePath + " / " + next.titlePath(),
                    content + "\n\n" + next.content(),
                    Math.min(level, next.level()),
                    topNumber > 0 ? topNumber : next.topNumber(),
                    sectionCount + next.sectionCount(),
                    parentTitlePath);
        }
    }
}
