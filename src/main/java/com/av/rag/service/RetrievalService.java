package com.av.rag.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for handling question answering using RAG (Retrieval Augmented Generation).
 * Retrieves relevant documents and uses them to generate contextual answers.
 */
@Service
public class RetrievalService {

    private static final Logger logger = LoggerFactory.getLogger(RetrievalService.class);

    private final ChatModel chatModel;
    private final VectorStore vectorStore;

    /**
     * Constructor for RetrievalService.
     * @param chatModel Chat model for generating answers
     * @param vectorStore Vector store for finding similar documents
     */
    public RetrievalService(ChatModel chatModel, VectorStore vectorStore) {
        this.chatModel = chatModel;
        this.vectorStore = vectorStore;
    }

    /**
     * Ask a question and get an AI-generated answer based on relevant documents.
     * @param question The question to ask
     * @return Answer generated using relevant document context
     */
    public String ask(String question) {
        logger.info("Searching for documents similar to question: {}", question);
        List<Document> similarDocs = vectorStore.similaritySearch(question);

        if (similarDocs.isEmpty()) {
            logger.warn("No similar documents found for question: {}", question);
            return "No relevant documents found. Please upload documents first or try a different question.";
        }

        logger.info("Found {} similar documents", similarDocs.size());
        String context = similarDocs.stream()
                .map(Document::toString)
                .collect(Collectors.joining("\n---\n"));

        String promptWithContextTemplate = """
                Use the following context to answer the question.
                If you don't know the answer from the context, say you don't know.
                Be accurate and concise in your response.
                
                CONTEXT:
                {context}
                
                QUESTION:
                {question}
                
                ANSWER:""";

        try {
            logger.debug("Creating prompt with context of length: {}", context.length());
            PromptTemplate template = new PromptTemplate(promptWithContextTemplate);
            Prompt prompt = template.create(Map.of("context", context, "question", question));

            String answer = chatModel.call(prompt).getResults().stream()
                    .map(Generation::getOutput)
                    .map(AssistantMessage::getText)
                    .collect(Collectors.joining("\n"));

            logger.info("Successfully generated answer");
            return answer;
        } catch (Exception e) {
            logger.error("Error during answer generation", e);
            throw new RuntimeException("Failed to generate answer: " + e.getMessage(), e);
        }
    }
}
