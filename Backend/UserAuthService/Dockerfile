FROM amazoncorretto:21-alpine3.20

WORKDIR /app

COPY target/UserAuthService-0.0.1.jar app.jar

EXPOSE 8080

# FIXED: JVM option must come *before* -jar
ENTRYPOINT ["java", "-Dspring.profiles.active=dev", "-jar", "app.jar"]
