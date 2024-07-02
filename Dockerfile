# Builder stage WITH MAVEN
FROM maven:3.8.5-openjdk-17-slim AS builder
WORKDIR /application
COPY pom.xml .
COPY src ./src
RUN mvn clean package
RUN java -Djarmode=layertools -jar target/*.jar extract

# Final stage
FROM openjdk:17-jdk-alpine
WORKDIR application
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
EXPOSE 8080
