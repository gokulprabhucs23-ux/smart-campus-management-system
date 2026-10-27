# Stage 1: Build the Spring Boot application using Maven and JDK 17
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /workspace

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and package application
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime image using lightweight JRE 17
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Run as non-root user
RUN groupadd -r campus && useradd -r -g campus campus
USER campus

# Copy the built jar
COPY --from=builder /workspace/target/smart-campus-backend-1.0.0.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
