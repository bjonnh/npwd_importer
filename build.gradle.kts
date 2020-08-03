import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val wdtkVersion = "0.11.1"
val rdf4jVersion = "3.3.0"
val log4jVersion = "2.13.3"
val junitApiVersion = "5.6.0"
val univocityParserVersion = "2.8.4"

plugins {
    kotlin("jvm") version "1.4.0-rc"
    application
}
group = "net.nprod"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://dl.bintray.com/kotlin/kotlin-eap")
    }
}
dependencies {
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j18-impl:$log4jVersion")

    implementation("org.wikidata.wdtk:wdtk-dumpfiles:$wdtkVersion")
    implementation("org.wikidata.wdtk:wdtk-wikibaseapi:$wdtkVersion")
    implementation("org.wikidata.wdtk:wdtk-datamodel:$wdtkVersion")
    implementation("org.wikidata.wdtk:wdtk-rdf:$wdtkVersion")

    implementation("org.eclipse.rdf4j:rdf4j-client:$rdf4jVersion")
    implementation("org.eclipse.rdf4j:rdf4j-core:$rdf4jVersion")

    implementation("com.univocity", "univocity-parsers", univocityParserVersion)
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitApiVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitApiVersion")
}
tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}
application {
    mainClassName = "MainKt"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}