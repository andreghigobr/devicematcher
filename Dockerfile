# Build stage
FROM gradle:8.13-jdk21 AS build
WORKDIR /app
COPY . .
RUN ./gradlew build --no-daemon -x test

# Runtime stage
FROM openjdk:21-jdk
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Configure JVM for containerized environments
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseContainerSupport"
ENV SPRING_PROFILES_ACTIVE=dev

# Expose application port
EXPOSE 8080

# Health check to verify the application is running correctly
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD curl -f http://localhost:8080/ || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} -jar app.jar"]