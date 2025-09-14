plugins {
    alias(libs.plugins.jetbrainsKotlinJvm)
    id("java-library")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    id("org.jetbrains.kotlin.kapt")
    kotlin("plugin.serialization") version "2.1.0"
    id("dk.holonet.plugin") version "0.0.1"
}

project.version = "0.1.0"

repositories {
    google()
    mavenCentral()
    mavenLocal()
}

dependencies {
    // Ktor
    compileOnly(ktorLibs.client.core)
    compileOnly(ktorLibs.client.cio)
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.netty)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.server.cors)
    compileOnly(ktorLibs.serialization.kotlinx.json)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

holoNetPlugin {
    pluginId.set("home-one")
    pluginClass.set("dk.holonet.one.home.HomeOnePlugin")
    pluginProvider.set("Holonet")
    pluginsDir.set(File("${rootProject.projectDir}/HomeOnePlugin/build/plugins"))
}

val copyWasmDist = tasks.register<Copy>("copyWasmDist") {
    from(project(":composeApp").tasks.named("wasmJsBrowserDistribution"))
    into(layout.projectDirectory.dir("src/main/resources/HomeOne"))
}

tasks.named("processResources") {
    dependsOn(copyWasmDist)
}

tasks.matching { it.name == "assemblePlugin" }.configureEach {
    dependsOn(tasks.named("processResources"))
}