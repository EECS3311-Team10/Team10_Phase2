# Stage 1: Build with Maven
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run with a universal OpenJDK 17 image
FROM eclipse-temurin:17-jdk
COPY --from=build /target/consultlink-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]