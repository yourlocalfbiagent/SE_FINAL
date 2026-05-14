# Stage 1: Build the application
FROM maven:3.9.9-eclipse-temurin-17 AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy only the pom.xml first to cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:17-jre-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the built jar file from the builder stage
# Using *.jar ensures it picks up the correct generated artifact name
COPY --from=builder /app/target/*.jar app.jar

# Explicitly set the server port so Spring Boot uses 8080 even without application.properties
ENV SERVER_PORT=8080

# Expose the port the ERP module runs on
EXPOSE 8080

# Run the jar file with optimized memory settings for container environments
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Xmx512m", "-jar", "app.jar"]
