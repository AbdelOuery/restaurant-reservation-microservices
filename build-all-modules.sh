#!/bin/sh

cd eureka-server && mvn clean package -DskipTests && cd ..
cd api-gateway && mvn clean package -DskipTests && cd ..
cd restaurant-service && mvn clean package -DskipTests && cd ..
cd reservation-service && mvn clean package -DskipTests && cd ..
