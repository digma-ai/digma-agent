plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version ("8.1.1")
    id("edu.sc.seis.version-class") version "1.3.0"
}

group = "org.digma.instrumentation"
version = "1.0.4"

repositories {
    mavenCentral()
}

shadow {

}


dependencies {

    //don't need to package byte buddy, if opentelemetry agent is in the classpath it inject byte buddy
    // to system class loader.
    //when packging byte buddy with relocation there are exceptions when otel tries to instrument byte buddy classes. why
    // otel tries to instrument relocated byte buddy classes in unknown. if we even need to package and relocate byte buddy
    // we need to investigate how to relocate it, probably it needs some special relocation.
    //but as said currently the agent uses byte buddy classes brought by otel agent.
    compileOnly("net.bytebuddy:byte-buddy:1.14.14")

    //need that for some useful byte buddy matchers
    implementation("io.opentelemetry.javaagent:opentelemetry-javaagent-extension-api:2.2.0-alpha") {
        isTransitive = false
    }

    implementation("org.jetbrains:annotations:21.0.0")


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

        //build with no version in release workflow, so we can download latest without version
        if (project.hasProperty("NoArchiveVersion")) {
            archiveVersion.set("")
        } else {
            archiveVersion.set(version.toString())
        }

        mergeServiceFiles()

        manifest {
            attributes["Main-Class"] = "org.digma.DigmaAgent"
            attributes["Agent-Class"] = "org.digma.DigmaAgent"
            attributes["Premain-Class"] = "org.digma.DigmaAgent"
            attributes["Can-Redefine-Classes"] = "true"
            attributes["Can-Retransform-Classes"] = "true"
            attributes["Implementation-Vendor"] = "Digma"
            attributes["Implementation-Version"] = "${project.version}"
            attributes["Created-By"] = "Gradle ${gradle.gradleVersion}"
            attributes["Build-OS"] = "${System.getProperty("os.name")} ${System.getProperty("os.arch")} ${System.getProperty("os.version")}"
            attributes["Build-Jdk"] = "${System.getProperty("java.version")} ${System.getProperty("java.vendor")} ${System.getProperty("java.vm.version")}"
        }


        //we probably don't need that in runtime, if we do then need to relocate them not to cause
        // confusion in application code
        dependencies {
            exclude(dependency("org.jetbrains:annotations"))
        }

        relocate("io.opentelemetry", "org.digma.io.opentelemetry")
    }


    build {
        finalizedBy("shadowJar")
    }

}