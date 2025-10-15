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
		languageVersion = JavaLanguageVersion.of(17)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

val mapstructVersion = "1.5.5.Final"

dependencies {
	// Spring Boot starters requeridos
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-actuator")

	// Jackson Kotlin
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	// MapStruct (implementación + processor vía kapt)
	implementation("org.mapstruct:mapstruct:$mapstructVersion")
	kapt("org.mapstruct:mapstruct-processor:$mapstructVersion")

	// Base de datos PostgreSQL (runtime en entorno no-test)
	runtimeOnly("org.postgresql:postgresql")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

// Deshabilitar completamente las tareas de test
tasks.withType<org.gradle.api.tasks.testing.Test>().configureEach {
	enabled = false
}

// Evitar compilar fuentes y procesar recursos del sourceSet de test
sourceSets {
	val test by getting {
		java.setSrcDirs(emptyList<String>())
		resources.setSrcDirs(emptyList<String>())
	}
}

tasks.matching { it.name in setOf("compileTestKotlin", "compileTestJava", "processTestResources", "testClasses") }
	.configureEach { it.enabled = false }


tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    archiveFileName.set("app.jar")
}
