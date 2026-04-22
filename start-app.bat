@echo off
REM Set environment variables for the application
set JAVA_HOME=D:\Madhu\Softwares\jdk-21\jdk-21.0.10
set PATH=D:\Madhu\Softwares\jdk-21\jdk-21.0.10\bin;%PATH%
set JWT_SECRET=your-super-secret-jwt-key-that-is-at-least-64-characters-long-for-production
set OPENAI_API_KEY=sk-test-key-for-development

REM Run Maven Spring Boot
echo Starting IntelliDocs AI Application...
mvnw.cmd spring-boot:run

pause

