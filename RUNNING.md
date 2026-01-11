# 🚀 Running Your Application

## ✅ Backend is Starting!

The Spring Boot backend is currently compiling and starting up. You'll see messages like:
- `[INFO] Building Enterprise Chatbot Platform`
- `[INFO] Compiling source files...`
- Finally: `Started ChatbotPlatformApplication` ✅

This takes about 1-2 minutes the first time.

## 📝 What's Happening

1. **Maven** is compiling all Java files
2. **Spring Boot** is starting the web server
3. **MySQL** connection will be established
4. **Database tables** will be created automatically
5. Server will start on **http://localhost:8080**

## 🎯 Next Steps (After Backend Starts)

### 1. Start the Frontend

Open a **NEW** terminal and run:
```bash
cd D:\enterprise-chatbot-platform\frontend
npm run dev
```

Frontend will start on **http://localhost:5173**

### 2. Open Your Browser

Go to: **http://localhost:5173**

### 3. Register Your Company

- Click "Register"
- Fill in:
  - Company Name: `TechCorp`
  - Domain: `techcorp.com`
  - Email: `admin@techcorp.com`
  - Password: `password123`

### 4. Add Some FAQs

- Go to "Knowledge Base"
- Click "Add New FAQ"
- Add 5-10 questions and answers about your company

### 5. Test the Chatbot!

- Go to "Chat"
- Ask questions from your knowledge base
- Watch the AI respond!

## ⚠️ If You See Errors

### MySQL Connection Error
```
Error: Could not connect to MySQL
```
**Fix:** Make sure MySQL is running. Check MySQL Workbench.

### Port Already in Use
```
Error: Port 8080 is already in use
```
**Fix:** Stop any other applications using port 8080.

### Database Error
```
Error: Unknown database 'chatbot_platform'
```
**Fix:** The database should be created automatically. If not, run in MySQL Workbench:
```sql
CREATE DATABASE chatbot_platform;
```

## 📊 Checking if Backend is Running

Look for this message in the terminal:
```
Started ChatbotPlatformApplication in X.XXX seconds
```

Then try: **http://localhost:8080/api/auth/login** in your browser.
You should see a login page or JSON response.

## 🎉 You're All Set!

Once both servers are running:
- Backend: http://localhost:8080
- Frontend: http://localhost:5173

Open the frontend URL and start using your AI chatbot platform!

---

**Need help?** Check the error messages in the terminal and refer to the troubleshooting section above.
