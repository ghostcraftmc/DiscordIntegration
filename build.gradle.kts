import kr.entree.spigradle.kotlin.spigot

plugins {
    kotlin("jvm") version "1.8.21"
    kotlin("plugin.serialization") version "1.8.21"
    id("kr.entree.spigradle") version "1.2.4"
}

group = "me.abhigya"
version = "1.0-SNAPSHOT"

val javaVersion = 1.8

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    compileOnly(spigot("1.16.5"))
    compileOnly(fileTree(mapOf("dir" to "${rootProject.rootDir}/lib", "include" to listOf("*.jar"))))

    compileOnly(kotlin("stdlib-jdk8"))
    compileOnly(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.7.1"))
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    compileOnly("com.charleskorn.kaml:kaml:0.54.0")
}

tasks {
    compileKotlin {
        kotlinOptions.suppressWarnings = true
        kotlinOptions.jvmTarget = javaVersion.toString()
        kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all")
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }
}

spigot {
    name = "DiscordIntegration"
    version = "1.0"
    authors = listOf("Abhigya")
    description = "Minecraft to Discord Integration"
    depends = listOf("KotlinLibrary", "DiscordMessenger2")
}