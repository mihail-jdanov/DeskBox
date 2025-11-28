import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import ir.mahozad.manifest.ManifestMode

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)

    id("ir.mahozad.compose-exe-manifest") version "1.0.0"
    kotlin("plugin.serialization") version "2.2.20"
}

kotlin {
    jvm()
    
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation("org.jetbrains.compose.material:material-icons-extended-desktop:1.7.3")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
            implementation("io.github.kdroidfilter:composenativetray:1.0.4")
        }
    }
}

compose.desktop {
    application {
        mainClass = "org.mikhailzhdanov.deskbox.MainKt"

        buildTypes.release.proguard {
            isEnabled = false
        }

        nativeDistributions {
            targetFormats(TargetFormat.Exe)
            packageName = "org.mikhailzhdanov.deskbox"
            packageVersion = "1.0.0"
        }
    }
}

composeExeManifest {
    enabled = true
    manifestMode = ManifestMode.EMBED
    manifestFile = file("app.manifest")
}