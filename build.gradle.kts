import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    id("org.springframework.boot") version "3.1.5"
    id("io.spring.dependency-management") version "1.1.3"
    id("org.openapi.generator") version "7.1.0"
    id("com.ncorti.ktfmt.gradle") version "0.15.1"
    id("org.jlleitschuh.gradle.ktlint") version "12.0.2"
    kotlin("jvm") version "1.8.22"
    kotlin("plugin.spring") version "1.8.22"
}

group = "ru.viklover"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.flywaydb:flyway-core")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.springframework:spring-jdbc")
    implementation("org.openapitools:openapi-generator-gradle-plugin:7.1.0") {
        exclude(group = "org.slf4j", module = "slf4j-simple")
    }
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.postgresql:r2dbc-postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}

tasks.bootBuildImage {
    builder.set("paketobuildpacks/builder-jammy-base:latest")
}

val projectPackage = "$group.${rootProject.name}"

val generatedSourcesPath = "${layout.buildDirectory.get()}/generated"
val contractsDirectoryPath = "$rootDir/src/main/resources/contracts"

val baseConfigOptions = mapOf(
    "reactive" to "true",
    "useSpringBoot3" to "true",
    "useSwaggerUI" to "false",
    "interfaceOnly" to "true",
    "annotationLibrary" to "none",
    "packageName" to "$projectPackage.contracts",
    "documentationProvider" to "none",
    "skipDefaultInterface" to "true"
)

openApiGenerate {
    generatorName.set("kotlin-spring")
    inputSpec.set("$rootDir/src/main/resources/contracts/base/base-api.yaml")
    outputDir.set(generatedSourcesPath)
    apiPackage.set("$projectPackage.contracts")
    modelPackage.set("$projectPackage.contracts")
    packageName.set("$projectPackage.contracts")
    configOptions.set(baseConfigOptions)
}

tasks.create("openApiGenerateV1", GenerateTask::class) {
    generatorName.set("kotlin-spring")
    inputSpec.set("$rootDir/src/main/resources/contracts/api-v1.0.yaml")
    outputDir.set(generatedSourcesPath)
    apiPackage.set("$projectPackage.contracts.v1.controller")
    modelPackage.set("$projectPackage.contracts.v1.models")
    packageName.set("$projectPackage.contracts.v1")
    configOptions.set(
        baseConfigOptions.toMutableMap() + mapOf("apiSuffix" to "ControllerV1")
    )
}

kotlin.sourceSets["main"].kotlin.srcDir("$generatedSourcesPath/src/main/kotlin")

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
    dependsOn("openApiGenerate", "openApiGenerateV1")
}
