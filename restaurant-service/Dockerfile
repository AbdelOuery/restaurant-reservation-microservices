FROM amazoncorretto:17.0.8-alpine3.18
WORKDIR /app
COPY restaurant-service-app/target/*.jar restaurant-service.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "restaurant-service.jar"]
