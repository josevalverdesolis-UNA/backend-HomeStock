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
- (Opcional) pgAdmin 4

### Base de datos local (pgAdmin 4)
- Usuario: `homestockapp` (password: `12345`)
- Base: `homestockapp`
- Owner: `homestockapp`

La app usa por defecto `jdbc:postgresql://localhost:5432/homestockapp` con `homestockapp / 12345` (ver `src/main/resources/application.yml`).

## Ejecución
- Compilar y ejecutar tests: `gradlew clean build`
- Iniciar aplicación: `gradlew bootRun`
- URL base: `http://localhost:8080`

Si el puerto 8080 está ocupado, libéralo y vuelve a iniciar.

## Perfiles
- `main`/default: usa PostgreSQL (config `application.yml`).
- `test`: usa H2 en memoria (`src/test/resources/application-test.yml`).

## Endpoints
- `GET /` → "Servidor activo"
- `GET /api/products` → lista productos
- `GET /api/products/{id}` → obtiene producto por id
- `POST /api/products` → crea producto (201 Created, Location)
- `PUT /api/products/{id}` → actualiza producto
- `DELETE /api/products/{id}` → elimina producto

Body ProductDto (JSON):
```
{
  "id": null,
  "name": "Arroz",
  "quantity": 2
}
```
Validaciones: `name` no vacío, `quantity` >= 0.

## Pruebas
- Unitarias y de integración con Spring Test + MockMvc.
- Perfil `test` con H2.

Ubicación de pruebas clave:
- `web/HomeControllerTest.kt` (raíz "/")
- `web/ProductControllerIT.kt` (CRUD end-to-end)
- `mapper/ProductMapperTest.kt` (MapStruct)
- `service/ProductServiceTest.kt` (servicio)

## Despliegue en Render

1) Crear servicio Web en Render (Java) y conectar tu repo.

2) Configurar Build & Start Commands:
- Build Command: `./gradlew clean bootJar -x test`
- Start Command: `java -Dserver.port=$PORT -jar build/libs/app.jar`

3) Health Check
- Path: `/actuator/health`
- Timeout: 180s (sugerido para arranques fríos)

4) Variables de entorno (Environment)
- `SPRING_DATASOURCE_URL` → JDBC URL de tu Postgres en Render, ejemplo:
  - `jdbc:postgresql://<host>:<port>/<db>?sslmode=require`
- `SPRING_DATASOURCE_USERNAME` → usuario de la DB
- `SPRING_DATASOURCE_PASSWORD` → contraseña de la DB
- `CORS_ALLOWED_ORIGINS` → orígenes permitidos separados por coma (ej: `https://tu-frontend.onrender.com, http://localhost:3000`)
- `DB_POOL_SIZE` → tamaño del pool Hikari (ej: `10`)
- (Opcional) `SPRING_JPA_SHOW_SQL=false` para reducir logs SQL en producción

Notas:
- El puerto lo inyecta Render en `$PORT` y ya está soportado en `application.yml`.
- Para logs más limpios en prod, también puedes bajar niveles con:
  - `logging.level.org.hibernate.SQL=info`
  - `logging.level.org.hibernate.type.descriptor.sql.BasicBinder=warn`
- Si tu DB es el servicio “PostgreSQL” de Render, copia la cadena JDBC desde sus detalles; si solo ves `postgres://`, conviértela a `jdbc:postgresql://` y añade `?sslmode=require`.

## Notas
- MapStruct se procesa con `kapt`. Si hay problemas de compilación, limpia el proyecto (`gradlew clean`).
- Para producción, considera usar migraciones (Flyway/Liquibase) en lugar de `ddl-auto=update`.
