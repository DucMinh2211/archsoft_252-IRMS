# Stage 1: Build
FROM gradle:8.7-jdk17 AS build
WORKDIR /app

# Copy gradle wrapper and config files first to cache dependencies
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle .
COPY settings.gradle .

# Copy shared common-lib first as it is a dependency for most services
COPY shared/common-lib/ shared/common-lib/

# Pre-resolve dependencies for the specific service to leverage Docker cache
# This layer will only be rebuilt if build.gradle or settings.gradle change
RUN ./gradlew :services:menu-service:dependencies --no-daemon

# Copy the service source code
COPY services/menu-service/ services/menu-service/

# Build the executable bootJar (skip tests for faster CI/CD)
RUN ./gradlew :services:menu-service:bootJar --no-daemon -x test

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Create a non-root user for better security
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Copy the built jar from the build stage
COPY --from=build /app/services/menu-service/build/libs/*-SNAPSHOT.jar app.jar

# JVM Performance & Container awareness:
# - MaxRAMPercentage: Dynamically scale memory based on container limits
# - ExitOnOutOfMemoryError: Ensure container restarts on heap exhaustion
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8082
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
