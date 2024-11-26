package org.digma;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

public class OtelApiInjector {

    private final static String OTEL_JARS_RESOURCE_FOLDER = "/otelJars";

    public static void injectOtelApiJarToSystemClassLoader() throws IOException {
        List<JarFile> jarFiles = createOtelJarFiles();
        for (JarFile jarFile : jarFiles) {
            Log.debug("injecting to system class loader , jar file:" + jarFile.getName());
            InstrumentationHolder.instrumentation.appendToSystemClassLoaderSearch(jarFile);
        }
    }

    private static List<JarFile> createOtelJarFiles() throws IOException {
        List<File> files = createFilesFromPaths();
        List<JarFile> jarFiles = new ArrayList<>();
        for (File file : files) {
            JarFile jarFile = new JarFile(file, false, ZipFile.OPEN_READ);
            jarFiles.add(jarFile);
        }

        return jarFiles;
    }


    private static List<File> createFilesFromPaths() throws IOException {
        List<String> resourcePaths = getResourceFolderFiles();
        List<File> files = new ArrayList<>();
        for (String path : resourcePaths) {
            Log.debug("creating file from resource path:" + path);
            InputStream resourceStream = OtelApiInjector.class.getResourceAsStream(path);
            if (resourceStream == null) {
                throw new FileNotFoundException("could not find resource " + path);
            }
            File out = File.createTempFile("digma", ".jar");
            out.deleteOnExit();
            Files.copy(resourceStream, out.toPath(), StandardCopyOption.REPLACE_EXISTING);
            files.add(out);
        }

        return files;
    }


    private static List<String> getResourceFolderFiles() {
        //todo: improve , find the way to list files in resource folder inside a jar.
        // if we change the versions in gradle this must be changed too
        List<String> files = new ArrayList<>();
        files.add(OTEL_JARS_RESOURCE_FOLDER + "/opentelemetry-api-1.44.1.myjar");
        files.add(OTEL_JARS_RESOURCE_FOLDER + "/opentelemetry-context-1.44.1.myjar");
        files.add(OTEL_JARS_RESOURCE_FOLDER + "/opentelemetry-instrumentation-annotations-2.10.0.myjar");
        return files;
    }


//    public static ClassLoader createOtelApiClassLoader() throws Exception {
//        URL[] urls = createFilesFromPaths().stream().map(file -> {
//            try {
//                return file.toURI().toURL();
//            } catch (MalformedURLException e) {
//                throw new RuntimeException(e);
//            }
//        }).toArray(URL[]::new);
//        return new URLClassLoader(urls,ClassLoader.getSystemClassLoader());
//    }

}
