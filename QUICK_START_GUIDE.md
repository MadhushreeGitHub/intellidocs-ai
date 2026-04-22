# 🚀 IntelliDocs AI - Quick Start Guide

## ✅ Current Status
- **Docker Services**: ✅ Running (PostgreSQL, Redis, RabbitMQ)
- **Java 21**: ✅ Configured
- **Application**: Starting in separate window...

## 📋 Everyday Mandatory Steps to Start Application

### **MANDATORY STEP 1: Start Docker Services**
```powershell
cd D:\Madhu\JavaRAGSystemProject\intellidocs-ai
docker-compose up -d
```
**Why?** Required for database, cache, and messaging.

### **MANDATORY STEP 2: Set Environment Variables**
```powershell
$env:JAVA_HOME="D:\Madhu\Softwares\jdk-21\jdk-21.0.10"
$env:Path="D:\Madhu\Softwares\jdk-21\jdk-21.0.10\bin;" + $env:Path
$env:JWT_SECRET="your-super-secret-jwt-key-that-is-at-least-64-characters-long"
$env:OPENAI_API_KEY="sk-your-openai-api-key-here"
```

### **MANDATORY STEP 3: Start Application**
**Option A: Using Batch Script (Recommended)**
```batch
cd D:\Madhu\JavaRAGSystemProject\intellidocs-ai
start-app.bat
```

**Option B: Using PowerShell**
```powershell
cd D:\Madhu\JavaRAGSystemProject\intellidocs-ai
java -jar target\intellidocs-ai-0.0.1-SNAPSHOT.jar
```

**Option C: Using Maven**
```powershell
cd D:\Madhu\JavaRAGSystemProject\intellidocs-ai
.\mvnw.cmd spring-boot:run
```

## 🧪 Testing the Register Endpoint

### **Test Command (PowerShell)**
```powershell
$body = @{
    fullName = "Madhu Test"
    email = "madhu@test.com"
    password = "password123"
    companyName = "Test Company"
} | ConvertTo-Json

$response = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/auth/register" `
    -Method POST -ContentType "application/json" -Body $body

Write-Host "Status: $($response.StatusCode)"
Write-Host "Response: $($response.Content)"
```

### **Test Command (curl)**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Madhu Test",
    "email": "madhu@test.com",
    "password": "password123",
    "companyName": "Test Company"
  }'
```

### **Expected Response**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "userId": "uuid-here",
    "email": "madhu@test.com",
    "fullName": "Madhu Test",
    "companyName": "Test Company",
    "token": "jwt-token-here"
  }
}
```

## 🔍 Verification Steps

### **Check if Application is Running**
```powershell
# Check port 8080
Get-NetTCPConnection -LocalPort 8080 -State Listen

# Check health endpoint
Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -Method GET
```

### **Check Docker Services**
```powershell
docker-compose ps
```

### **Check Java Processes**
```powershell
Get-Process java
```

## 🌐 Access Points

- **Application**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs
- **Health Check**: http://localhost:8080/actuator/health
- **PgAdmin**: http://localhost:5050 (admin@intellidocs.com / admin)
- **RabbitMQ**: http://localhost:15672 (intellidocs / rabbitmq_pass)

## 🛠️ Troubleshooting

### **If Port 8080 Not Listening**
1. Check Java application window for errors
2. Verify Docker services: `docker-compose ps`
3. Check environment variables are set
4. Kill existing Java processes: `taskkill /f /im java.exe`

### **If Database Connection Failed**
```powershell
docker-compose restart postgres
```

### **If Application Won't Start**
1. Clean and rebuild: `.\mvnw.cmd clean package -DskipTests`
2. Check Java version: `java -version`
3. Verify JAR exists: `ls target\*.jar`

## 📝 Development Workflow

### **Daily Development Start**
1. **Start Docker**: `docker-compose up -d`
2. **Set Environment**: Run the environment variable commands
3. **Start App**: `start-app.bat`
4. **Test**: Use the test commands above
5. **Develop**: Make changes, rebuild, restart

### **After Code Changes**
```powershell
# Rebuild and restart
.\mvnw.cmd clean package -DskipTests
# Then restart the application
```

## 📋 Summary Checklist

- ✅ **Docker services running** (PostgreSQL, Redis, RabbitMQ)
- ✅ **Java 21 configured** (JAVA_HOME and PATH)
- ✅ **Environment variables set** (JWT_SECRET, OPENAI_API_KEY)
- ✅ **Application started** (port 8080 listening)
- ✅ **Health check passes** (/actuator/health)
- ✅ **API endpoint tested** (/api/v1/auth/register)

---

**🎯 Next: Check the Java application window for startup logs, then run the test commands above!**

