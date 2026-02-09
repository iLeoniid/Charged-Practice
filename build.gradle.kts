plugins {
    kotlin("jvm") version "1.9.22"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.charged"
version = "2.0.0-FINAL"

repositories {
    mavenCentral()
}

dependencies {
    // Spigot 1.8.8 descargado manualmente
    compileOnly(files("libs/spigot-1.8.8.jar"))
    compileOnly(files("libs/ProtocolLib-4.7.0.jar"))

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.22")

    // Gson para JSON
    implementation("com.google.code.gson:gson:2.10.1")

    // SQLite
    implementation("org.xerial:sqlite-jdbc:3.44.1.0")

    // Guava (reemplazo de Caffeine)
    implementation("com.google.guava:guava:31.1-jre")

    // Dependencias comunes necesarias para 1.8.8
    compileOnly("commons-io:commons-io:2.11.0")
    compileOnly("org.apache.commons:commons-lang3:3.12.0")
    compileOnly("com.googlecode.json-simple:json-simple:1.1.1")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

kotlin {
    jvmToolchain(8)
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xjvm-default=all")
        }
    }

    compileJava {
        options.encoding = "UTF-8"
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }

    shadowJar {
        archiveBaseName.set("Charged")
        archiveClassifier.set("")
        archiveVersion.set("2.0.0-FINAL")

        // Relocate para evitar conflictos
        relocate("kotlin", "com.charged.libs.kotlin")
        relocate("com.google.gson", "com.charged.libs.gson")
        relocate("org.sqlite", "com.charged.libs.sqlite")
        relocate("com.google.common", "com.charged.libs.guava")

        // Excluir archivos de firma
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")

        minimize {
            // Mantener Kotlin stdlib
            exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib:.*"))
            exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib-jdk8:.*"))
        }
    }

    build {
        dependsOn(shadowJar)
    }

    // Tarea para limpiar mejor
    clean {
        delete("build")
    }
}

// Configuración para usar Java 8
configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.22")
        force("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
        force("org.jetbrains.kotlin:kotlin-stdlib-common:1.9.22")
    }
}