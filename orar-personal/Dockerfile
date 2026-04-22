# Pasul 1: Construim aplicația folosind Maven și Java 17 (Amazon Corretto)
FROM maven:3.8.4-amazoncorretto-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Pasul 2: Rulăm aplicația folosind Java 17 (Amazon Corretto)
FROM amazoncorretto:17-alpine-jdk
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]