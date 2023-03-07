plugins {
    id("java")
    id("application")
    id("jacoco")
}


group = "org.academic"
version = "1.0-SNAPSHOT"


application {
    mainClass.set("org.academic.Main")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testImplementation("org.mockito:mockito-inline:5.0.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.0.0")
//    add jdbc driver
    implementation("org.postgresql:postgresql:42.5.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

