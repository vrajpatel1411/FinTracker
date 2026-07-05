FROM openjdk:18-jdk-alpine3.15

WORKDIR /app

COPY target/personalExpense-0.0.1.jar app.jar

EXPOSE 8080

# FIXED: JVM option must come *before* -jar
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]