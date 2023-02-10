plugins {
    kotlin("jvm") version "1.8.0"
    application
}

group = "by.cp.feedback.mechanism.bot"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("dev.inmo:tgbotapi:5.1.0")
    implementation("io.ktor:ktor-server-netty:2.2.3")
    implementation("org.jetbrains.exposed:exposed-core:0.40.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.40.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.40.1")
    implementation("com.impossibl.pgjdbc-ng:pgjdbc-ng:0.8.9")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}