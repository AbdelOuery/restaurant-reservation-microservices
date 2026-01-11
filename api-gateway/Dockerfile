FROM amazoncorretto:17.0.8-alpine3.18
WORKDIR /app
COPY target/*.jar api-gateway.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "api-gateway.jar"]
