package org.digma;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Configuration {

    /**
     * list of package names to instrument seperated by semicolon.
     * for example: digma.autoinstrument.packages=my.pkg1;my.pkg2
     */
    private static final String DIGMA_AUTO_INSTRUMENT_PACKAGES_SYSTEM_PROPERTY = "digma.autoinstrument.packages";
    private static final String DIGMA_AUTO_INSTRUMENT_PACKAGES_ENV_VAR = "DIGMA_AUTOINSTRUMENT_PACKAGES";

    /**
     * exclude names should be sent as a list seperated by semicolon.
     * the list may include simple class names to exclude a whole class, or simple class name and method name seperated by dot to exclude specific
     * methods on class, or *String to exclude anything that ends with String.
     * nested and inner classes should be seperated by $.
     * for example: digma.autoinstrument.packages.exclude.names=MyClass;MyOtherClass.myOtherMethod;MyClass$MyNestedClass;*get
     */
    private static final String DIGMA_AUTO_INSTRUMENT_PACKAGES_EXCLUDE_NAMES_SYSTEM_PROPERTY = "digma.autoinstrument.packages.exclude.names";
    private static final String DIGMA_AUTO_INSTRUMENT_PACKAGES_EXCLUDE_NAMES_ENV_VAR = "DIGMA_AUTOINSTRUMENT_PACKAGES_EXCLUDE_NAMES";

    /**
     * argument to control injection of otel api to system class loader.
     * if it doesn't exist the agent will check if @WithSpan exists in the system class loader, if not, it will inject otel api to the system class loader.
     * if it exists and is true the agent will inject otel api to system class loader without check first if @WithSpan exists.
     * if it exists and is false the agent will not inject otel api to system class loader.
     */
    private static final String SHOULD_INJECT_OTEL_API_TO_SYSTEM_CLASS_LOADER_SYSTEM_PROPERTY = "digma.agent.injectOtelApiToSystemClassLoader";
    private static final String SHOULD_INJECT_OTEL_API_TO_SYSTEM_CLASS_LOADER_ENV_VAR = "DIGMA_AGENT_INJECT_OTEL_API_TO_SYSTEM_CLASS_LOADER";

    /**
     * turn on this flag to see debug logging from the agent. logging will always be in INFO level because the application may not
     * configure JUL for debug, but we hide debug logging behind this argument
     */
    private static final String DEBUG_SYSTEM_PROPERTY = "digma.agent.debug";
    private static final String DEBUG_ENV_VAR = "DIGMA_AGENT_DEBUG";


    private final List<String> includePackages;
    private final List<String> excludeClasses;
    private final List<String> excludeMethods;

    private static final Configuration INSTANCE = new Configuration();

    public Configuration() {
        includePackages = getExtendedObservabilityPackages();
        excludeClasses = getExcludeClassNames();
        excludeMethods = getExcludeMethodNames();
    }

    //this getInstance doesn't need to be thread safe and a real singleton. nothing will happen if we have more then one instance.
    public static Configuration getInstance() {
        return INSTANCE;
    }


    private boolean getBoolean(String systemPropertyName, String envPropertyName) {
        String value = getEnvOrSystemProperty(systemPropertyName);
        if (value == null) {
            value = getEnvOrSystemProperty(envPropertyName);
        }

        return Boolean.parseBoolean(value);
    }


    public boolean shouldInjectOtelApiToSystemClassLoader() {
        return getBoolean(SHOULD_INJECT_OTEL_API_TO_SYSTEM_CLASS_LOADER_SYSTEM_PROPERTY,
                SHOULD_INJECT_OTEL_API_TO_SYSTEM_CLASS_LOADER_ENV_VAR);
    }

    public boolean shouldInjectOtelApiToSystemClassLoaderExist() {
        return getEnvOrSystemProperty(SHOULD_INJECT_OTEL_API_TO_SYSTEM_CLASS_LOADER_SYSTEM_PROPERTY) != null ||
                getEnvOrSystemProperty(SHOULD_INJECT_OTEL_API_TO_SYSTEM_CLASS_LOADER_ENV_VAR) != null;
    }

    public boolean isDebug() {
        return getBoolean(DEBUG_SYSTEM_PROPERTY, DEBUG_ENV_VAR);
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
            return Arrays.stream(excludeNames.split(";")).filter(s -> !s.isEmpty()).collect(Collectors.toList());
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
