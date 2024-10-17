/*
* Copyright (c) 2024 Fzzyhmstrs
*
* This is free software provided under the terms of the Timefall Development License - Modified (TDL-M).
* You should have received a copy of the TDL-M with this software.
* If you did not, see <https://github.com/fzzyhmstrs/Timefall-Development-Licence-Modified>.
* */

import org.jetbrains.kotlin.cli.common.toBooleanLenient
import com.matthewprenger.cursegradle.CurseArtifact
import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseRelation
import com.matthewprenger.cursegradle.Options

plugins {
    id("fabric-loom")
    val kotlinVersion: String by System.getProperties()
    kotlin("jvm").version(kotlinVersion)
    id("com.matthewprenger.cursegradle") version "1.4.0"
    id("com.modrinth.minotaur") version "2.+"
}
base {
    val archivesBaseName: String by project
    archivesName.set(archivesBaseName)
}
val log: File = file("changelog.md")
val minecraftVersion: String by project
val modVersion: String by project
version = "$modVersion+$minecraftVersion"
val mavenGroup: String by project
group = mavenGroup
println("## Changelog for ${base.archivesName.get()} $version \n\n" + log.readText())
println(base.archivesName.get().replace('_','-'))
repositories {
    maven {
        name = "Modrinth"
        url = uri("https://api.modrinth.com/maven")
        content {
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
dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    val yarnMappings: String by project
    mappings("net.fabricmc:yarn:$yarnMappings:v2")
    val loaderVersion: String by project
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")
    val fabricVersion: String by project
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
    val fabricKotlinVersion: String by project
    modImplementation("net.fabricmc:fabric-language-kotlin:$fabricKotlinVersion")

    val fzzyConfigVersion: String by project
    modImplementation("me.fzzyhmstrs:fzzy_config:$fzzyConfigVersion") {
        exclude("net.fabricmc.fabric-api")
    }
}

tasks {
    val javaVersion = JavaVersion.VERSION_21
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
        options.release.set(javaVersion.toString().toInt())
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = javaVersion.toString()
            freeCompilerArgs = listOf("-Xjvm-default=all")
        }
    }

    jar {
        from("LICENSE") { rename { "${base.archivesName.get()}_${it}" } }
    }
    jar {
        from( "credits.txt") { rename { "${base.archivesName.get()}_${it}" } }
    }

    processResources {
        val loaderVersion: String by project
        val fabricKotlinVersion: String by project
        inputs.property("version", project.version)
        inputs.property("id", base.archivesName.get())
        inputs.property("loaderVersion", loaderVersion)
        inputs.property("fabricKotlinVersion", fabricKotlinVersion)
        filesMatching("fabric.mod.json") {
            expand(mutableMapOf(
                "version" to project.version,
                "id" to base.archivesName.get(),
                "loaderVersion" to loaderVersion,
                "fabricKotlinVersion" to fabricKotlinVersion)
            )
        }
    }
    java {
        toolchain { languageVersion.set(JavaLanguageVersion.of(javaVersion.toString())) }
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        withSourcesJar()
    }
}
if (System.getenv("MODRINTH_TOKEN") != null) {
    modrinth {
        val modrinthSlugName: String by project
        val mcVersions: String by project
        val modLoaders: String by project
        val releaseType: String by project
        val uploadDebugMode: String by project

        token.set(System.getenv("MODRINTH_TOKEN"))
        projectId.set(modrinthSlugName)
        versionNumber.set("${project.version}")
        versionName.set("${base.archivesName.get()}-${project.version}")
        versionType.set(releaseType)
        uploadFile.set(tasks.remapJar.get())
        gameVersions.addAll(mcVersions.split(","))
        loaders.addAll(modLoaders.split(",").map { it.lowercase() })
        detectLoaders.set(false)
        changelog.set("## Changelog for " + "${project.version} \n\n" + log.readText())
        dependencies {
            val requiredDeps: String by project
            for (dep in requiredDeps.split(",")) {
                required.project(dep)
            }
            val optionalDeps: String by project
            for (dep in optionalDeps.split(",")) {
                optional.project(dep)
            }
        }
        debugMode.set(uploadDebugMode.toBooleanLenient() ?: true)
    }
}

if (System.getenv("CURSEFORGE_TOKEN") != null) {
    curseforge {
        val mcCurseVersions: String by project
        val mcVersions: String by project
        val modLoaders: String by project
        val releaseType: String by project
        val uploadDebugMode: String by project
        val curseProjectId: String by project

        val realVersions = if(mcCurseVersions.isEmpty()) {
            mcVersions.split(",")
        } else {
            mcCurseVersions.split(",")
        }

        apiKey = System.getenv("CURSEFORGE_TOKEN")
        project(closureOf<CurseProject> {
            id = curseProjectId
            changelog = log
            changelogType = "markdown"
            this.releaseType = releaseType
            for (ver in realVersions) {
                addGameVersion(ver)
            }
            for (ml in modLoaders.split(",")) {
                addGameVersion(ml)
            }
            mainArtifact(tasks.remapJar.get().archiveFile.get(), closureOf<CurseArtifact> {
                displayName = "${base.archivesName.get()}-${project.version}"
                relations(closureOf<CurseRelation> {
                    val requiredDeps: String by project
                    for (dep in requiredDeps.split(",")) {
                        this.requiredDependency(dep)
                    }
                    val optionalDeps: String by project
                    for (dep in optionalDeps.split(",")) {
                        this.optionalDependency(dep)
                    }
                })
            })
            relations(closureOf<CurseRelation> {
                val requiredDeps: String by project
                for (dep in requiredDeps.split(",")) {
                    this.requiredDependency(dep)
                }
                val optionalDeps: String by project
                if (optionalDeps.isNotEmpty()) {
                    for (dep in optionalDeps.split(",")) {
                        this.optionalDependency(dep)
                    }
                }
            })
        })
        options(closureOf<Options> {
            javaIntegration = false
            forgeGradleIntegration = false
            javaVersionAutoDetect = false
            debug = uploadDebugMode.toBooleanLenient() ?: true
        })
    }
}

tasks.register("uploadAll") {
    group = "upload"
    dependsOn(tasks.modrinth.get())
    dependsOn(tasks.curseforge.get())
}