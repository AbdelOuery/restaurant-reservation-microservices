FROM amazoncorretto:17.0.8-alpine3.18
WORKDIR /app
COPY reservation-service-app/target/*.jar reservation-service.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "reservation-service.jar"]
