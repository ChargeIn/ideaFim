
import dev.feedforward.markdownto.DownParser
import org.intellij.markdown.ast.getTextInNode
import java.net.HttpURLConnection
import java.net.URL

buildscript {
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
        classpath("com.github.AlexPl292:mark-down-to-slack:1.1.2")
        classpath("org.eclipse.jgit:org.eclipse.jgit:6.1.0.202203080745-r")
        classpath("org.kohsuke:github-api:1.305")

        // This comes from the changelog plugin
//        classpath("org.jetbrains:markdown:0.3.1")
    }
}

plugins {
    antlr
    java
    kotlin("jvm") version "1.6.21"

    id("org.jetbrains.intellij") version "1.10.0-SNAPSHOT"
    id("org.jetbrains.changelog") version "1.3.1"

    // ktlint linter - read more: https://github.com/JLLeitschuh/ktlint-gradle
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
}

// Import variables from gradle.properties file
val javaVersion: String by project
val kotlinVersion: String by project
val ideaVersion: String by project
val downloadIdeaSources: String by project
val instrumentPluginCode: String by project
val remoteRobotVersion: String by project
val antlrVersion: String by project

val publishChannels: String by project
val publishToken: String by project

val slackUrl: String by project

repositories {
    mavenCentral()
    maven { url = uri("https://cache-redirector.jetbrains.com/intellij-dependencies") }
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    compileOnly("org.jetbrains:annotations:23.0.0")

    // https://mvnrepository.com/artifact/com.ensarsarajcic.neovim.java/neovim-api
    testImplementation("com.ensarsarajcic.neovim.java:neovim-api:0.2.3")
    testImplementation("com.ensarsarajcic.neovim.java:core-rpc:0.2.3")

    // https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-test
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")

    // https://mvnrepository.com/artifact/org.mockito.kotlin/mockito-kotlin
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")

    testImplementation("com.intellij.remoterobot:remote-robot:$remoteRobotVersion")
    testImplementation("com.intellij.remoterobot:remote-fixtures:$remoteRobotVersion")
    testImplementation("com.automation-remarks:video-recorder-junit:2.0")
    runtimeOnly("org.antlr:antlr4-runtime:$antlrVersion")
    antlr("org.antlr:antlr4:$antlrVersion")

    api(project(":fim-engine"))

    testApi("com.squareup.okhttp3:okhttp:4.10.0")
}

configurations {
    runtimeClasspath {
        exclude(group = "org.antlr", module = "antlr4")
    }
}

// --- Compilation
// This can be moved to other test registration when issue with tests in gradle will be fixed
tasks.register<Test>("testWithNeovim") {
    group = "verification"
    systemProperty("ideafim.nvim.test", "true")
    exclude("/ui/**")
    exclude("**/longrunning/**")
    exclude("**/propertybased/**")
}

tasks.register<Test>("testPropertyBased") {
    group = "verification"
//    include("**/propertybased/**")
}

tasks.register<Test>("testLongRunning") {
    group = "verification"
//    include("**/longrunning/**")
}

tasks {
    // Issue in gradle 7.3
    val test by getting(Test::class) {
        isScanForTestClasses = false
        // Only run tests from classes that end with "Test"
        include("**/*Test.class")
        include("**/*test.class")
        include("**/*Tests.class")
        exclude("**/ParserTest.class")
    }

    val testWithNeovim by getting(Test::class) {
        isScanForTestClasses = false
        // Only run tests from classes that end with "Test"
        include("**/*Test.class")
        include("**/*test.class")
        include("**/*Tests.class")
        exclude("**/ParserTest.class")
        exclude("**/longrunning/**")
        exclude("**/propertybased/**")
    }

    val testPropertyBased by getting(Test::class) {
        isScanForTestClasses = false
        // Only run tests from classes that end with "Test"
        include("**/propertybased/*Test.class")
        include("**/propertybased/*test.class")
        include("**/propertybased/*Tests.class")
    }

    val testLongRunning by getting(Test::class) {
        isScanForTestClasses = false
        // Only run tests from classes that end with "Test"
        include("**/longrunning/**/*Test.class")
        include("**/longrunning/**/*test.class")
        include("**/longrunning/**/*Tests.class")
        exclude("**/longrunning/**/ParserTest.class")
    }

    compileJava {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion

        options.encoding = "UTF-8"
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = javaVersion
            apiVersion = "1.6"
            freeCompilerArgs = listOf("-Xjvm-default=all-compatibility")
//            allWarningsAsErrors = true
        }
    }
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = javaVersion
            apiVersion = "1.6"
