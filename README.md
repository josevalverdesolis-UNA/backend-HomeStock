# HomeStock Backend

Backend en Kotlin (Spring Boot 3.2.5, Java 17) para gestionar inventario doméstico.

## Stack
- Kotlin 1.9.25
- Spring Boot 3.2.5 (Web, Data JPA, Validation)
- PostgreSQL (producción/desarrollo) / H2 (tests)
- MapStruct (mapeo DTO/Entidad/Dominio) con kapt

## Estructura de paquetes
```
cr.ac.una.homestock
  ├─ BackendHomeStockApplication.kt (clase principal)
  ├─ domain/
  │   ├─ model/ (modelos de dominio puros)
  │   └─ repository/ (interfaces puerto repositorio)
  ├─ data/
  │   ├─ entity/ (@Entity JPA)
  │   └─ repository/ (JpaRepository + adapters a dominio)
  ├─ dto/ (request/response DTOs con Validation)
  ├─ mapper/ (MapStruct mappers componentModel=spring)
  ├─ service/ (casos de uso / orquestación)
  └─ web/ (controladores REST)
```


## Ejecución

## ✅ Requisitos de instalación
🔹 1. Java 17

🔹 2. Gradle

🔹 3. PostgreSQL

🔹 4. Instalar también pgAdmin 4

    Crear la base de datos y usuario desde pgAdmin 4
    usuario:
    Name: homestockapp
    password: 12345

    base:
    Database homestockapp
    Owner homestockapp

🚀 Cómo ejecutar el proyecto

1. Compilar y ejecutar tests

gradlew clean build
2. Levantar la aplicación

gradlew bootRun
La app se iniciará en:
http://localhost:8080

si tienen ese puerto ocupado le dare error
netstat -ano | findstr :8080
el puerto que les de lo ponen y luego lo matan
taskkill /PID **** /F

resultado:
{"status":"ok"}


