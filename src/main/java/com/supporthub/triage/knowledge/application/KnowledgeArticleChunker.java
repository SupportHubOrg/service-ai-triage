package com.supporthub.triage.knowledge.application;

import com.supporthub.triage.knowledge.config.RagProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class KnowledgeArticleChunker {

    private final RagProperties properties;

    public List<KnowledgeArticleChunk> chunk(String content) {
        var normalized = normalize(content);
        if (normalized.isBlank()) {
            return List.of();
        }

        var maxChars = properties.chunking().maxChars();
        var overlapChars = properties.chunking().overlapChars();
        var chunks = new ArrayList<KnowledgeArticleChunk>();
        var start = 0;

        while (start < normalized.length()) {
            var end = findChunkEnd(normalized, start, maxChars);
            chunks.add(new KnowledgeArticleChunk(chunks.size(), normalized.substring(start, end).trim()));
            start = nextStart(normalized, end, overlapChars);
        }

        return chunks;
    }

    private String normalize(String content) {
        if (content == null) {
            return "";
        }
        return content.replace("\r\n", "\n").trim();
    }

    private int findChunkEnd(String content, int start, int maxChars) {
        var limit = Math.min(content.length(), start + maxChars);
        if (limit == content.length()) {
            return limit;
        }

        var paragraphBreak = content.lastIndexOf("\n\n", limit);
        if (paragraphBreak > start) {
            return paragraphBreak;
        }

        var sentenceBreak = content.lastIndexOf(". ", limit);
        if (sentenceBreak > start) {
            return sentenceBreak + 1;
        }

        var wordBreak = content.lastIndexOf(' ', limit);
        if (wordBreak > start) {
            return wordBreak;
        }

        return limit;
    }

    private int nextStart(String content, int previousEnd, int overlapChars) {
        if (previousEnd >= content.length()) {
            return content.length();
        }
        var start = Math.max(0, previousEnd - overlapChars);
        while (start < content.length() && Character.isWhitespace(content.charAt(start))) {
            start++;
        }
        if (start >= previousEnd) {
            return previousEnd;
        }
        return start;
    }
}
