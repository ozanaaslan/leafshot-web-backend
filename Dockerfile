# Docker Multi-stage build
# Stage 1: Build the application
FROM maven:3.6.3-jdk-11 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM maven:3.6.3-jdk-11
WORKDIR /app
COPY --from=build /app/target/leafshot-webmin-1.0-SNAPSHOT.jar app.jar

# Create directories for data and workdir
RUN mkdir data workdir

# Expose the default port
EXPOSE 8091

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
