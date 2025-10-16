import org.gradle.api.tasks.testing.Test

plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	kotlin("plugin.jpa") version "1.9.25"
	id("org.springframework.boot") version "3.2.5"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("kapt") version "1.9.25"
}

group = "cr.ac.una.homestock"
version = "0.0.1-SNAPSHOT"
description = "Backend API para la app HomeStock (inventario y consumo doméstico)"

java {
	toolchain {
		languageVersion.set(org.gradle.jvm.toolchain.JavaLanguageVersion.of(17))
	}
}

repositories { mavenCentral() }

val mapstructVersion = "1.5.5.Final"

dependencies {
	// Spring Boot
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")

	// JSON Kotlin
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

	// MapStruct (DTO <-> Entidad)
	implementation("org.mapstruct:mapstruct:$mapstructVersion")
	kapt("org.mapstruct:mapstruct-processor:$mapstructVersion")

	// OpenAPI/Swagger UI
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")

	// Migraciones + Driver
	implementation("org.flywaydb:flyway-core")
	runtimeOnly("org.postgresql:postgresql")

	// Tests
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

kapt { correctErrorTypes = true }

// Kotlin compile settings
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
	kotlinOptions {
		jvmTarget = "17"
		freeCompilerArgs = listOf("-Xjsr305=strict")
	}
}

// Usa JUnit 5
tasks.withType<Test> { useJUnitPlatform() }

// Empaquetado JAR ejecutable para Render
tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
	archiveFileName.set("app.jar")
}
