plugins {
    kotlin("jvm") version "1.9.22"
    application
}

group = "com.example"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("com.example.WebServerKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.postgresql:postgresql:42.7.10")
    implementation("com.sparkjava:spark-core:2.9.4")
    implementation("org.slf4j:slf4j-simple:1.7.36")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("com.github.jknack:handlebars:4.3.1")
    
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.1")
    testImplementation("org.junit.platform:junit-platform-launcher:1.10.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.9.22")
    testImplementation("com.h2database:h2:2.4.240")
}

tasks.test {
    useJUnitPlatform()
    setMaxParallelForks(1)
    testLogging {
        events("passed", "skipped", "failed")
        showExceptions = true
        showCauses = true
        showStackTraces = true
        maxGranularity = 0
    }
    afterSuite(KotlinClosure2({ desc: TestDescriptor, result: TestResult ->
        if (desc.parent == null) {
            println("\n========================================")
            println("Test Results: ${result.resultType}")
            println("  Passed: ${result.successfulTestCount}")
            println("  Failed: ${result.failedTestCount}")
            println("  Skipped: ${result.skippedTestCount}")
            println("========================================")
        }
    }))
}

kotlin {
    jvmToolchain(17)
}
