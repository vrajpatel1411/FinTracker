FROM openjdk:18-jdk-alpine3.15

WORKDIR /app

COPY target/FintrackerGateway-0.0.1.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","-Dspring.profiles.active=prod","app.jar"]

