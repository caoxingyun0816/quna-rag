package com.quna.rag.springrag.metadata;

import com.quna.rag.springrag.model.RagCollectionType;
import com.quna.rag.springrag.parser.ParsedDocument;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class RagMetadataBuilder {
    public Map<String, Object> documentMetadata(RagCollectionType collectionType, ParsedDocument document,
                                                String project, String module, String docType, String tags) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("collectionCode", collectionType.getCode());
        metadata.put("collectionName", collectionType.getLabel());
        metadata.put("filename", value(document.getFilename()));
        metadata.put("fileType", value(document.getFileType()));
        metadata.put("project", value(project));
        metadata.put("module", value(module));
        metadata.put("docType", value(docType));
        metadata.put("tags", value(tags));
        metadata.put("permissionScope", "ALL");
        metadata.put("visibleRoles", "");
        return sanitize(metadata);
    }

    public Map<String, Object> chunkMetadata(Map<String, Object> documentMetadata, Long docId, Long chunkId,
                                             Integer chunkIndex, String titlePath) {
        Map<String, Object> metadata = new LinkedHashMap<>(documentMetadata);
        metadata.put("docId", docId == null ? 0L : docId);
        metadata.put("chunkId", chunkId == null ? 0L : chunkId);
        metadata.put("chunkIndex", chunkIndex == null ? 0 : chunkIndex);
        metadata.put("titlePath", value(titlePath));
        return sanitize(metadata);
    }

    public Map<String, Object> sanitize(Map<String, Object> source) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        if (source == null) {
            return metadata;
        }
        source.forEach((key, value) -> metadata.put(key, normalize(value)));
        return metadata;
    }

    private Object normalize(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value;
        }
        return value(value.toString());
    }

    private String value(String value) {
        return value == null ? "" : value.trim();
    }
}
