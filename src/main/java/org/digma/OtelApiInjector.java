package org.digma;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
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
        List<File> files = createOtelJarFilesFromPaths();
        List<JarFile> jarFiles = new ArrayList<>();
        for (File file : files) {
            JarFile jarFile = new JarFile(file, false, ZipFile.OPEN_READ);
            jarFiles.add(jarFile);
        }

        return jarFiles;
    }


    private static List<File> createOtelJarFilesFromPaths() throws IOException {
        List<String> resourcePaths = getOtelJarsFiles();
        List<File> files = new ArrayList<>();
        for (String path : resourcePaths) {
            Log.debug("creating file from resource path " + path);
            InputStream resourceStream = OtelApiInjector.class.getResourceAsStream(path);
            if (resourceStream == null) {
                throw new FileNotFoundException("could not find resource " + path);
            }
            File out = File.createTempFile("digma", ".jar");
            out.deleteOnExit();
            Files.copy(resourceStream, out.toPath(), StandardCopyOption.REPLACE_EXISTING);
            files.add(out);
            Log.debug("file from resource path " + path + " created " + out);
        }

        return files;
    }


    private static List<String> getOtelJarsFiles() throws IOException {

        //the file otel-jars-list.txt is generated at build time and contains the list of file that are
        // packaged as the otel jars.
        // it is done this way because it s tricky to just list files in a resource folder in a jar and may not always work.

        String otelJarsList = OTEL_JARS_RESOURCE_FOLDER + "/otel-jars-list.txt";
        List<String> files = new ArrayList<>();

        try (InputStream resourceStream = OtelApiInjector.class.getResourceAsStream(otelJarsList)) {
            if (resourceStream == null) {
                throw new FileNotFoundException("could not find otel-jars-list.txt");
            }
            Reader reader = new InputStreamReader(resourceStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(reader);
            List<String> lines = bufferedReader.lines().collect(Collectors.toList());
            lines.forEach(line -> {
                files.add(OTEL_JARS_RESOURCE_FOLDER + "/" + line);
            });
        }

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
