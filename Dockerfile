# Docker Multi-stage build
# Stage 1: Build the application
FROM maven:3.8.4-openjdk-11-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=build /app/target/leafshot-webmin-1.0-SNAPSHOT.jar app.jar

# Create directories for data and workdir
RUN mkdir data workdir

# Expose the default port
EXPOSE 8091

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
