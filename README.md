# HomeStock Backend

Backend en Kotlin (Spring Boot 3.2.5, Java 17) para gestionar inventario doméstico.

## Stack
- Kotlin 1.9.25
- Spring Boot 3 (Web, Data JPA, Validation)
- PostgreSQL (desarrollo/producción) / H2 (tests)
- MapStruct (mapeo DTO/Entidad/Dominio) con kapt

## Estructura de paquetes
```
cr.ac.una.homestock
  ├─ BackendHomeStockApplication.kt (clase principal)
  ├─ domain/ (modelo de dominio y puertos)
  ├─ data/ (entities JPA + adapters repositorio)
  ├─ dto/ (request/response con Bean Validation)
  ├─ mapper/ (MapStruct mappers componentModel=spring)
  ├─ service/ (lógica/orquestación)
  └─ web/ (controladores REST)
```

## Requisitos
- Java 17
- PostgreSQL (local)
-  pgAdmin 4

### Base de datos local (pgAdmin 4)
crear base de datos
- Usuario: `homestockapp` (password: `12345`)
- Base: `homestockapp`
- Owner: `homestockapp`

La app usa por defecto `jdbc:postgresql://localhost:5432/homestockapp` con `homestockapp / 12345` (ver `src/main/resources/application.yml`).

## Ejecución
- Compilar y ejecutar tests: `gradlew clean build`
- Iniciar aplicación: `gradlew bootRun`
- URL base: `http://localhost:8080`
- gradlew.bat test -Dspring.profiles.active=test

Si el puerto 8080 está ocupado, libéralo y vuelve a iniciar.

## Perfiles
- `main`/default: usa PostgreSQL (config `application.yml`).
- `test`

## Despliegue en Render

1) Crear servicio Web en Render (Java) y conectar tu repo.

2) Configurar Build & Start Commands:
- Build Command: `./gradlew clean bootJar -x test`
- Start Command: `java -Dserver.port=$PORT -jar build/libs/app.jar`

3) Health Check
- Path: `/actuator/health`
- Timeout: 180s (sugerido para arranques fríos)


