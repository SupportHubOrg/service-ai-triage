#Stage 1: Build environment
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace/app

#Copy Maven wrapper and POM to cache dependencies
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

#Download dependencies offline to leverage Docker layer caching
RUN ./mvnw dependency:go-offline

#Copy the application source code and compile the JAR
COPY src src
RUN ./mvnw clean package -DskipTests

#Stage 2: Production environment
FROM eclipse-temurin:17-jre-alpine AS final
WORKDIR /app

#Create a restricted non-root user specifically for the Spring Boot application
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

#Copy the compiled executable JAR from the build stage
COPY --from=build /workspace/app/target/ai-triage-0.0.1-SNAPSHOT.jar app.jar

#Expose the standard port
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]