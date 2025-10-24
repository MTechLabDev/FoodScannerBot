plugins {
    java
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "dev.todoforever"
version = "0.0.1-SNAPSHOT"
description = "FoodScannerBot"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
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

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // dotenv
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    // telegram
    implementation("org.telegram:telegrambots-springboot-longpolling-starter:9.2.0")
    implementation("org.telegram:telegrambots-client:9.2.0")

    // barcodes
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.google.zxing:javase:3.5.3")

    // http
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val buildNumber: String = System.getenv("GITHUB_RUN_NUMBER") ?: "local"
version = "1.0.$buildNumber"

tasks.register("printVersion") {
    doLast {
        println(rootProject.name+"-"+version)
    }
}

tasks.register("printTag") {
    doLast {
        println("1.0.$buildNumber")
    }
}