FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

FROM gcr.io/distroless/java21-debian12
WORKDIR /
COPY --from=builder /app/target/*.jar /app.jar
ENV JAVA_TOOL_OPTIONS="-Xms256m -Xmx512m -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE=dev
USER nonroot
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app.jar"]