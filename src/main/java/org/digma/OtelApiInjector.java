package org.digma;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.zip.ZipFile;

public class OtelApiInjector {


    public static void injectOtelApiJarToSystemClassLoader(Logger logger, Instrumentation inst) throws IOException {
        List<String> files = getResourceFolderFiles("/otelJars");
        for (String file : files) {
            JarFile jarFile = loadJar(file);
            logger.info("injecting to system class loader from:" + file + " to:"+jarFile.getName());
            inst.appendToSystemClassLoaderSearch(jarFile);
        }
    }


    private static JarFile loadJar(String path) throws IOException {
        InputStream otelApiJar = OtelApiInjector.class.getResourceAsStream(path);
        File out = File.createTempFile("digma",".jar");
        Files.copy( otelApiJar, out.toPath(), StandardCopyOption.REPLACE_EXISTING);
        JarFile jarFile = new JarFile(out,false, ZipFile.OPEN_READ);
        return jarFile;
    }

    private static List<String> getResourceFolderFiles (String folder) {
        //todo: improve to list files in resource folder inside a jar
        List<String> files = new ArrayList<>();
        files.add(folder + "/opentelemetry-api-1.35.0.myjar");
        files.add(folder + "/opentelemetry-context-1.35.0.myjar");
        files.add(folder + "/opentelemetry-instrumentation-annotations-2.1.0.myjar");
        return files;
    }

}
