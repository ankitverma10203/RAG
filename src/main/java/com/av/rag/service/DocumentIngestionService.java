package com.av.rag.service;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for handling document ingestion and indexing.
 * Processes uploaded files, splits them into chunks, and stores them in the vector store.
 */
@Service
public class DocumentIngestionService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentIngestionService.class);

    private final Tika tika;
    private final VectorStore vectorStore;

    @Value("${app.rag.chunk-size:500}")
    private int chunkSize;

    @Value("${app.rag.keep-separator:true}")
    private boolean keepSeparator;

    /**
     * Constructor for DocumentIngestionService.
     * @param tika Tika instance for document parsing
     * @param vectorStore Vector store for storing embeddings
     */
    DocumentIngestionService(Tika tika, VectorStore vectorStore) {
        this.tika = tika;
        this.vectorStore = vectorStore;
    }

    /**
     * Process and store documents in the vector store.
     * @param files List of files to process
     */
    public void processAndStore(List<MultipartFile> files) {
        logger.info("Starting document ingestion for {} files", files.size());

        List<Document> documents = files.stream().map(file -> {
            try {
                logger.debug("Parsing file: {}", file.getOriginalFilename());
                String content = tika.parseToString(file.getInputStream());
                logger.debug("Successfully parsed file: {} with {} characters",
                        file.getOriginalFilename(), content.length());
                return new Document(content);
            } catch (IOException | TikaException e) {
                logger.error("Failed to parse file: {}", file.getOriginalFilename(), e);
                throw new RuntimeException("Failed to parse file: " + file.getOriginalFilename(), e);
            }
        }).collect(Collectors.toList());

        logger.info("Successfully parsed {} documents", documents.size());

        List<Document> chunks = ingestFiles(documents);
        logger.info("Created {} chunks from documents", chunks.size());

        vectorStore.add(chunks);
        logger.info("Successfully indexed all documents in vector store");
    }

    /**
     * Split documents into chunks for better semantic search.
     * @param documents List of documents to split
     * @return List of document chunks
     */
    public List<Document> ingestFiles(List<Document> documents) {
        logger.debug("Splitting {} documents with chunk size: {}", documents.size(), chunkSize);
        TokenTextSplitter textSplitter = TokenTextSplitter.builder()
                .withChunkSize(chunkSize)
                .withKeepSeparator(keepSeparator)
                .build();
        return textSplitter.split(documents);
    }
}
