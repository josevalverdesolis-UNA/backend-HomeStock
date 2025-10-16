# Despliegue en Render (Docker) para HomeStock Backend

Esta guía te lleva paso a paso para desplegar el backend (Spring Boot + Kotlin, JDK 17) en Render usando Docker.

## Requisitos
- Cuenta en Render (https://render.com)
- Este repositorio en GitHub con `Dockerfile` y `render.yaml` en la RAÍZ del repo.
- Base de datos PostgreSQL en Render (se crea en los pasos de abajo).

## 1) Crear la base de datos PostgreSQL en Render
1. En el dashboard de Render: New → PostgreSQL.
2. Nombre: `homestock-db` (o el que prefieras).
3. Región: Virginia (para baja latencia con el servicio web).
4. Plan: Free (o superior si necesitas más recursos).
5. Espera a que el estado sea `Available` y copia las credenciales.

Render te mostrará varias URLs de conexión. Toma nota de la URL JDBC (reemplaza `postgres://` por `jdbc:postgresql://` y agrega `?sslmode=require`):
```
jdbc:postgresql://HOST:PORT/DBNAME?sslmode=require
```

## 2) Variables de entorno necesarias
Configura en tu servicio web (Render → Service → Environment):
- `SPRING_DATASOURCE_URL=jdbc:postgresql://<HOST>:5432/<DB>?sslmode=require`
- `SPRING_DATASOURCE_USERNAME=usuario_render`
- `SPRING_DATASOURCE_PASSWORD=contraseña_render`
- Opcional: `SPRING_PROFILES_ACTIVE=prod`
- Opcional: `SPRING_FLYWAY_ENABLED=true` (si quieres ejecutar migraciones)

El `application.properties` ya lee estas variables y usa `server.port=${PORT:8080}`.

## 3) Despliegue en Render usando Docker
Puedes usar Blueprint (render.yaml) o configurarlo vía UI.

### Opción A: Blueprint (render.yaml en la RAÍZ del repo)
- `dockerfilePath: ./Dockerfile`
- `dockerContext: .`
- `healthCheckPath: /v1/api-docs`

### Opción B: Configurar vía UI (sin Blueprint)
- New → Web Service → Build & Deploy from a Git repository.
- Root Directory: DEJAR VACÍO (proyecto en la raíz)
- Runtime: Docker
- Dockerfile Path: `./Dockerfile`
- Docker Build Context Directory: `.`
- Region: `Virginia`
- Plan: `Free` o superior
- Variables de entorno: agrega las de la sección 2

Nota: Solo si mueves el proyecto a un subdirectorio (monorepo), entonces usarías:
- Root Directory: `backend-HomeStock`
- Dockerfile Path: `backend-HomeStock/Dockerfile`
- Docker Build Context Directory: `backend-HomeStock`

## 4) Probar localmente (Windows CMD)
Compila y ejecuta desde la raíz del repo:

1) Construir imagen:
```cmd
docker build -f ./Dockerfile -t homestock-backend:local .
```

2) Ejecutar contenedor:
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

3) Abre http://localhost:8080 → Docs: `/v1/api-docs`, UI: `/swagger-ui`.

## 5) Solución de problemas
- Error “Falta el directorio raíz del servicio /opt/render/project/src/backend-HomeStock”: ajusta el servicio para Root Directory VACÍO y usa `./Dockerfile` y `.` como contexto.
- SSL: asegura `?sslmode=require` en la URL JDBC.
- Migraciones Flyway: deshabilitadas por defecto; activa con `SPRING_FLYWAY_ENABLED=true` si quieres aplicar scripts en `classpath:bd/migration`.
- Puerto: Render inyecta `PORT`. La imagen lo respeta y por defecto usa `8080` localmente.
- Health check: `/v1/api-docs` (o `/actuator/health` si usas Actuator).

## 6) Referencia rápida de archivos
- `Dockerfile` (raíz): multi-stage Temurin 17, ejecuta `app.jar` y respeta `PORT`.
- `render.yaml` (raíz): servicio Docker en Virginia con paths relativos.
- `src/main/resources/application.properties`: usa `SPRING_DATASOURCE_*` y `PORT`.
- `.dockerignore` (raíz): acelera builds evitando incluir caches/artefactos.