//            allWarningsAsErrors = true
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}

gradle.projectsEvaluated {
    tasks.compileJava {
//        options.compilerArgs.add("-Werror")
        options.compilerArgs.add("-Xlint:deprecation")
    }
}

// --- Intellij plugin

intellij {
    version.set(ideaVersion)
    pluginName.set("IdeaFim")

    updateSinceUntilBuild.set(false)

    downloadSources.set(downloadIdeaSources.toBoolean())
    instrumentCode.set(instrumentPluginCode.toBoolean())
    intellijRepository.set("https://www.jetbrains.com/intellij-repository")
    // Yaml is only used for testing. It's part of the IdeaIC distribution, but needs to be included as a reference
    plugins.set(listOf("java", "AceJump:3.8.4", "yaml"))
}

tasks {
    downloadRobotServerPlugin {
        version.set(remoteRobotVersion)
    }

    publishPlugin {
        channels.set(publishChannels.split(","))
        token.set(publishToken)
    }

    runIdeForUiTests {
        systemProperty("robot-server.port", "8082")
        systemProperty("ide.mac.message.dialogs.as.sheets", "false")
        systemProperty("jb.privacy.policy.text", "<!--999.999-->")
        systemProperty("jb.consents.confirmation.enabled", "false")
    }

    runPluginVerifier {
        downloadDir.set("${project.buildDir}/pluginVerifier/ides")
        teamCityOutputFormat.set(true)
//        ideVersions.set(listOf("IC-2021.3.4"))
    }

    generateGrammarSource {
        maxHeapSize = "128m"
        arguments.addAll(listOf("-package", "com.flop.idea.fim.fimscript.parser.generated", "-visitor"))
        outputDirectory = file("src/main/java/com/flop/idea/fim/fimscript/parser/generated")
    }

    named("compileKotlin") {
        dependsOn("generateGrammarSource")
    }

    // Add plugin open API sources to the plugin ZIP
    val createOpenApiSourceJar by registering(Jar::class) {
        // Java sources
        from(sourceSets.main.get().java) {
            include("**/com/flop/idea/fim/**/*.java")
        }
        // Kotlin sources
        from(kotlin.sourceSets.main.get().kotlin) {
            include("**/com/flop/idea/fim/**/*.kt")
        }
        destinationDirectory.set(layout.buildDirectory.dir("libs"))
        archiveClassifier.set("src")
    }

    buildPlugin {
        dependsOn(createOpenApiSourceJar)
        from(createOpenApiSourceJar) { into("lib/src") }
    }

    // Don't forget to update plugin.xml
    patchPluginXml {
        sinceBuild.set("223")
    }
}

// --- Linting

ktlint {
    disabledRules.add("no-wildcard-imports")
    version.set("0.43.0")
}

// --- Tests

tasks {
    test {
        exclude("**/propertybased/**")
        exclude("**/longrunning/**")
        exclude("/ui/**")
    }
}

tasks.register<Test>("testUi") {
    group = "verification"
    include("/ui/**")
}

// --- Changelog

changelog {
    groups.set(listOf("Features:", "Changes:", "Deprecations:", "Fixes:", "Merged PRs:"))
    itemPrefix.set("*")
    path.set("${project.projectDir}/CHANGES.md")
    unreleasedTerm.set("To Be Released")
    headerParserRegex.set("(\\d\\.\\d+(.\\d+)?)".toRegex())
//    header = { "${project.version}" }
//    version = "0.60"
}
