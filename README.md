# MindLog - AI-Powered Mental Health Journaling App

A modern web application that combines personal journaling with AI-powered insights to help users track and understand their mental health patterns.

## 🚀 Features

- **Daily Journaling** - Write about your thoughts, feelings, and experiences
- **AI-Powered Insights** - Get meaningful reflections on your entries using local AI (Ollama)
- **Smart Categorization** - Automatic clustering of notes into meaningful themes
- **Mental Health Tracking** - Visual patterns and trends over time
- **Privacy-First** - All AI processing happens locally on your machine

## 🏗️ Architecture

This is a monorepo containing:

- **Backend**: Spring Boot REST API with PostgreSQL
- **Frontend**: React TypeScript with Material-UI
- **AI Integration**: Local Ollama models for privacy
- **Database**: PostgreSQL for data persistence

## 📁 Project Structure

```
mindlog/
├── backend/                 # Spring Boot application
│   ├── src/main/java/com/mindlog/
│   │   ├── controller/      # REST API endpoints
│   │   ├── service/         # Business logic
│   │   ├── model/          # Data models
│   │   ├── repository/     # Data access layer
│   │   └── config/         # Configuration
│   └── pom.xml
├── frontend/               # React TypeScript application
│   ├── src/
│   │   ├── components/     # React components
│   │   └── services/       # API services
│   └── package.json
├── shared/                 # Shared types and utilities
│   └── types/
├── docker-compose.yml      # Development environment
└── package.json           # Root scripts
```

## 🛠️ Prerequisites

- **Java 17+**
- **Node.js 16+**
- **PostgreSQL 12+**
- **Ollama** (for local AI processing)

## 🚀 Quick Start

### 1. Install Dependencies

```bash
# Install root dependencies
npm install

# Install frontend dependencies
cd frontend && npm install

# Install backend dependencies (Maven will handle this)
cd backend && ./mvnw clean install
```

### 2. Set Up Database

```bash
# Start PostgreSQL (if not already running)
# Create database 'mood' with user 'postgres' and password '123'
```

### 3. Set Up Ollama

```bash
# Install Ollama (https://ollama.ai)
# Pull the required model
ollama pull gemma3:4b
ollama pull mxbai-embed-large
```

### 4. Run the Application

```bash
# Run both frontend and backend
npm run dev

# Or run separately:
npm run dev:backend    # Backend on http://localhost:8080
npm run dev:frontend   # Frontend on http://localhost:3000
```

### 5. Using Docker (Alternative)

```bash
# Run everything with Docker
docker-compose up
```

## 🔧 Configuration

### Backend Configuration

Edit `backend/src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/mood
spring.datasource.username=postgres
spring.datasource.password=123

# AI Service
ai.service.url=http://localhost:11434
ai.service.model=gemma3:4b
ai.service.enabled=true
```

### Frontend Configuration

The frontend automatically proxies API calls to the backend. No additional configuration needed.

## 📚 API Endpoints

### Notes
- `POST /api/notes/{userId}` - Create a new note
- `GET /api/notes/{userId}` - Get all notes for a user
- `PUT /api/notes/{userId}/{noteId}` - Update a note
- `DELETE /api/notes/{userId}/{noteId}` - Delete a note
- `GET /api/notes/{userId}/summary` - Get AI insights

### Users
- `POST /api/users` - Create a new user
- `GET /api/users/{userId}` - Get user details

## 🧠 AI Features

### Note Clustering
The app automatically categorizes notes into 17 different themes:
- Work & Career
- Money & Finances
- Relationships (Romantic, Family, Friends)
- Mental Health (Anxiety, Depression, Loneliness, Positive)
- Physical Health & Fitness
- Sleep & Rest
- Personal Growth
- Life Transitions
- Creativity & Hobbies
- External World
- Open Reflections

### AI Insights
Using local Ollama models, the app provides:
- Personalized reflections on your entries
- Pattern recognition
- Emotional context analysis
- Supportive responses without therapeutic language

## 🧪 Testing

```bash
# Run backend tests
cd backend && ./mvnw test

# Run frontend tests
cd frontend && npm test

# Run all tests
npm run test
```

## 🚀 Deployment

### Backend Deployment
```bash
cd backend
./mvnw clean package
java -jar target/mindlog-0.0.1-SNAPSHOT.jar
```

### Frontend Deployment
```bash
cd frontend
npm run build
# Deploy the build/ folder to your web server
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

If you encounter any issues:
1. Check the logs in both frontend and backend
2. Ensure Ollama is running with the correct models
3. Verify database connectivity
4. Check CORS configuration if frontend can't connect to backend

## 🔮 Future Enhancements

- [ ] User authentication and authorization
- [ ] Mobile app (React Native)
- [ ] Advanced analytics and charts
- [ ] Export functionality
- [ ] Multiple AI model support
- [ ] Collaborative journaling
- [ ] Mood tracking with visualizations
