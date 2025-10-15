# ---------- Build ----------
FROM gradle:8.7-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle --no-daemon clean bootJar

# ---------- Run ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /opt/app
COPY --from=builder /app/build/libs/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]
