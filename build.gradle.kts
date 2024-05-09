plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version ("8.1.1")
}

group = "org.digma.otel.wrapper"
version = "1.0.0"

repositories {
    mavenCentral()
}

shadow {

}


dependencies {

    compileOnly("net.bytebuddy:byte-buddy:1.14.14")
//    compileOnly("net.bytebuddy:byte-buddy-agent:1.14.14")

    implementation("io.opentelemetry.javaagent:opentelemetry-javaagent-extension-api:2.2.0-alpha"){
        isTransitive = false
    }


    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks {

    jar {
        enabled = false
    }


    withType<JavaCompile>() {
        options.compilerArgs.addAll(listOf("-Xlint:unchecked,deprecation"))
        options.release.set(JavaLanguageVersion.of(8).asInt())
    }


    shadowJar {
        archiveBaseName.set("digma-agent")
        archiveClassifier.set("")
        archiveVersion.set(version.toString())

        mergeServiceFiles()

        manifest {
            attributes.put("Main-Class", "org.digma.DigmaAgent")
            attributes.put("Agent-Class", "org.digma.DigmaAgent")
            attributes.put("Premain-Class", "org.digma.DigmaAgent")
            attributes.put("Can-Redefine-Classes", "true")
            attributes.put("Can-Retransform-Classes", "true")
            attributes.put("Implementation-Vendor", "Digma")
            attributes.put("Implementation-Version", "${project.version}")
        }


        relocate("io.opentelemetry", "org.digma.io.opentelemetry")
//        relocate("net.bytebuddy", "org.digma.agent.net.bytebuddy")
    }


    build {
        finalizedBy("shadowJar")
    }

}