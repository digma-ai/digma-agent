import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("java")
    id("semantic-version")
    id("com.github.johnrengelman.shadow") version ("8.1.1")
    id("edu.sc.seis.version-class") version "1.3.0"
}

group = "org.digma.instrumentation"
version = common.semanticversion.getSemanticVersion(project)

val otelJarsFolder = "otelJars"

repositories {
    mavenCentral()
}


val otelApiJar: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}


dependencies {

    implementation("net.bytebuddy:byte-buddy:1.14.17")

    //need that for some useful byte buddy matchers
    implementation(libs.otelExtensionApi) {
        isTransitive = false
    }

    //datasource-proxy is packaged in digma-agent and in otel extension, fortunately
    // both are loaded by the system class loader so there is no duplicate classed
    //the shadowing must be the same
    implementation("net.ttddyy:datasource-proxy:1.10")

    //we don't need those annotations at runtime, it's only for development in Idea
    compileOnly("org.jetbrains:annotations:21.0.0")


    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.0")
//    testImplementation("org.mockito:mockito-core:5.12.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")


    otelApiJar(libs.otelInstrumentationAnnotations)
}

tasks {

    test {
        useJUnitPlatform()
        //jvmArgs = listOf("-Djdk.instrument.traceUsage")
    }

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
            attributes["Build-Timestamp"] = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
        }


        //must relocate otel classes because they are included in the otel agent
        relocate("io.opentelemetry", "org.digma.io.opentelemetry")

        //we need to package and relocate bytebuddy.
        // if otel debug is on, on startup of an app with our agent and otel agent, otel emits many exceptions about:
        //Cannot resolve type description for org.digma.net.bytebuddy.agent.builder.$Proxy31
        //IMO it can be ignored, its because our agent installs bytebuddy transformer and bytebuddy injects some proxies to the
        //class loader which otel can't handle.
        //I started a discussion in GitHub otel repo and waiting for suggestions if these are real errors or can be ignored.
        //https://github.com/open-telemetry/opentelemetry-java-instrumentation/discussions/11336
        relocate("net.bytebuddy", "org.digma.net.bytebuddy")

        relocate("net.ttddyy.dsproxy", "org.digma.net.ttddyy.dsproxy")

    }


    build {
        finalizedBy("shadowJar")
    }

    val packageOtelJar by registering {

        val output = layout.buildDirectory.dir(otelJarsFolder)
        val outputDirectory = output.get().dir(otelJarsFolder)

        inputs.files(otelApiJar)
        outputs.dir(output)

        doLast {
            outputDirectory.asFile.mkdirs()
            copy {
                from(inputs.files)
                into(outputDirectory)
                //bug in shadowJar that it ignores .jar files in the resource folder, the suggestion in the issue was to rename it
                // to something other than jar. the agent code will rename to .jar when it uses it.
                //see https://github.com/johnrengelman/shadow/issues/276
                rename {
                    it.replace(".jar", ".myjar")
                }
            }
        }

        //generate a file with the list of files to use at runtime.
        //the list of files is used by org.digma.OtelApiInjector.getOtelJarsFiles.
        //we use this way because it is tricky in java to list files in a jar resource folder and may not always work.
        doLast{
            val outputFile = outputDirectory.file("otel-jars-list.txt")
            outputFile.asFile.bufferedWriter().use { writer ->
                for (file in inputs.files) writer.appendLine(file.name.replace(".jar",".myjar"))
            }
        }
    }

    sourceSets.main{
        resources.srcDir(packageOtelJar)
    }

}