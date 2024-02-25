import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.22"
    id("org.jetbrains.compose") version "1.5.12"
}

group = "ua.kulya.jbcomposeheavyweightoverlay"
version = "1.0"

repositories {
    google()
    mavenCentral()
    maven ("https://jogamp.org/deployment/maven")
    maven("https://jitpack.io")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation("com.github.imptah:worldwindjava:stable-SNAPSHOT")
//    implementation("org.jogamp.gluegen:gluegen-rt-main:2.4.0")
//    implementation("org.jogamp.jogl:jogl-all-main:2.4.0")
//    implementation("org.codehaus.jackson:jackson-core-asl:1.9.13")
    implementation(compose.desktop.currentOs)
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "18"
}

compose.desktop {
    application {
        mainClass = "MainKt"
        jvmArgs += listOf(
            "--add-exports=java.base/java.lang=ALL-UNNAMED",
            "--add-exports=java.desktop/sun.awt=ALL-UNNAMED",
            "--add-exports=java.desktop/sun.java2d=ALL-UNNAMED",
            "-XX:+AllowRedefinitionToAddDeleteMethods"
        )
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "jb-compose-heavyweight-overlay"
            packageVersion = "1.0.0"
        }
    }
}