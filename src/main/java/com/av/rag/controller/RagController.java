package com.av.rag.controller;

import com.av.rag.service.DocumentIngestionService;
import com.av.rag.service.RetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/v1")
public class RagController {

    @Autowired
    private DocumentIngestionService documentIngestionService;
    @Autowired
    private RetrievalService retrievalService;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("files") List<MultipartFile> files) {
        documentIngestionService.processAndStore(files);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/ask")
    public ResponseEntity<?> ask(@RequestParam("question") String question) {
        return new ResponseEntity<>(retrievalService.ask(question), HttpStatus.OK);
    }
}
