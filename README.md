# Chatbot Backend

A Spring Boot 3.x application that serves as the backend for the AI Chatbot Customer Service application.
It integrates with the Groq API for AI completion and uses H2 to store chat history locally.

## Prerequisites
- Java 17
- Maven 3.6+

## Configuration
Before running the application, you need to provide your Groq API key:
1. First, generate an API key at [Groq Console](https://console.groq.com/keys).
2. Open `src/main/resources/application.properties` and replace `YOUR_GROQ_API_KEY` with your actual key:
   ```properties
   groq.api.key=gsk_your_actual_key_here
   ```

## Running the Application
To run the Spring Boot application locally, you can use the Maven wrapper or your local maven setup:

```bash
mvn spring-boot:run
```

The application will start on port 8080.

## Database
This project uses an in-memory H2 database.
You can access the H2 console to view the stored Chat Sessions and Chat Messages at:
http://localhost:8080/h2-console

- **JDBC URL:** `jdbc:h2:mem:chatbotdb`
- **Username:** `sa`
- **Password:** *(leave blank)*

## API Endpoints
- `POST /api/chat/send` (Body: `{ "sessionId": "UUID", "message": "Hi" }`)
- `GET /api/chat/history/{sessionId}`
- `DELETE /api/chat/session/{sessionId}`
