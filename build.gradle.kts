/*
* Copyright (c) 2024-5 Fzzyhmstrs
*
* This is free software provided under the terms of the Timefall Development License - Modified (TDL-M).
* You should have received a copy of the TDL-M with this software.
* If you did not, see <https://github.com/fzzyhmstrs/Timefall-Development-Licence-Modified>.
* */

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("dev.architectury.loom").apply(false)//.version("1.7-SNAPSHOT").apply(false)
    id("architectury-plugin")
    id("com.github.johnrengelman.shadow").apply(false)//.version("7.1.2").apply(false)
    val kotlinVersion: String by System.getProperties()
    kotlin("jvm").version(kotlinVersion)
    `maven-publish`
}

val minecraftVersion: String by project
val modVersion: String by project
val javaVersion = JavaVersion.VERSION_21
val mavenGroup: String by project

base {
    val archivesBaseName: String by project
    archivesName.set(archivesBaseName)
}

val log: File = file("changelog.md")
println("## Changelog for ${base.archivesName.get()} $version \n\n" + log.readText())
println(base.archivesName.get().replace('_','-'))

allprojects {
    apply {
        plugin("java")
    }
    apply {
        plugin("architectury-plugin")
    }

    architectury {
        compileOnly()
    }

    base {
        val archivesBaseName: String by rootProject
        archivesName.set(archivesBaseName)
    }

    version = "$modVersion+$minecraftVersion+${project.name}"
    group = mavenGroup

    repositories {
        exclusiveContent {
            forRepository {
                maven {
                    name = "Modrinth"
                    url = uri("https://api.modrinth.com/maven")

                }
            }
            filter {
                includeGroup("maven.modrinth")
            }
        }
        maven {
            name = "FzzyMaven"
            url = uri("https://maven.fzzyhmstrs.me/")
        }
        mavenLocal()
        mavenCentral()
    }

    tasks {
        withType<JavaCompile> {
            options.encoding = "UTF-8"
            sourceCompatibility = javaVersion.toString()
            targetCompatibility = javaVersion.toString()
            options.release.set(javaVersion.majorVersion.toInt())
        }
        withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            compilerOptions {
                jvmTarget.set(JvmTarget.fromTarget(javaVersion.majorVersion))
                freeCompilerArgs = listOf("-Xjvm-default=all")
            }
        }
        java {
            toolchain { languageVersion.set(JavaLanguageVersion.of(javaVersion.toString())) }
            sourceCompatibility = javaVersion
            targetCompatibility = javaVersion
            withSourcesJar()
        }
    }
}

tasks.register("publishAll") {
    group = "upload"
    dependsOn(project(":fabric").tasks.publish.get())
    dependsOn(project(":neoforge").tasks.publish.get())
}

tasks.register("uploadAllAll") {
    group = "upload"
    dependsOn(project(":fabric").tasks["uploadAll"])
    dependsOn(project(":neoforge").tasks["uploadAll"])
}