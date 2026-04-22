# IntelliDocs AI - API Test Guide

## ✅ Setup Complete

### Environment Configuration
- **Java Version**: 21.0.10
- **JAVA_HOME**: `D:\Madhu\Softwares\jdk-21\jdk-21.0.10`
- **Maven Build**: ✓ Successful
- **Application JAR**: `D:\Madhu\JavaRAGSystemProject\intellidocs-ai\target\intellidocs-ai-0.0.1-SNAPSHOT.jar`

### Required Environment Variables
```
JWT_SECRET=your-super-secret-jwt-key-that-is-at-least-64-characters-long
OPENAI_API_KEY=sk-demo-key
JAVA_HOME=D:\Madhu\Softwares\jdk-21\jdk-21.0.10
```

### Required Services (Docker)
- ✓ PostgreSQL (localhost:5432)
- ✓ Redis (localhost:6379)
- ✓ RabbitMQ (localhost:5672)

## How to Start the Application

### Option 1: Using the Batch Script
```batch
cd D:\Madhu\JavaRAGSystemProject\intellidocs-ai
start-app.bat
```

### Option 2: Using PowerShell (with Java 21)
```powershell
$env:JAVA_HOME="D:\Madhu\Softwares\jdk-21\jdk-21.0.10"
$env:Path="D:\Madhu\Softwares\jdk-21\jdk-21.0.10\bin;"+$env:Path
$env:JWT_SECRET="your-super-secret-jwt-key-that-is-at-least-64-characters-long"
$env:OPENAI_API_KEY="sk-demo-key"

cd D:\Madhu\JavaRAGSystemProject\intellidocs-ai
java -jar target/intellidocs-ai-0.0.1-SNAPSHOT.jar
```

### Option 3: Using Maven
```powershell
$env:JAVA_HOME="D:\Madhu\Softwares\jdk-21\jdk-21.0.10"
$env:Path="D:\Madhu\Softwares\jdk-21\jdk-21.0.10\bin;"+$env:Path
$env:JWT_SECRET="your-super-secret-jwt-key-that-is-at-least-64-characters-long"
$env:OPENAI_API_KEY="sk-demo-key"

cd D:\Madhu\JavaRAGSystemProject\intellidocs-ai
.\mvnw.cmd spring-boot:run
```

## Test API: User Registration

### Endpoint
```
POST http://localhost:8080/api/v1/auth/register
```

### Request Headers
```
Content-Type: application/json
```

### Request Body
```json
{
  "fullName": "Madhu Test",
  "email": "madhu@test.com",
  "password": "password123",
  "companyName": "Test Company"
}
```

### Test Using PowerShell
```powershell
$body = @{
    fullName = "Madhu Test"
    email = "madhu@test.com"
    password = "password123"
    companyName = "Test Company"
} | ConvertTo-Json

$response = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/auth/register" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body

Write-Host "Status: $($response.StatusCode)"
Write-Host "Response: $($response.Content)"
```

### Test Using curl (if available)
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

### Expected Response (Success - 201 or 200)
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "userId": "uuid",
    "email": "madhu@test.com",
    "fullName": "Madhu Test",
    "companyName": "Test Company"
  }
}
```

## Health Check

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -Method GET
```

## API Documentation

Access Swagger UI after the application starts:
- **URL**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs

## Troubleshooting

### Issue: "Port 8080 already in use"
```powershell
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Issue: "PostgreSQL connection failed"
```powershell
# Check Docker containers
docker-compose ps

# Restart PostgreSQL
docker-compose restart postgres
```

### Issue: "JWT_SECRET not found"
Make sure to set the environment variable before starting:
```powershell
$env:JWT_SECRET="your-super-secret-jwt-key-that-is-at-least-64-characters-long"
```

## Important Files
- **POM**: `D:\Madhu\JavaRAGSystemProject\intellidocs-ai\pom.xml` (Java 21 configured)
- **Config**: `D:\Madhu\JavaRAGSystemProject\intellidocs-ai\src\main\resources\application.yml`
- **Start Script**: `D:\Madhu\JavaRAGSystemProject\intellidocs-ai\start-app.bat`

