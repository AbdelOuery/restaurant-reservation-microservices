FROM amazoncorretto:17.0.8-alpine3.18
WORKDIR /app
COPY target/*.jar eureka-server.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "eureka-server.jar"]
