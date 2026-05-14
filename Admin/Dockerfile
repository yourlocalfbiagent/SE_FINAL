# Stage 1: Build the application
FROM maven:3.9.9-eclipse-temurin-21 AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the built jar file from the builder stage
COPY --from=builder /app/target/admin.jar app.jar

# Explicitly set the server port so Spring Boot uses 8081 even without application.properties
ENV SERVER_PORT=8081

# Expose the port the Admin module runs on
EXPOSE 8081

# Run the jar file with optimized memory settings for container environments
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Xmx512m", "-jar", "app.jar"]
