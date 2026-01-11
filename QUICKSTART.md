# Quick Start Guide

## Prerequisites Check

Before starting, ensure you have:
- ✅ Java 17 or higher installed
- ✅ MySQL 8.0 running
- ✅ Node.js 18+ installed
- ✅ Gemini API key configured

## Step 1: Create Database

Open MySQL and run:
```sql
CREATE DATABASE chatbot_platform;
```

Or the database will be created automatically when you start the backend.

## Step 2: Install Dependencies

### Backend
```bash
cd backend
mvn clean install
```

### Frontend
```bash
cd frontend
npm install
```

## Step 3: Start the Application

### Terminal 1 - Start Backend
```bash
cd backend
mvn spring-boot:run
```

Wait for the message: "Started ChatbotPlatformApplication"
Backend will be running on: http://localhost:8080

### Terminal 2 - Start Frontend
```bash
cd frontend
npm run dev
```

Frontend will be running on: http://localhost:5173

## Step 4: Access the Application

Open your browser and go to: **http://localhost:5173**

## Step 5: Test the Application

### 1. Register Your Company
- Click "Register"
- Enter:
  - Company Name: "TechCorp"
  - Domain: "techcorp.com"
  - Email: "admin@techcorp.com"
  - Password: "password123"
- Click "Register"

### 2. Add Knowledge Base FAQs
- Navigate to "Knowledge Base"
- Click "Add New FAQ"
- Add some sample FAQs:

**FAQ 1:**
- Q: What are your business hours?
- A: We are open Monday-Friday, 9 AM to 6 PM EST.

**FAQ 2:**
- Q: How do I contact support?
- A: You can email us at support@techcorp.com or call 1-800-TECH-CORP.

**FAQ 3:**
- Q: What is your refund policy?
- A: We offer a 30-day money-back guarantee on all products.

**FAQ 4:**
- Q: Do you offer free shipping?
- A: Yes, we offer free shipping on orders over $50.

**FAQ 5:**
- Q: Where are you located?
- A: Our headquarters is in San Francisco, California.

### 3. Test the Chatbot
- Go to "Chat"
- Try these questions:
  - "What are your business hours?" → Should return exact FAQ answer
  - "How can I contact you?" → Should return support info
  - "Tell me about your refund policy" → Should return refund policy
  - "Do you have a mobile app?" → AI will try to answer or say "I don't have that information"

### 4. Review Unanswered Questions
- Go to "Unanswered"
- You'll see questions the AI couldn't answer
- Click "Approve & Add to KB"
- Provide an answer
- The question becomes a new FAQ!

### 5. Test Multi-Tenant Isolation
- Logout
- Register another company: "RetailCo"
- Add different FAQs
- Verify you can't see TechCorp's FAQs
- Each company has isolated data

## Troubleshooting

### Backend won't start
- Check MySQL is running
- Verify database credentials in `application.yml`
- Check Java version: `java -version`

### Frontend won't start
- Delete `node_modules` and run `npm install` again
- Check Node version: `node --version`

### API errors
- Verify Gemini API key is correct in `application.yml`
- Check backend console for error messages

### Database errors
- Ensure MySQL is running
- Create database manually: `CREATE DATABASE chatbot_platform;`
- Check username/password in `application.yml`

## Default Configuration

**Backend:**
- Port: 8080
- Context Path: /api
- Database: chatbot_platform
- MySQL User: root
- MySQL Password: root

**Frontend:**
- Port: 5173
- API Proxy: http://localhost:8080

## Next Steps

Once everything is running:
1. ✅ Register your company
2. ✅ Add 5-10 FAQs to knowledge base
3. ✅ Test the chatbot with various questions
4. ✅ Review and approve unanswered questions
5. ✅ Explore the admin dashboard

## Need Help?

Check the main README.md for detailed documentation and architecture overview.

---

**Enjoy your AI-powered chatbot platform! 🚀**
