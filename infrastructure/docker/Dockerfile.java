# Generic Dockerfile for Java Spring Boot Services
# Usage: docker build --build-arg SERVICE_NAME=menu-service -t irms/menu-service .

# Stage 1: Build
FROM gradle:8.7-jdk17 AS build
WORKDIR /app
ARG SERVICE_NAME

# Copy gradle wrapper and config files
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle .
COPY settings.gradle .

# Copy shared common-lib
COPY shared/common-lib/ shared/common-lib/

# Resolve dependencies for caching (using specific service to keep image lean)
RUN ./gradlew :services:${SERVICE_NAME}:dependencies --no-daemon

# Copy source code of the service
COPY services/${SERVICE_NAME}/ services/${SERVICE_NAME}/

# Build executable bootJar
RUN ./gradlew :services:${SERVICE_NAME}:bootJar --no-daemon -x test

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
ARG SERVICE_NAME
ENV SERVICE_NAME_ENV=${SERVICE_NAME}

# Security: Non-root user
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Copy built artifact
COPY --from=build /app/services/${SERVICE_NAME}/build/libs/*-SNAPSHOT.jar app.jar

# JVM Container awareness
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

# EXPOSE port should be managed via docker-compose.yml or -p
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
