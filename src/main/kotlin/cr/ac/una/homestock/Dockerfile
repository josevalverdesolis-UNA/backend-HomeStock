# -------------------------------
# Dockerfile para Render - Spring Boot (Kotlin)
# -------------------------------

# 1. Etapa de construcción
FROM gradle:8.7-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon

# 2. Etapa de ejecución
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
