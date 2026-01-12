# Restaurant Reservation System

Project was made in Java 17 using Spring Boot v3.5.9 and Maven 3.9

#### Requirements
- Maven
- Unix based terminal

## Setup Instructions

### 1. Introduction

The project is comprised of 4 main components which were placed in the same repository for convience

- **api-gateway**: API Gateway serving as our Load Balancer
- **eureka-server**: Service Registry and Discovery provider from which the gateway will be locating our services.
- **restaurant-service**: The restaurant microservice
- **reservation-service**: The reservation system microservice

There is also a postgreSQL container for each service. 

### 2. Building the components

We can build every project by running the supplied script

```
chmod +x ./build-all-modules.sh
./build-all-modules.sh
```

OR manually building every project individually

```
cd eureka-server
mvn clean package -DskipTests
```
```
cd api-gateway
mvn clean package -DskipTests
```
```
cd restaurant-service
mvn clean package -DskipTests
```
```
cd reservation-service
mvn clean package -DskipTests
```

### 3. Running the containers

From the root of the repository we run this command

`docker-compose up --build -d`

This will spin up two instances of each microservice (restaurant-service and reservation-service) with an API gateway and Eureka Server.

### 4. Checking the logs

`docker-compose logs -f`

If everything went well, we now should see that our gateway is up and running by navigating to: http://localhost:8080/actuator/info

We can also check the Eureka Dashboard: http://localhost:8761/

### 5. Running up the Test

Unit and integration tests (TestContainers) were written to cover a variety of technical scenarios.

Because

We can run them by executing the following command inside the respective project's folder:

`cd restaurant-service`

`mvn test`

##  Calling the endpoints

The optimal way to test our endpoints by is using the supplied Bruno Collection (contains all the requests), each request goes through the API gateway (http://localhost:8080) and gets re-routed to the appropriate service, each requiring a JWT Token as a security measure.

To keep it simple, I didn't implement a fully fledged user signup/signin feature, instead I've hardcoded some credentials for convenience.

**We can acquire a JWT bearer Token through the following Authentication endpoint that's exposed on the gateway.**

POST: http://localhost:8080/auth/login

```
{
    "username": "user",
    "password": "password"
}
```


We could also test every service directly using Swagger UI.

- Restaurant Service: http://localhost:8081/swagger-ui/index.html
- Reservation Service: http://localhost:8083/swagger-ui/index.html

### Reservation Test Scenario

I have inserted some sample data comprising of some restaurants and tables in the **restaurant-service**, the user can make a reservation by calling the **reservation-service** endpoint, we can then call other endpoints to confirm or cancel the restaurant.

## Architecture & Technical decisions

### 1. Project Folder Structure

I've opted for a multi-module project structure for every service to optimize separation of cencerns between modules:

- **service-api**: Contains the API code (DTOs, Enums, Exceptions)
- **service-app**: Contains the logic code behind the app (Controllers, Entities, Services, Tests...)
- **service-client**: Only contains the FeignClient to be included by other microservices

When it comes to package organization, a feature based architecture was used to allow for better code organization if the app gets larger, except for the API gateway where I opted for a layer based architecture since there is no business code.

### 2. Microservice inter-communication

Both microservices call each other when it comes to handling availability of tables in restaurants or when booking a reservation.

The **restaurant-service** will call **reservation-service** to check if the tables at a certain restaurant are already booked or not, and viceversa, the **reservation-service** will call the **restaurant-service** during the reservation process to check if there are any tables that correspond to the customer's needs.

### 3. Technology choices

#### Flyway

For database versioning and schema migrations, I chose Flyway over Liquibase because it provides a simple and friendly way to manage database changes in a small microservice architecture, also, we won't be needing the unnecessary complexity that Liquidbase provides to this small application.

#### TestContainers

When it comes to integration testing, I've opted for TestContainers to simulate true end to end testing between the two microservices because it acts as a real environment similar to production, Spring Cloud is more suitable for microservices belonging to different teams and evolving at different rates with different release cycles thus making it critical to validate the contracts and to ensure backwards compatibility. 

**Running the TestContainers integration tests**

We first have to build the docker images of each microservice.

```
cd restaurant-service
docker build -t restaurant-service:latest .
```

```
cd ../reservation-service
docker build -t reservation-service:latest .
```

Then we navigate into the app folder or the respective microservice

- Example 1 (Runs the Availability <-> Reservation integration test)

```
cd restaurant-service/restaurant-service-app
mvn test -Dtest=AvailabilityControllerIntegrationContainerTest
```

- Example 2 (Runs the Reservation <-> Availability integration test)

```
cd reservation-service/reservation-service-app
mvn test -Dtest=ReservationControllerIntegrationContainerTest
```
