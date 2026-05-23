plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
	jacoco
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.9.1")
    implementation("com.google.guava:guava:31.1-jre")
    //implementation("com.microsoft.onnxruntime:onnxruntime:1.16.3")
    implementation("org.apache.commons:commons-lang3:3.17.0")
    implementation("org.apache.commons:commons-math3:3.6.1")
    //implementation("org.json:json:20231013")
    implementation("black.ninia:jep:4.2.2")
}

application {
    mainClass.set("engine.main.BBMain")
    //mainClass.set("engine.tuning.HyperparameterTuner")
    applicationDefaultJvmArgs = listOf(
        //"-Djava.library.path=/home/arch/trade-engine-coint/.venv/lib/python3.10/site-packages/jep",
        // "-XX:+UseSerialGC",
        // "-XX:+UseZGC",
        "-XX:+AlwaysPreTouch",
        "-XX:ZUncommitDelay=0s",
        "-XX:+ZHeapDumpOnOutOfMemory",
        "-XX:SoftRefLRUPolicyMSPerMB=0",
        // "-Xms7g", "-Xmx7g",
        // "-XX:+UnlockExperimentalVMOptions", "-XX:+UseEpsilonGC"
    )
}

tasks.named<JavaExec>("run") {
    classpath += files(".venv/lib/python3.10/site-packages/jep/jep-4.2.2.jar")
    //classpath += files("libs/json-20231013.jar")
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

tasks.register("runtimeClasspath") {
    doLast {
        println(sourceSets["main"].runtimeClasspath.asPath)
    }
}

tasks.withType<JavaExec> {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

tasks.test {
    useJUnitPlatform()
	finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
}

jacoco {
    toolVersion = "0.8.13"
}
