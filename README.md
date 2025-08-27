## mindLog

Semantic journaling backend (Spring Boot). Notes are auto-categorized into themes using free, local embeddings (Ollama) and cosine similarity.

### Features
- Server-assigned subjects; users never choose categories
- Local embeddings via Ollama (`nomic-embed-text`)
- Per-note semantic assignment to curated themes (see `NoteCluster`)
- Endpoints to list subjects, filter notes by subject, and trigger assignment

## Setup

### Prerequisites
- Java 17+
- Maven
- PostgreSQL
- Ollama installed and available on `http://localhost:11434`

### Configure DB
Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mood
spring.datasource.username=postgres
spring.datasource.password=123
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### Start Ollama
```bash
ollama serve
ollama pull nomic-embed-text
```

### Run
```bash
mvn spring-boot:run
```
App: `http://localhost:8080`

## API
Base: `/api/notes`

- POST `/{userId}`: create notes (subject ignored on create)
```json
[
  { "text": "I feel overwhelmed by deadlines." },
  { "text": "I’m excited about my new painting project!" }
]
```

- POST `/{userId}/auto-cluster`: embed and assign themes per note
- GET `/{userId}/subjects`: list user’s distinct subjects
- GET `/{userId}/subject/{subject}`: notes by subject (URL-encode)
- GET `/subjects`: all allowed theme labels

## Themes
Defined in `com.mindlog.model.NoteCluster` with rich descriptions to improve matching (e.g., Work stress, Romantic relationships, Excitement & anticipation, Debt & bills, Sleep problems, Random musings).

## Tech
Spring Boot 3, Spring Web, JPA, PostgreSQL, Ollama embeddings.

## License
MIT (see `LICENSE`).
