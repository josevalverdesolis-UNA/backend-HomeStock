# -------------------------------
# Dockerfile para Render - Spring Boot (Kotlin)
# -------------------------------
# ---- Build stage ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app


# Copia Gradle wrapper y archivos de build primero para cacheo
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./


# Descarga dependencias (sin compilar el código aún)
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true


# Copia el código fuente
COPY src src


# Construye el JAR (saltamos tests por ahora)
RUN ./gradlew clean bootJar -x test --no-daemon


# ---- Run stage ----
FROM eclipse-temurin:17-jre AS run
WORKDIR /app


# Copia el JAR construido
COPY --from=build /app/build/libs/*.jar /app/app.jar


# Render provee $PORT; Spring ya lo soporta desde application.yml
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
