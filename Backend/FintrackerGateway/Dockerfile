FROM amazoncorretto:21-alpine3.20

WORKDIR /app

COPY target/FintrackerGateway-0.0.1.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","-Dspring.profiles.active=dev","app.jar"]

