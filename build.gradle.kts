plugins {
    id("java")
}

group = "org.academic"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
//    add jdbc driver
    implementation("mysql:mysql-connector-java:8.0.32")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}