# Enterprise AI Chatbot Platform

A multi-tenant AI-powered customer service chatbot platform built with **Spring Boot** and **React**. Companies can register, manage their knowledge base, and provide AI-driven customer support with zero hallucinations.

## 🎯 Features

### Core Capabilities
- ✅ **Multi-Tenant Architecture** - Multiple companies with isolated data
- ✅ **AI-Powered Responses** - Google Gemini API integration
- ✅ **Knowledge Base Management** - CRUD operations for FAQs
- ✅ **Hallucination Prevention** - Strict context-based responses
- ✅ **Auto-Learning System** - Capture and review unanswered questions
- ✅ **JWT Authentication** - Secure role-based access control
- ✅ **Real-Time Chat Interface** - Beautiful, responsive UI

### Technology Stack

**Backend:**
- Java 17
- Spring Boot 3.2
- Spring Security + JWT
- Spring Data JPA
- MySQL 8.0
- Google Gemini AI API

**Frontend:**
- React 18
- Vite
- React Router
- Axios
- Modern CSS with animations

## 📋 Prerequisites

- Java 17 or higher
- Node.js 18+ and npm
- MySQL 8.0
- Google Gemini API Key (free at https://makersuite.google.com/app/apikey)

## 🚀 Quick Start

### 1. Database Setup

Create MySQL database:
```sql
CREATE DATABASE chatbot_platform;
```

### 2. Backend Setup

```bash
cd backend

# Configure environment variables
cp .env.example .env
# Edit .env and add your Gemini API key

# Build and run
mvn clean install
mvn spring-boot:run
```

Backend will start on `http://localhost:8080`

### 3. Frontend Setup

```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

Frontend will start on `http://localhost:5173`

## 🔑 Getting Gemini API Key

1. Visit https://makersuite.google.com/app/apikey
2. Sign in with your Google account
3. Click "Create API Key"
4. Copy the key and add it to `backend/.env`:
   ```
   GEMINI_API_KEY=your_key_here
   ```

## 📖 Usage Guide

### For Company Admins

1. **Register Your Company**
   - Go to `/register`
   - Enter company name, domain, email, and password
   - You'll be logged in automatically

2. **Manage Knowledge Base**
   - Navigate to "Knowledge Base"
   - Add FAQs with questions and answers
   - Edit or delete existing FAQs
   - These FAQs power the AI responses

3. **Review Unanswered Questions**
   - Navigate to "Unanswered"
   - See questions the AI couldn't answer
   - Approve and add answers to convert them to FAQs
   - Track frequency to prioritize common questions

### For Customers

1. **Chat with AI**
   - Login with your credentials
   - Navigate to "Chat"
   - Ask questions about the company's services
   - Get instant, accurate responses

## 🏗️ Architecture

### Multi-Tenant Design
- Each company has isolated data via `company_id`
- JWT tokens contain `companyId` for automatic tenant filtering
- All queries are scoped to the authenticated company

### AI Response Flow
1. User asks a question
2. System searches company's knowledge base
3. If high-confidence match found → return FAQ answer
4. Else → inject context + call Gemini AI
5. If AI can't answer → log as unanswered question
6. Admin reviews and adds to knowledge base

### Hallucination Prevention
- Strict prompt engineering
- Context injection from knowledge base only
- AI instructed to refuse answers outside context
- Confidence scoring and validation

## 📁 Project Structure

```
enterprise-chatbot-platform/
├── backend/
│   ├── src/main/java/com/enterprise/chatbot/
│   │   ├── ai/              # Gemini service, retrieval engine
│   │   ├── config/          # Security, CORS configuration
│   │   ├── controller/      # REST API endpoints
│   │   ├── dto/             # Request/response objects
│   │   ├── model/           # JPA entities
│   │   ├── repository/      # Database access
│   │   ├── security/        # JWT, authentication
│   │   └── service/         # Business logic
│   └── pom.xml
│
└── frontend/
    ├── src/
    │   ├── components/      # Reusable components
    │   ├── context/         # Auth context
    │   ├── pages/           # Page components
    │   ├── services/        # API calls
    │   └── styles/          # CSS files
    └── package.json
```

## 🔒 Security Features

- **JWT Authentication** - Stateless, secure tokens
- **Password Encryption** - BCrypt hashing
- **Role-Based Access** - ADMIN vs CUSTOMER roles
- **Tenant Isolation** - Automatic data filtering
- **CORS Protection** - Configured origins only

## 🎨 UI Features

- **Modern Design** - Gradient backgrounds, glassmorphism
- **Smooth Animations** - Slide-ups, fade-ins, hover effects
- **Responsive Layout** - Mobile and desktop optimized
- **Real-Time Chat** - Auto-scroll, typing indicators
- **Modal Dialogs** - Clean CRUD operations

## 📊 Database Schema

- `companies` - Company registration data
- `users` - Admin and customer accounts
- `knowledge_base` - FAQ questions and answers
- `chat_history` - All chat interactions
- `unanswered_questions` - Questions for review

## 🛠️ API Endpoints

### Authentication
- `POST /api/auth/register` - Company registration
- `POST /api/auth/login` - User login

### Chat
- `POST /api/chat` - Send message, get AI response

### Knowledge Base (Admin only)
- `GET /api/admin/kb` - List FAQs
- `POST /api/admin/kb` - Create FAQ
- `PUT /api/admin/kb/{id}` - Update FAQ
- `DELETE /api/admin/kb/{id}` - Delete FAQ

### Unanswered Questions (Admin only)
- `GET /api/admin/unanswered` - List unanswered questions
- `POST /api/admin/unanswered/{id}/approve` - Approve and add to KB
- `DELETE /api/admin/unanswered/{id}` - Reject question

## 🚢 Deployment

### Using Docker (Coming Soon)
```bash
docker-compose up
```

### Manual Deployment
1. Build backend: `mvn clean package`
2. Build frontend: `npm run build`
3. Deploy JAR and static files to your server
4. Configure MySQL connection
5. Set environment variables

## 🤝 Contributing

This is a demonstration project. Feel free to fork and customize for your needs!

## 📝 License

MIT License - feel free to use this project for learning or commercial purposes.

## 🙏 Acknowledgments

- Google Gemini AI for powerful language models
- Spring Boot for robust backend framework
- React for modern frontend development

---

**Built with ❤️ for enterprise customer support automation**
