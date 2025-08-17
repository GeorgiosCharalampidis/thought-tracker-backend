# mindLog

mindLog is a Spring Boot application designed to track and analyze user Notes and moods. It uses the DeepSeek R1 model running locally via Ollama to provide insights and summaries of user Notes.

## Getting Started

### Prerequisites

- Java 17
- Maven
- DeepSeek R1 model running locally via Ollama

### Installation

1. Clone the repository:
   ```sh
   git clone https://github.com/yourusername/mindlog.git
   cd mindlog
   ```

2. Build the project using Maven:
   ```sh
   mvn clean install
   ```

3. Run the application:
   ```sh
   mvn spring-boot:run
   ```

### Configuration

Ensure that the DeepSeek R1 model is running locally and accessible at `http://localhost:11434/api/deepseek`.

### API Endpoints

#### User Endpoints

- **Create User**
  ```http
  POST /api/users
  ```
  Request Body:
  ```json
  {
      "username": "john_doe",
      "email": "john@example.com",
      "password": "password123"
  }
  ```

- **Get User by Username**
  ```http
  GET /api/users/{username}
  ```

- **Get All Users**
  ```http
  GET /api/users
  ```

- **Update User**
  ```http
  PUT /api/users/{username}
  ```
  Request Body:
  ```json
  {
      "email": "john_new@example.com",
      "password": "newpassword123"
  }
  ```

- **Delete User**
  ```http
  DELETE /api/users/{username}
  ```

#### Note Endpoints

- **Create Note for User**
  ```http
  POST /api/Notes/{userName}
  ```
  Request Body:
  ```json
  {
      "text": "Today was a good day!",
      "moodRating": 4,
      "date": "2023-10-15",
      "user": {
          "id": 1
      }
  }
  ```

- **Get Notes by User**
  ```http
  GET /api/Notes/{userName}
  ```

- **Get Notes by User and Date Range**
  ```http
  GET /api/Notes/user/{userId}/date-range?startDate=2023-10-01&endDate=2023-10-15
  ```

- **Delete Note**
  ```http
  DELETE /api/Notes/{NoteId}
  ```

- **Delete All Notes**
  ```http
  DELETE /api/Notes
  ```

- **Get User Notes Summary**
  ```http
  GET /api/Notes/{username}/summary
  ```

## License

This project is licensed under the MIT License. See the `LICENSE` file for details.
