plugins {
    java
}

group = "com.chirikhin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compile("org.projectlombok", "lombok", "1.18.10")
    annotationProcessor ("org.projectlombok", "lombok", "1.18.10")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.2")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events( "passed", "skipped", "failed")
    }
}