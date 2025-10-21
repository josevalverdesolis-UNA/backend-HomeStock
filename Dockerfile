# syntax=docker/dockerfile:1

# -------- Build stage --------
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace

# Copiamos solo lo necesario para aprovechar la caché de Docker
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./
COPY src src

# Normaliza fin de línea por si el wrapper está en CRLF (Windows) y marca como ejecutable
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

# Construye el JAR ejecutable (omitimos tests para acelerar la imagen)
RUN ./gradlew --no-daemon clean bootJar -x test

# -------- Runtime stage --------
FROM eclipse-temurin:17-jre-jammy AS runtime

ENV APP_HOME=/opt/app
WORKDIR ${APP_HOME}

# Usuario no-root para ejecutar la app
RUN useradd -ms /bin/bash appuser

# Copiamos el artefacto construido
COPY --from=build /workspace/build/libs/app.jar app.jar
RUN chown -R appuser:appuser ${APP_HOME}
USER appuser

# Puerto por defecto local; Render inyecta PORT en runtime
ENV PORT=8080
EXPOSE 8080

# Flags JVM seguros para contenedores
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -Djava.security.egd=file:/dev/./urandom"

# Si Render provee DATABASE_URL (postgres://user:pass@host:port/db?sslmode=require),
# lo convertimos a variables SPRING_DATASOURCE_* esperadas por la app.
# Esto permite vincular fácilmente el servicio de Postgres en Render sin configurar manualmente las 3 vars.
ENTRYPOINT ["/bin/sh","-c","\
if [ -n \"$DATABASE_URL\" ] && [ -z \"$SPRING_DATASOURCE_URL\" ]; then \
  proto_removed=\${DATABASE_URL#*://}; \
  creds=\${proto_removed%@*}; \
  hostpath=\${proto_removed#*@}; \
  user=\${creds%%:*}; \
  pass=\${creds#*:}; \
  export SPRING_DATASOURCE_URL=jdbc:postgresql://\${hostpath}; \
  export SPRING_DATASOURCE_USERNAME=\"\${user}\"; \
  export SPRING_DATASOURCE_PASSWORD=\"\${pass}\"; \
fi; \
exec java $JAVA_OPTS -jar app.jar --server.port=\${PORT:-8080}"]
