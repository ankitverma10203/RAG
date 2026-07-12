# RAG - Retrieval Augmented Generation

A Spring Boot application that implements Retrieval Augmented Generation (RAG) using Google Gemini AI and PostgreSQL with PGVector for semantic search capabilities. This application allows you to upload documents and ask questions about them using advanced AI-powered retrieval and generation.

## Features

- **Document Ingestion**: Upload multiple documents in various formats (PDF, DOCX, TXT, etc.) using Apache Tika
- **Semantic Search**: Leverage vector embeddings to find relevant content from uploaded documents
- **AI-Powered QA**: Ask questions and get contextually relevant answers using Google Gemini 2.5 Flash
- **Vector Storage**: Efficient storage and retrieval using PostgreSQL with PGVector extension
- **REST API**: Simple HTTP endpoints for document upload and question answering

## Architecture

The application is built with a layered architecture:

- **Controller Layer**: REST endpoints for document upload and queries
- **Service Layer**: Business logic for document processing and retrieval
- **Integration**: Spring AI framework for LLM and embedding model integration
- **Data Storage**: PostgreSQL with PGVector for vector similarity search

## Prerequisites

Before running the application, ensure you have:

- **Java 21** or higher
- **Maven 3.6+**
- **PostgreSQL 14+** with PGVector extension installed
- **Google Gemini API Key** (available from [Google AI Studio](https://ai.google.dev))

### PostgreSQL Setup

1. Install PostgreSQL and ensure it's running
2. Create a database for the application:
   ```sql
   CREATE DATABASE rag_db;
   ```
3. Install PGVector extension:
   ```sql
   CREATE EXTENSION vector;
   ```

## Configuration

Set the following environment variables before running the application:

```bash
export GEMINI_API_KEY=your_google_gemini_api_key
export DB_USERNAME=your_postgres_username
export DB_PASSWORD=your_postgres_password
```

You can also modify the `application.properties` file to change:
- PostgreSQL connection details
- Vector embedding dimensions (default: 768)
- AI model selection (currently using Gemini 2.5 Flash)
- Vector store schema initialization

## Building and Running

### Build the Project

```bash
./mvnw clean package
```

### Run the Application

```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### Upload Documents
**POST** `/v1/upload`

Upload one or more documents to the system for indexing.

**Parameters:**
- `files` (multipart file) - Document files to upload

**Example:**
```bash
curl -X POST -F "files=@document1.pdf" -F "files=@document2.docx" \
  http://localhost:8080/v1/upload
```

### Ask Questions
**GET** `/v1/ask`

Ask a question and get an AI-generated answer based on the indexed documents.

**Parameters:**
- `question` (string) - Your question

**Example:**
```bash
curl "http://localhost:8080/v1/ask?question=What%20is%20the%20main%20topic?"
```

**Response:**
```json
{
  "answer": "Based on the documents, the main topic is..."
}
```

## Technology Stack

- **Framework**: Spring Boot 4.0.5
- **AI/LLM**: 
  - Google Gemini 2.5 Flash (Chat Model)
  - Google Gemini Embedding API (Vector Embeddings)
- **Vector Store**: PostgreSQL with PGVector
- **Document Processing**: Apache Tika
- **Language**: Java 21
- **Build Tool**: Maven

## Dependencies

- Spring Boot Starter WebMVC
- Spring AI Advisors Vector Store
- Spring AI Google GenAI Starter
- Spring AI PGVector Vector Store
- Spring AI Tika Document Reader
- PostgreSQL JDBC Driver

## License

This project is provided as-is for educational and development purposes.

## Support

For issues or questions, please check:
- Application logs in the console output
- PostgreSQL connection and PGVector installation
- Google Gemini API key validity
- Document format compatibility with Apache Tika
