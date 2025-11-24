# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

# a non-root user for better security
RUN adduser --system --group spring
USER spring

# copy the jar from the build stage
COPY --from=build /app/target/roboterapi-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8090

ENTRYPOINT ["java", "-jar", "app.jar"]