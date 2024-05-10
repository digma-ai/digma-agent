package org.digma;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Configuration {

    //list of package names seperated by semicolon
    //digma.autoinstrument.packages=my.pkg1;my.pkg2
    private static final String DIGMA_AUTO_INSTRUMENT_PACKAGES_SYSTEM_PROPERTY = "digma.autoinstrument.packages";
    private static final String DIGMA_AUTO_INSTRUMENT_PACKAGES_ENV_VAR = "DIGMA_AUTOINSTRUMENT_PACKAGES";

    //exclude names should be sent as a list seperated by semicolon.
    //the list may include simple class names to exclude a whole class, or simple class name and method name seperated by dot to exclude specific
    // methods on class, or *String to exclude anything that ends with String.
    //nested and inner classes should be seperated by $.
    //digma.autoinstrument.packages.exclude.names=MyClass;MyOtherClass.myOtherMethod;MyClass$MyNestedClass;*get
    private static final String DIGMA_AUTO_INSTRUMENT_PACKAGES_EXCLUDE_NAMES_SYSTEM_PROPERTY = "digma.autoinstrument.packages.exclude.names";
    private static final String DIGMA_AUTO_INSTRUMENT_PACKAGES_EXCLUDE_NAMES_ENV_VAR = "DIGMA_AUTOINSTRUMENT_PACKAGES_EXCLUDE_NAMES";


    private final List<String> includePackages;
    private final List<String> excludeClasses;
    private final List<String> excludeMethods;


    public Configuration() {
        includePackages = getExtendedObservabilityPackages();
        excludeClasses = getExcludeClassNames();
        excludeMethods = getExcludeMethodNames();
    }

    @NotNull
    public List<String> getIncludePackages() {
        return includePackages;
    }

    @NotNull
    public List<String> getExcludeClasses() {
        return excludeClasses;
    }

    @NotNull
    public List<String> getExcludeMethods() {
        return excludeMethods;
    }

    @NotNull
    private List<String> getExtendedObservabilityPackages() {

        String packageNames = getEnvOrSystemProperty(DIGMA_AUTO_INSTRUMENT_PACKAGES_SYSTEM_PROPERTY);
        if (packageNames == null) {
            packageNames = getEnvOrSystemProperty(DIGMA_AUTO_INSTRUMENT_PACKAGES_ENV_VAR);
        }
        if (packageNames != null) {
            return Arrays.asList(packageNames.split(";"));
        } else {
            return Collections.emptyList();
        }
    }

    @NotNull
    private List<String> getExcludeClassNames() {

        List<String> excludeClasses = new ArrayList<>();
        List<String> excludeNames = getExcludeNames();
        for (String excludeName : excludeNames) {
            //add names that don't have a dot separator, it means a whole class to exclude
            if (!excludeName.contains(".") || excludeName.startsWith("*")) {
                excludeClasses.add(excludeName);
            }
        }

        return excludeClasses;
    }

    @NotNull
    private List<String> getExcludeMethodNames() {

        List<String> excludeMethods = new ArrayList<>();
        List<String> excludeNames = getExcludeNames();
        for (String excludeName : excludeNames) {
            //add names that have a dot separator, its means specific method on a class like SIMPLE_CLASS_NAME.METHOD_NAME
            if (excludeName.contains(".") || excludeName.startsWith("*")) {
                excludeMethods.add(excludeName);
            }
        }

        return excludeMethods;
    }

    @NotNull
    private List<String> getExcludeNames() {
        String excludeNames = getEnvOrSystemProperty(DIGMA_AUTO_INSTRUMENT_PACKAGES_EXCLUDE_NAMES_SYSTEM_PROPERTY);
        if (excludeNames == null) {
            excludeNames = getEnvOrSystemProperty(DIGMA_AUTO_INSTRUMENT_PACKAGES_EXCLUDE_NAMES_ENV_VAR);
        }
        if (excludeNames != null) {
            return Arrays.asList(excludeNames.split(";")).stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }


    @Nullable
    private static String getEnvOrSystemProperty(String key) {
        String envVal = System.getenv(key);
        if (envVal != null) {
            return envVal;
        }
        return System.getProperty(key);
    }


}
