👉 **[Click here to view Frontend code](https://github.com/nayciyilmaz/MoodMate)**

---

A Spring Boot REST API for the MoodMate mood tracking application. Provides JWT-based authentication, mood CRUD operations, and AI-powered mental health advice generation via Google Gemini API.

## Features

- **JWT Authentication:** Stateless token-based auth with Spring Security and BCrypt
- **Mood CRUD:** Create, read, update, delete mood entries with ownership validation
- **AI Advice:** Personalized mental health advice using Google Gemini API
- **Multi-Language:** Turkish, English, Spanish, Italian error messages and validations
- **Password Management:** Secure password change with current password verification
- **API Documentation:** Swagger UI via SpringDoc OpenAPI

## Tech Stack

- **Spring Boot** - REST API framework on Java 17
- **Spring Security** - JWT authentication with HMAC-SHA256
- **Spring Data JPA / Hibernate** - ORM layer
- **PostgreSQL** - Production database with HikariCP pooling
- **MapStruct** - DTO-Entity mapping with Lombok integration
- **Bean Validation** - Jakarta validation with i18n message keys
- **SpringDoc OpenAPI** - Swagger UI documentation
- **Google Gemini API** - AI advice generation via gemini-3-flash-preview

## Architecture

- **Layered Architecture** - Controller → Service → Repository → Entity
- **Global Exception Handling** - Centralized error responses with locale support
- **Error Code System** - Typed error codes (1001-9000) with HTTP status mapping
- **BCrypt Hashing** - Secure password storage
- **CORS Configuration** - Open CORS for mobile client access
- **Locale Resolution** - Accept-Language header based language detection

## Gemini AI Integration

- Analyzes last 3 days of mood entries (emoji, score, notes)
- Empathetic Turkish counselor persona
- Identifies emotional patterns and provides supportive advice
- Suggests professional help when negative emotions are intense
- Returns 2-3 paragraph warm, motivational responses

## Testing

- **JUnit 5** - Unit and integration testing
- **Mockito** - Service layer mocking
- **MockMvc** - Controller integration tests with @WithMockUser
- **H2 Database** - In-memory test database
- **89 Unit & Integration Tests** covering services, controllers, and repositories

## Multi-Language Support

- Turkish (default), English, Spanish, Italian
- All error messages and validation messages are localized
- Accept-Language header based locale resolution
