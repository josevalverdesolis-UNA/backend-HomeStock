# Despliegue en Render (Docker) para HomeStock Backend

Esta guía te lleva paso a paso para desplegar el backend (Spring Boot + Kotlin, JDK 17) en Render usando Docker.

## Requisitos
- Cuenta en Render (https://render.com)
- Este repositorio en GitHub (o GitLab/Bitbucket) con los archivos: `backend-HomeStock/Dockerfile`, `backend-HomeStock/render.yaml` o un Blueprint en el root.
- Base de datos PostgreSQL en Render (se crea en los pasos de abajo).

## 1) Crear la base de datos PostgreSQL en Render
1. En el dashboard de Render: New → PostgreSQL.
2. Nombre: `homestock-db` (o el que prefieras).
3. Región: Virginia (para baja latencia con el servicio web que también estará en Virginia).
4. Plan: Free (o superior si necesitas más recursos).
5. Crea la base y espera a que el estado sea `Available` y veas las credenciales.

Render te mostrará varias URLs de conexión. Toma nota de:
- Internal Database URL (recomendado cuando el servicio web también está en Render)
- External Database URL (útil para conectarte desde tu laptop u otra red)

Ambas suelen tener el formato:
```
postgres://USERNAME:PASSWORD@HOST:PORT/DBNAME
```

Para Spring Boot necesitas la versión JDBC (cambia el esquema `postgres://` por `jdbc:postgresql://` y agrega `?sslmode=require`):
```
jdbc:postgresql://HOST:PORT/DBNAME?sslmode=require
```

## 2) Variables de entorno necesarias
En el servicio web configura estas variables (Render → Service → Environment):
- `SPRING_DATASOURCE_URL=jdbc:postgresql://<HOST>:5432/<DB>?sslmode=require`
- `SPRING_DATASOURCE_USERNAME=usuario_render`
- `SPRING_DATASOURCE_PASSWORD=contraseña_render`
- Opcional: `SPRING_PROFILES_ACTIVE=prod`
- Opcional: `SPRING_FLYWAY_ENABLED=true` (si quieres ejecutar migraciones)

El archivo `application.properties` ya está preparado para leer estas variables y usar el puerto `PORT` que Render inyecta.

## 3) Despliegue en Render usando Docker
Tienes dos rutas posibles: Blueprint (render.yaml) o vía UI.

### Opción A: Blueprint (render.yaml)
- Si tu `render.yaml` está en el root del repo (monorepo), usa:
  - `dockerfilePath: backend-HomeStock/Dockerfile`
  - `dockerContext: backend-HomeStock`
- Si el `render.yaml` está dentro de `backend-HomeStock/`, puedes dejar `dockerfilePath: ./Dockerfile` y `dockerContext: .`.


El Blueprint ya define `healthCheckPath: /v1/api-docs` (SpringDoc). Si agregas Actuator, puedes cambiarlo a `/actuator/health`.

### Opción B: Configurar vía UI (sin Blueprint)
- New → Web Service → Build & Deploy from a Git repository.
- Root Directory: `backend-HomeStock`
- Runtime: Docker
- Dockerfile Path: `backend-HomeStock/Dockerfile`
- Docker Build Context Directory: `backend-HomeStock`
- Region: `Virginia`
- Plan: `Free` o superior
- Variables de entorno: agrega las de la sección 2

## 4) Probar localmente (Windows CMD)
Compila y ejecuta usando el mismo contexto y Dockerfile que en Render:

1) Construir imagen (desde el root del repo):
```cmd
docker build -f backend-HomeStock/Dockerfile -t homestock-backend:local backend-HomeStock
```

2) Ejecutar el contenedor (usa tu DB de Render o local con SSL si aplica):
```cmd
set SPRING_DATASOURCE_URL=jdbc:postgresql://HOST:5432/DBNAME?sslmode=require
set SPRING_DATASOURCE_USERNAME=USERNAME
set SPRING_DATASOURCE_PASSWORD=PASSWORD

docker run -p 8080:8080 ^
  -e SPRING_DATASOURCE_URL=%SPRING_DATASOURCE_URL% ^
  -e SPRING_DATASOURCE_USERNAME=%SPRING_DATASOURCE_USERNAME% ^
  -e SPRING_DATASOURCE_PASSWORD=%SPRING_DATASOURCE_PASSWORD% ^
  homestock-backend:local
```

3) Abre http://localhost:8080. Con SpringDoc, la UI está en `/swagger-ui` y los docs en `/v1/api-docs`.

## 5) Solución de problemas
- Conexión rechazada/SSL: asegura `?sslmode=require` en la URL JDBC.
- Credenciales: verifica usuario/contraseña exactos de Render.
- Migraciones Flyway: deshabilitadas por defecto; activa con `SPRING_FLYWAY_ENABLED=true` si quieres aplicar scripts en `classpath:bd/migration`.
- Puerto en Render: la imagen respeta `PORT` y por defecto usa `8080` localmente.
- Health check: por defecto `/v1/api-docs`. Si usas Actuator, cambia a `/actuator/health`.

## 6) Referencia rápida de archivos
- `backend-HomeStock/Dockerfile`: multi-stage con Temurin 17, respeta `PORT` y ejecuta `app.jar`.
- `backend-HomeStock/render.yaml`: servicio Docker en Virginia (si usas Blueprint monorepo, puede estar en root con paths al subdirectorio).
- `backend-HomeStock/src/main/resources/application.properties`: lee variables `SPRING_DATASOURCE_*` y `PORT`.
- `.dockerignore` en `backend-HomeStock/` para acelerar builds.
