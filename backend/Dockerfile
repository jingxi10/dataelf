# 直接使用本地构建好的 jar
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/ai-data-platform-1.0.0.jar app.jar
RUN mkdir -p /app/logs /app/uploads
EXPOSE 8080
ENTRYPOINT ["java", "-Xms512m", "-Xmx1024m", "-jar", "app.jar"]
