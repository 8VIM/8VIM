@file:Suppress("DSL_SCOPE_VIOLATION")

import java.io.FileInputStream
import java.util.Properties
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    alias(libs.plugins.agp.application)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.mannodermaus.android.junit5)
    alias(libs.plugins.mikepenz.aboutlibraries)
    checkstyle
    jacoco
}

apply(plugin = "checkstyle")

tasks.register<Checkstyle>("checkstyle") {
    source = fileTree("src")
    configFile = project.rootProject.file("config/checkstyle/checkstyle.xml")
    includes += "**/*.java"
    isShowViolations = true
    excludes += setOf("**/gen/**", "**/R.java")
    classpath = files()
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.check {
    dependsOn("checkstyle")
}

android {
    val versionPropsFile = file("version.properties")
    val versionProps = Properties()
    versionProps.load(FileInputStream(versionPropsFile))

    val versionMajor = (versionProps["MAJOR"] as String).toInt()
    val versionMinor = (versionProps["MINOR"] as String).toInt()
    val versionPatch = (versionProps["PATCH"] as String).toInt()
    val versionRc = (versionProps["RC"] as String).toInt()

    namespace = "inc.flide.vim8"
    compileSdk = 34

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf(
            "-Xallow-result-return-type",
            "-opt-in=kotlin.contracts.ExperimentalContracts",
            "-Xjvm-default=all-compatibility"
        )
    }

    defaultConfig {
        applicationId = "inc.flide.vi8"
        minSdk = 24
        targetSdk = 34

        val rcValue = if (versionRc > 0) 100 - versionRc else 0
        versionCode =
            versionMajor * 1000000 + 10000 * versionMinor + 100 * versionPatch - rcValue
        versionName = "$versionMajor.$versionMinor.$versionPatch"

        resValue("string", "app_name", "8Vim")
        resValue("string", "version_name", versionName.toString())

        if (versionRc > 0) {
            versionNameSuffix = "-rc.$versionRc"
            resValue("string", "version_name", versionName + versionNameSuffix)
        }

        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    bundle.language.enableSplit = false

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }

    if (System.getenv("VIM8_BUILD_KEYSTORE_FILE") != null) {
        signingConfigs {
            create("release") {
                storeFile = file(System.getenv("VIM8_BUILD_KEYSTORE_FILE"))
                storePassword = System.getenv("VIM8_BUILD_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("VIM8_BUILD_KEY_ALIAS")
                keyPassword = System.getenv("VIM8_BUILD_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        named("debug") {
            isDebuggable = true
            applicationIdSuffix = ".debug"

            resValue("string", "app_name", "8Vim Debug")
            enableUnitTestCoverage
            /* Activate R8 in debug mode, good to check if any new library added works
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            */
        }

        named("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            if (System.getenv("VIM8_BUILD_KEYSTORE_FILE") != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }

        create("rc") {
            initWith(getByName("release"))
            applicationIdSuffix = ".rc"
            resValue("string", "app_name", "8Vim RC")
        }
    }

    lint {
        abortOnError = true
        disable += listOf(
            "ObsoleteLintCustomCheck",
            "ClickableViewAccessibility",
            "VectorPath",
            "UnusedResources",
            "GradleDependency",
            "OldTargetApi"
        )
        htmlReport = true
        warningsAsErrors = true
    }

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
    sourceSets {
        findByName("main")?.java?.srcDirs(project.file("src/main/kotlin"))
        findByName("test")?.java?.srcDirs(project.file("src/test/kotlin"))
    }
}
tasks.withType<JacocoReport> {
    dependsOn(tasks.withType<Test>())
    executionData.setFrom(project.fileTree(buildDir) { include("**/jacoco/*.exec") })
    reports {
        csv.required.set(false)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()

    finalizedBy(tasks.withType<JacocoReport>())

    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal*")
    }

    testLogging {
        events(
            TestLogEvent.FAILED,
            TestLogEvent.PASSED,
            TestLogEvent.STANDARD_ERROR,
            TestLogEvent.SKIPPED
        )

        exceptionFormat = TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}

dependencies {
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.android.material)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.recyclerview)
    implementation(libs.apache.commons.text)
    implementation(libs.arrow.core)
    implementation(libs.arrow.optics)
    implementation(libs.colorpreference)
    implementation(libs.jackson.dataformat.yaml)
    implementation(libs.jackson.module.arrow.kotlin)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.json.schema.validator)
    implementation(libs.keyboardview)
    implementation(libs.kotlin.reflect)
    implementation(libs.logback.android)
    implementation(libs.mikepenz.aboutlibraries.core)
    implementation(libs.mikepenz.aboutlibraries.compose)
    implementation(libs.slf4j.api)

    ksp(libs.arrow.ksp)

    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.junit4)

    testImplementation(libs.logback.classic)
    testImplementation(libs.junit5.api)
    testRuntimeOnly(libs.junit5.jupiter.engine)
    testRuntimeOnly(libs.junit5.vintage.engine)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.kotest.extensions.arrow)
    testImplementation(libs.kotest.framework.datatest)
    testImplementation(libs.kotest.junit5)
    testImplementation(libs.kotest.property)
    testImplementation(libs.kotest.property.arrow)
    testImplementation(libs.kotest.property.arrow.optics)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit5)
    testImplementation(libs.mockk.core)
    testImplementation(libs.mockk.android)
    testImplementation(libs.mockk.agent)
}

configurations.testImplementation {
    exclude(module = "logback-android")
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    debug.set(true)
    verbose.set(true)
    android.set(true)
    outputToConsole.set(true)
    outputColorName.set("RED")
    reporters {
        reporter(ReporterType.HTML)
        reporter(ReporterType.CHECKSTYLE)
    }
}
