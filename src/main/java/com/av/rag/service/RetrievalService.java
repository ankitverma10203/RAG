package com.av.rag.service;

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

@Service
public class RetrievalService {

    private final ChatModel chatModel;
    private final VectorStore vectorStore;

    public RetrievalService(ChatModel chatModel, VectorStore vectorStore) {
        this.chatModel = chatModel;
        this.vectorStore = vectorStore;
    }

    public String ask(String question) {
        List<Document> similarDocs = vectorStore.similaritySearch(question);

        String context = similarDocs.stream().map(Document::toString).collect(Collectors.joining("\n"));

        String promptWithContextTemplate = """
                Use the following context to answer the question.
                If you don't know, say you don't know.
                
                CONTEXT:
                {context}
                
                QUESTION:
                {question}
                """;

        PromptTemplate template = new PromptTemplate(promptWithContextTemplate);
        Prompt prompt = template.create(Map.of("context", context, "question", question));
        return chatModel.call(prompt).getResults().stream()
                .map(Generation::getOutput)
                .map(AssistantMessage::getText)
                .collect(Collectors.joining("\n"));
    }
}
