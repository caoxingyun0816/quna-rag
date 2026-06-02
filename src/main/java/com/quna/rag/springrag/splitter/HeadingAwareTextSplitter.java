package com.quna.rag.springrag.splitter;

import com.quna.rag.springrag.config.SpringRagProperties;
import com.quna.rag.springrag.model.RagCollectionType;
import com.quna.rag.springrag.model.RagSplitConfigEntity;
import com.quna.rag.springrag.model.ParsedDocument;
import com.quna.rag.springrag.store.RagSplitConfigMapper;
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
    private static final Pattern API_TITLE = Pattern.compile(".*(接口|路由|请求参数|响应参数|Controller|Action|API|/[^\\s`]+).*", Pattern.CASE_INSENSITIVE);

    private final SpringRagProperties properties;
    private final RagSplitConfigMapper splitConfigMapper;

    public HeadingAwareTextSplitter(SpringRagProperties properties, RagSplitConfigMapper splitConfigMapper) {
        this.properties = properties;
        this.splitConfigMapper = splitConfigMapper;
    }

    @Override
    public List<RagChunk> split(ParsedDocument document, RagCollectionType collectionType, Map<String, Object> metadata) {
        String content = clean(document.getText());
        if (content.isBlank()) {
            return List.of();
        }
        ResolvedSplitConfig config = resolveConfig(collectionType);
        // Markdown 技术文档优先走标题感知切片，普通文本再退回 Spring AI 的 token 切分。
        if (isMarkdown(document)) {
            return splitMarkdown(document, collectionType, metadata, content, config);
        }
        return splitByToken(document, metadata, content, value(document.getTitle()), config);
    }

    private List<RagChunk> splitMarkdown(ParsedDocument document, RagCollectionType collectionType,
                                         Map<String, Object> metadata, String content, ResolvedSplitConfig config) {
        List<MarkdownSection> sections = parseMarkdownSections(document, content);
        if (collectionType == RagCollectionType.BUSINESS_DOC) {
            sections = mergeBusinessSections(sections, config);
        }
        List<MarkdownSection> merged = mergeSmallSections(sections, config);
        List<RagChunk> chunks = new ArrayList<>();
        for (MarkdownSection section : merged) {
            String text = clean(section.content());
            if (isLowValueSection(text, config) || text.length() < config.minLength()) {
                continue;
            }
            // 接口说明、代码块、表格等语义单元尽量整段保留；只有明显超长时才继续细分。
            if (shouldKeepWhole(section, config)) {
                chunks.add(new RagChunk(chunks.size() + 1, section.titlePath(), text));
                continue;
            }
            for (String blockChunk : splitOversizeMarkdownSection(text, config)) {
                String blockText = clean(blockChunk);
                if (blockText.length() < config.minLength()) {
                    continue;
                }
                if (blockText.length() <= config.hardMaxLength()) {
                    chunks.add(new RagChunk(chunks.size() + 1, section.titlePath(), blockText));
                    continue;
                }
                for (RagChunk chunk : splitByToken(document, metadata, blockText, section.titlePath(), config)) {
                    chunks.add(new RagChunk(chunks.size() + 1, chunk.getTitlePath(), chunk.getContent()));
                }
            }
        }
        return chunks;
    }

    private List<MarkdownSection> mergeBusinessSections(List<MarkdownSection> sections, ResolvedSplitConfig config) {
        List<MarkdownSection> merged = new ArrayList<>();
        MarkdownSection pending = null;
        for (MarkdownSection section : sections) {
            if (isLowValueSection(section.content(), config)) {
                continue;
            }
            if (pending == null) {
                pending = section;
                continue;
            }
            boolean sameBusinessChapter = !pending.businessGroupTitle().isBlank()
                    && pending.businessGroupTitle().equals(section.businessGroupTitle());
            int combinedLength = pending.content().length() + section.content().length() + 2;
            if (sameBusinessChapter && combinedLength <= Math.max(config.hardMaxLength(), properties.getChunk().getEmbeddingMaxLength())) {
                pending = pending.mergeToGroup(section);
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
                ? List.of(new MarkdownSection(currentTitle, content, 1, topNumber(currentTitle), 1,
                parentTitle(currentTitle), businessGroupTitle(currentTitle)))
                : sections;
    }

    private List<MarkdownSection> mergeSmallSections(List<MarkdownSection> sections, ResolvedSplitConfig config) {
        List<MarkdownSection> merged = new ArrayList<>();
        MarkdownSection pending = null;
        for (MarkdownSection section : sections) {
            if (pending == null) {
                pending = section;
                continue;
            }
            if (shouldMerge(pending, section, config)) {
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

    private boolean shouldMerge(MarkdownSection left, MarkdownSection right, ResolvedSplitConfig config) {
        int minLength = config.minLength();
        int combinedLength = left.content().length() + right.content().length() + 2;
        if (left.sectionCount() >= config.markdownMaxMergeSections() || combinedLength > config.maxLength()) {
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

    private List<RagChunk> splitByToken(ParsedDocument document,
                                        Map<String, Object> metadata, String content, String fallbackTitle,
                                        ResolvedSplitConfig config) {
        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(config.chunkSize())
                .withMinChunkSizeChars(config.minLength())
                .withMinChunkLengthToEmbed(config.minLength())
                .withKeepSeparator(true)
                .build();

        List<Document> splitDocuments = splitter.apply(List.of(new Document(content, metadata)));
        List<RagChunk> chunks = new ArrayList<>();
        for (Document splitDocument : splitDocuments) {
            String text = clean(splitDocument.getText());
            if (text.length() < config.minLength()) {
                continue;
            }
            chunks.add(new RagChunk(chunks.size() + 1, resolveTitlePath(document, text, fallbackTitle), text));
        }
        return chunks;
    }

    private void flushSection(List<MarkdownSection> sections, String title, StringBuilder current) {
        String content = clean(current.toString());
        if (!content.isBlank()) {
            sections.add(new MarkdownSection(title, content, headingLevel(title), topNumber(title), 1,
                    parentTitle(title), businessGroupTitle(title)));
            current.setLength(0);
        }
    }

    private boolean shouldKeepWhole(MarkdownSection section, ResolvedSplitConfig config) {
        if (section.content().length() <= config.hardMaxLength()) {
            return true;
        }
        if (config.protectApiSection() && isApiSection(section)) {
            return section.content().length() <= properties.getChunk().getEmbeddingMaxLength();
        }
        return section.level() >= config.markdownAtomicHeadingLevel()
                && section.content().length() <= config.hardMaxLength() * 2;
    }

    private List<String> splitOversizeMarkdownSection(String content, ResolvedSplitConfig config) {
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

            boolean tableLine = config.protectMarkdownTable() && !inCodeBlock && trimmed.startsWith("|") && trimmed.endsWith("|");
            if (tableLine) {
                if (!inTable) {
                    flushBlock(current, block, chunks, config.hardMaxLength());
                    inTable = true;
                    tableHeader = line;
                    tableSeparator = "";
                } else if (tableSeparator.isBlank() && trimmed.matches("^\\|\\s*:?-{3,}:?\\s*(\\|\\s*:?-{3,}:?\\s*)+\\|?$")) {
                    tableSeparator = line;
                }
                if (current.length() + block.length() + line.length() > config.hardMaxLength() && block.length() > 0) {
                    flushBlock(current, block, chunks, config.hardMaxLength());
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
                flushBlock(current, block, chunks, config.hardMaxLength());
                inTable = false;
                tableHeader = "";
                tableSeparator = "";
            }

            if (!inCodeBlock && trimmed.isBlank()) {
                flushBlock(current, block, chunks, config.hardMaxLength());
                current.append('\n');
                if (current.length() > config.hardMaxLength()) {
                    flushChunk(current, chunks);
                }
                continue;
            }
            block.append(line).append('\n');
        }
        flushBlock(current, block, chunks, config.hardMaxLength());
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

    private ResolvedSplitConfig resolveConfig(RagCollectionType collectionType) {
        RagSplitConfigEntity config = null;
        try {
            config = splitConfigMapper.selectEnabled(collectionType.getCode());
        } catch (Exception ignored) {
            // 配置表不存在或未初始化时，回退到 application.yml 默认值，避免影响主流程。
        }
        int defaultChunkSize = collectionType == RagCollectionType.TECH_DOC
                ? properties.getChunk().getTechSize()
                : properties.getChunk().getBusinessSize();
        int chunkSize = intValue(config == null ? null : config.getChunkSize(), defaultChunkSize, 200);
        int minLength = intValue(config == null ? null : config.getMinLength(), properties.getChunk().getMinLength(), 20);
        int softMultiplier = intValue(config == null ? null : config.getMarkdownSoftMultiplier(), properties.getChunk().getMarkdownSoftMultiplier(), 1);
        int hardMultiplier = intValue(config == null ? null : config.getMarkdownHardMultiplier(), properties.getChunk().getMarkdownHardMultiplier(), 1);
        return new ResolvedSplitConfig(
                chunkSize,
                Math.max(chunkSize * softMultiplier, chunkSize),
                Math.max(chunkSize * hardMultiplier, chunkSize),
                minLength,
                intValue(config == null ? null : config.getMarkdownAtomicHeadingLevel(), properties.getChunk().getMarkdownAtomicHeadingLevel(), 1),
                intValue(config == null ? null : config.getMarkdownMaxMergeSections(), properties.getChunk().getMarkdownMaxMergeSections(), 1),
                boolValue(config == null ? null : config.getProtectMarkdownTable(), true),
                boolValue(config == null ? null : config.getProtectCodeFence(), true),
                boolValue(config == null ? null : config.getProtectApiSection(), true),
                boolValue(config == null ? null : config.getProtectJsonBlock(), true),
                boolValue(config == null ? null : config.getProtectSqlBlock(), true)
        );
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

    private String businessGroupTitle(String titlePath) {
        if (titlePath == null || titlePath.isBlank()) {
            return "";
        }
        String[] parts = titlePath.split("\\s*/\\s*");
        if (parts.length <= 2) {
            return titlePath.trim();
        }
        return parts[0].trim() + " / " + parts[1].trim();
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

    private boolean isApiSection(MarkdownSection section) {
        return API_TITLE.matcher(section.titlePath()).matches()
                || API_TITLE.matcher(firstHeading(section.content())).matches()
                || section.content().contains("**路由**")
                || section.content().contains("请求参数表")
                || section.content().contains("响应参数表");
    }

    private boolean isLowValueSection(String content, ResolvedSplitConfig config) {
        String text = content == null ? "" : content;
        String semanticText = text
                .replaceAll("!\\[[^\\]]*]\\([^)]*\\)", " ")
                .replaceAll("https?://\\S+", " ")
                .replaceAll("(?m)^#{1,6}\\s+", " ")
                .replaceAll("(?m)^```[a-zA-Z0-9_+-]*\\s*$", " ")
                .replaceAll("[\\p{Punct}\\s]+", " ")
                .trim();
        return semanticText.length() < Math.max(20, config.minLength() / 2);
    }

    private int intValue(Integer value, int fallback, int min) {
        return value == null ? Math.max(fallback, min) : Math.max(value, min);
    }

    private boolean boolValue(Boolean value, boolean fallback) {
        return value == null ? fallback : value;
    }

    private record Heading(int level, String title) {
    }

    private record ResolvedSplitConfig(int chunkSize, int maxLength, int hardMaxLength, int minLength,
                                       int markdownAtomicHeadingLevel, int markdownMaxMergeSections,
                                       boolean protectMarkdownTable, boolean protectCodeFence,
                                       boolean protectApiSection, boolean protectJsonBlock,
                                       boolean protectSqlBlock) {
    }

    private record MarkdownSection(String titlePath, String content, int level, int topNumber, int sectionCount,
                                   String parentTitlePath, String businessGroupTitle) {
        MarkdownSection merge(MarkdownSection next) {
            String mergedTitle = mergeTitle(titlePath, next.titlePath);
            return new MarkdownSection(mergedTitle,
                    content + "\n\n" + next.content(),
                    Math.min(level, next.level()),
                    topNumber > 0 ? topNumber : next.topNumber(),
                    sectionCount + next.sectionCount(),
                    parentTitle(mergedTitle),
                    businessGroupTitle(mergedTitle));
        }

        MarkdownSection mergeToGroup(MarkdownSection next) {
            String mergedTitle = businessGroupTitle.isBlank() ? titlePath : businessGroupTitle;
            return new MarkdownSection(mergedTitle,
                    content + "\n\n" + next.content(),
                    Math.min(level, next.level()),
                    topNumber > 0 ? topNumber : next.topNumber(),
                    sectionCount + next.sectionCount(),
                    parentTitle(mergedTitle),
                    businessGroupTitle(mergedTitle));
        }

        private String mergeTitle(String left, String right) {
            if (left == null || left.isBlank()) {
                return right == null ? "" : right;
            }
            if (right == null || right.isBlank() || left.equals(right) || right.startsWith(left + " / ")) {
                return left;
            }
            if (left.startsWith(right + " / ")) {
                return right;
            }
            String leftParent = parentTitle(left);
            String rightParent = parentTitle(right);
            if (!leftParent.isBlank() && leftParent.equals(rightParent)) {
                return leftParent;
            }
            return left + " / " + leafTitle(right);
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

        private String businessGroupTitle(String titlePath) {
            if (titlePath == null || titlePath.isBlank()) {
                return "";
            }
            String[] parts = titlePath.split("\\s*/\\s*");
            if (parts.length <= 2) {
                return titlePath.trim();
            }
            return parts[0].trim() + " / " + parts[1].trim();
        }

        private String leafTitle(String titlePath) {
            if (titlePath == null || titlePath.isBlank()) {
                return "";
            }
            String[] parts = titlePath.split("\\s*/\\s*");
            return parts[parts.length - 1].trim();
        }
    }
}
