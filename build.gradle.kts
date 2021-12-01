import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31"
    id("org.jetbrains.compose") version "1.0.0-beta5"//"1.0.0-beta6-dev450"
}

group = "ua.kulya.jbcomposeheavyweightoverlay"
version = "1.0"

repositories {
    jcenter()
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(fileTree("libs") { include("*.jar") })
    implementation("com.metsci.ext.org.jogamp.jogl:jogl-all-main:2.4.0-rc-20200202")
    implementation("com.metsci.ext.org.jogamp.gluegen:gluegen-rt-main:2.4.0-rc-20200202")
    implementation(compose.desktop.currentOs)
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "jb-compose-heavyweight-overlay"
            packageVersion = "1.0.0"
        }
    }
}