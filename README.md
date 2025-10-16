# HomeStock Backend

Backend en Kotlin (Spring Boot 3.2.5, Java 17) para gestionar inventario doméstico.

## Stack
- Kotlin 1.9.25
- Spring Boot 3 (Web, Data JPA, Validation)
- PostgreSQL (desarrollo, test y producción)
- MapStruct (mapeo DTO/Entidad/Dominio) con kapt

## Estructura de paquetes
```
cr.ac.una.homestock
  ├─ BackendHomeStockApplication.kt (clase principal)
  ├─ domain/
  │   └─ entity/ (entidades JPA)
  ├─ repository/ (repositorios Spring Data JPA)
  ├─ dto/ (request/response con Bean Validation)
  ├─ mapper/ (MapStruct mappers componentModel=spring)
  ├─ service/ (lógica/orquestación)
  └─ web/ (controladores REST)
```

## Requisitos
- Java 17
- PostgreSQL (local)
- pgAdmin 4 (opcional)
- Cliente psql (opcional, para comandos rápidos)

### Base de datos local (pgAdmin 4 / psql)
Crear base de datos y usuario (con superusuario de Postgres)
La app usa por defecto `jdbc:postgresql://localhost:5432/homestockapp` con `homestockapp / 12345` (ver `src/main/resources/application.properties`).

### Esquema de pruebas (perfil `test`)
Los tests usan el mismo database `homestockapp` pero en un esquema aislado `test` (Flyway lo creará automáticamente). Si necesitas crearlo/manual o ajustar permisos:

```
psql -U postgres -h localhost -d homestockapp -c "CREATE SCHEMA IF NOT EXISTS test AUTHORIZATION homestockapp;"
```

Reiniciar el esquema de pruebas (opcional, para un entorno fresco):
```
psql -U homestockapp -h localhost -d homestockapp -c "DROP SCHEMA IF EXISTS test CASCADE;"
```

## Comandos útiles (Windows - cmd.exe)
- Compilar + ejecutar tests (perfil por defecto):
```
gradlew.bat clean build
```

- Ejecutar todos los tests con el perfil `test` (usa esquema `test`):
```
gradlew.bat test -Dspring.profiles.active=test
```

- Ejecutar un test específico (CRUD integración):
```
gradlew.bat test -Dspring.profiles.active=test --tests "cr.ac.una.homestock.it.IntegrationDbIT.shouldPerformCrudOnProduct"
```

- Ver avisos de deprecación (Gradle):
```
gradlew.bat clean build --warning-mode all
```

- Iniciar aplicación:
```
gradlew.bat bootRun
```

- Iniciar aplicación (sin tests previos):
```
gradlew.bat assemble
gradlew.bat bootRun
```

## Ejecución
- URL base: `http://localhost:8080`
- Si el puerto 8080 está ocupado, libéralo y vuelve a iniciar.

## Perfiles
- `main`/default: usa PostgreSQL (config `application.properties`).
- `test`: usa PostgreSQL en el esquema `test` con Flyway habilitado (migraciones en `classpath:bd/migration`).

## Troubleshooting
- FlywayValidateException / errores de migración en tests:
  - Asegúrate de usar el perfil `test` y que el usuario pueda crear el esquema `test`.
  - Si el esquema quedó en un estado inconsistente, ejecuta: `DROP SCHEMA IF EXISTS test CASCADE;` y vuelve a correr los tests con el perfil `test`.
- Avisos de Gradle “Deprecated Gradle features were used…”: no bloquean el build actual. Para ver el detalle: `--warning-mode all`.
- Mensajes Hikari/JPA al finalizar: logs normales de apagado al terminar tests o la app.

## Despliegue en Render (en proceso)
