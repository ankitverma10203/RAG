package com.av.rag.service;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentIngestionService {
    private final Tika tika;
    private final VectorStore vectorStore;

    DocumentIngestionService(Tika tika, VectorStore vectorStore) {
        this.tika = tika;
        this.vectorStore = vectorStore;
    }

    public void processAndStore(List<MultipartFile> files) {
        List<Document> documents = files.stream().map(file -> {
            try {
                return new Document(tika.parseToString(file.getInputStream()));
            } catch (IOException | TikaException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        List<Document> chunks = ingestFiles(documents);
        vectorStore.add(chunks);
    }

    public List<Document> ingestFiles(List<Document> documents) {
        TokenTextSplitter textSplitter = TokenTextSplitter.builder().withChunkSize(500).withKeepSeparator(true).build();
        return textSplitter.split(documents);
    }
}
