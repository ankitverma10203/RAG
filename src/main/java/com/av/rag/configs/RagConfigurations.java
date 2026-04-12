package com.av.rag.configs;

import org.apache.tika.Tika;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RagConfigurations {

    @Bean
    public Tika getTika() {
        return new Tika();
    }
}
