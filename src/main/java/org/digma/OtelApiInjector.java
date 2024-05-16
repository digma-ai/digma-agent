package org.digma;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

public class OtelApiInjector {

    private final static String OTEL_JARS_RESOURCE_FOLDER = "/otelJars";

    public static void injectOtelApiJarToSystemClassLoader(Instrumentation inst) throws IOException {
        List<String> resourcePaths = getResourceFolderFiles();
        for (String path : resourcePaths) {
            JarFile jarFile = loadJar(path);
            Log.info("injecting to system class loader , resource path:" + path + " jar file:" + jarFile.getName());
            inst.appendToSystemClassLoaderSearch(jarFile);
        }
    }


    private static JarFile loadJar(String path) throws IOException {
        InputStream otelApiJar = OtelApiInjector.class.getResourceAsStream(path);
        if (otelApiJar == null) {
            throw new FileNotFoundException("could not find resource " + path);
        }
        File out = File.createTempFile("digma", ".jar");
        Files.copy(otelApiJar, out.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return new JarFile(out, false, ZipFile.OPEN_READ);
    }

    private static List<String> getResourceFolderFiles() {
        //todo: improve , find the way to list files in resource folder inside a jar.
        // if we change the versions in gradle this must be changed too
        List<String> files = new ArrayList<>();
        files.add(OTEL_JARS_RESOURCE_FOLDER + "/opentelemetry-api-1.35.0.myjar");
        files.add(OTEL_JARS_RESOURCE_FOLDER + "/opentelemetry-context-1.35.0.myjar");
        files.add(OTEL_JARS_RESOURCE_FOLDER + "/opentelemetry-instrumentation-annotations-2.1.0.myjar");
        return files;
    }

}
