plugins {
    kotlin("jvm") version "1.9.22"
    `java-library`
    `maven-publish`
    id("io.papermc.paperweight.userdev") version "1.5.11"
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
    
    maven { url = uri("https://repo.dmulloy2.net/repository/public/") }
}

dependencies {
    compileOnly(kotlin("stdlib"))

    paperweight.paperDevBundle("1.20.4-R0.1-SNAPSHOT")

    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")

    implementation("net.kyori:adventure-text-minimessage:4.15.0")
}

kotlin {
    jvmToolchain(17)
}

group = "net.punchtree"
version = "0.1.0-SNAPSHOT"
description = "PunchTree-Util"
java.sourceCompatibility = JavaVersion.VERSION_17

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks {
    // Configure reobfJar to run when invoking the build task
    assemble {
        dependsOn(reobfJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything

        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release.set(17)
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
    }

    test {
        useJUnitPlatform()
    }
}

val localOutputDir: String? by project
val buildLocal by tasks.registering(Copy::class) {
    group = "build"
    description = "Builds the shaded JAR locally without publishing to the live server."

    from("build/libs/${project.name}-${project.version}.jar")
    into(provider {
        if (localOutputDir != null) {
            localOutputDir?.let { project.file(it) }
        } else {
            logger.warn("Environment variable LOCAL_OUTPUT_DIR is not set. Using the default output directory.")
            project.file("build/libs")
        }
    })
    dependsOn("reobfJar")
    dependsOn("publishToMavenLocal")
}