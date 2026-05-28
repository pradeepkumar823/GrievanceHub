# Stage 1: Build the application
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Create uploads directory and ensure it is fully writable
RUN mkdir -p /app/uploads && chmod 777 /app/uploads
# Expose the default container port
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
