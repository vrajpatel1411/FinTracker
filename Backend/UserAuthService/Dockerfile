
#FROM eclipse-temurin:21-noble
#
#WORKDIR /app
#
#COPY target/UserAuthService-0.0.1.jar app.jar
#
#EXPOSE 8080
#
## FIXED: JVM option must come *before* -jar
#ENTRYPOINT ["java", "-Dspring.profiles.active=dev", "-jar", "app.jar"]


FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

FROM gcr.io/distroless/java21-debian12
WORKDIR /
COPY --from=builder /app/target/*.jar /app.jar
ENV JAVA_TOOL_OPTIONS="-Xms256m -Xmx512m -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE=dev
#ENV SPRING_PROFILES_ACTIVE=prod
USER nonroot
EXPOSE 8084
ENTRYPOINT ["java","-jar","/app.jar"]

