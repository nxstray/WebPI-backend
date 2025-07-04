# Gunakan base image Maven untuk build
FROM maven:3.9.4-eclipse-temurin-17 AS build

COPY . /app

WORKDIR /app

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk

COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]