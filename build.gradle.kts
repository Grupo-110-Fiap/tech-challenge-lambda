plugins {
    kotlin("jvm") version "1.9.23"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "br.com.fiap.techchallenge.lambda"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.amazonaws:aws-lambda-java-core:1.2.2")
    implementation("com.amazonaws:aws-lambda-java-events:3.11.0")
    implementation("software.amazon.awssdk:cognitoidentityprovider:2.20.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")


    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.14.5")
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("br.com.fiap.techchallenge.LambdaHandlerKt")
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveBaseName.set("lambda-tech-challenge") // Nome do arquivo JAR de saída
    archiveClassifier.set("")
    archiveVersion.set("")
}
