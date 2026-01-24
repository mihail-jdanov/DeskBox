import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)

    kotlin("plugin.serialization") version "2.2.20"
}

configurations.all {
    resolutionStrategy.dependencySubstitution.apply {
        substitute(module("net.java.dev.jna:jna-platform"))
            .using(module("net.java.dev.jna:jna-platform-jpms:5.17.0"))
    }
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
            implementation("io.github.g0dkar:qrcode-kotlin:4.5.0")
            implementation("com.github.Dansoftowner:jSystemThemeDetector:3.9.1")
            implementation("net.java.dev.jna:jna-jpms:5.17.0")
            implementation("io.github.vinceglb:auto-launch:0.7.0")
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
            modules("java.net.http")
            targetFormats(TargetFormat.Exe)
            packageName = "DeskBox"
            packageVersion = "1.4.0"
            windows {
                iconFile = project.file("resources/icon.ico")
            }
            macOS {
                iconFile = project.file("resources/icon.icns")
                jvmArgs += "-Dapple.awt.application.appearance=system"
                infoPlist {
                    extraKeysRawXml = """
                        <key>LSUIElement</key>
                        <true/>
                        <key>NSHumanReadableCopyright</key>
                        <string></string>
                        <key>CFBundleURLTypes</key>
                        <array>
                            <dict>
                                <key>CFBundleURLName</key>
                                <string>sing-box</string>
                                <key>CFBundleURLSchemes</key>
                                <array>
                                    <string>sing-box</string>
                                </array>
                            </dict>
                        </array>
                    """.trimIndent()
                }
            }
            linux {
                iconFile = project.file("resources/icon.png")
            }
        }
    }
}