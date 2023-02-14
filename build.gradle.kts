plugins {
    id("java")
    id("application")
}


group = "org.academic"
version = "1.0-SNAPSHOT"

//runtimeOnly("org.postgresql:postgresql:42.2.24")

application {
    mainClass.set("org.academic.Main")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
//    add jdbc driver
    implementation("org.postgresql:postgresql:42.5.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}


//fun runtimeOnly(s: String) {
//
//}
