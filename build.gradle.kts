plugins {
    kotlin("jvm") version "2.1.10"
    alias(libs.plugins.lombok)
}

group = "com.mucheng.mucute.relay"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.opencollab.dev/maven-releases")
    maven("https://repo.opencollab.dev/maven-snapshots")
}

dependencies {
    implementation(platform(libs.log4j.bom))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    implementation(libs.log4j.api)
    implementation(libs.log4j.core)
    implementation(libs.minecraft.auth)
    implementation(libs.jose4j)
    implementation(libs.jackson.databind)
    implementation(project(":Network:transport-raknet"))
    implementation(project(":Protocol:bedrock-codec"))
    implementation(project(":Protocol:bedrock-connection"))
    implementation(project(":Protocol:common"))
    implementation(libs.bundles.netty)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}