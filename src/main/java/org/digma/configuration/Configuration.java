package org.digma.configuration;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("DuplicatedCode")
public class Configuration {

    public static final String OS_NAME = System.getProperty("os.name");
    public static final String JAVA_VERSION = System.getProperty("java.version");


    /**
     * list of package names to instrument seperated by semicolon.
     * the list may include package names or FQN of classes.
     * for example: digma.autoinstrument.packages=my.pkg1;my.pkg2;my.pkg3.MyClass
     */
    public static final String DIGMA_AUTO_INSTRUMENT_PACKAGES_SYSTEM_PROPERTY = "digma.autoinstrument.packages";
    public static final String DIGMA_AUTO_INSTRUMENT_PACKAGES_ENV_VAR = "DIGMA_AUTOINSTRUMENT_PACKAGES";

    /**
     * exclude names should be sent as a list seperated by semicolon.
     * this list operates on matchers for types and matchers for methods.
     * the list may include the following patterns:
     * simple * patterns:
     * a value that starts with * and ends with * means nameContainsIgnoreCase
     * a value that starts with * means nameEndsWithIgnoreCase
     * a value that ends with * means nameStartsWithIgnoreCase
     * for example:
     * *Stub* will exclude any class or method that has the string stub in its name including the package name.
     * it will match: myStubMethod, com.example.stub.MyClass, com.example.MyStubClass.
     * *Stub will exclude all classes and methods that end with the string stub.
     * it will match: myMethodStub, com.example.MyStub
     * Stub* will exclude all class and methods that start with the string stub, this includes the package name,
     * it will match StubMethod. stub.example.MyClass
     * to exclude a class called com.example.StubClass use "*.Stub*", but it will also match com.stub.test.MyClass,
     * so it's  better to exclude with FQN like com.example.StubClass
     * excluding method names has kind of the same rules. to exclude a specific method of a class use the FQN of the class
     * with a #. for example:
     * com.example.MyClass#myMethod
     * to exclude all methods in a class that start with common pattern use: com.example.testpkg.testclasses.MyTestClass#myTest*
     * see unit tests for more examples on using exclude patterns
     * TypeMatchersTests,MethodMatchersTests,ComplexMatchersTests
     */
    public static final String DIGMA_AUTO_INSTRUMENT_PACKAGES_EXCLUDE_NAMES_SYSTEM_PROPERTY = "digma.autoinstrument.packages.exclude.names";
    public static final String DIGMA_AUTO_INSTRUMENT_PACKAGES_EXCLUDE_NAMES_ENV_VAR = "DIGMA_AUTOINSTRUMENT_PACKAGES_EXCLUDE_NAMES";


    /**
     * a list of annotations names.
     * the agent will instrument any method that is annotation with any of these annotations.
     * for example micrometer io.micrometer.observation.annotation.Observed
     */
    public static final String DIGMA_AUTO_INSTRUMENT_METHODS_BY_ANNOTATION_SYSTEM_PROPERTY = "digma.autoinstrument.methods.by.annotation";
    public static final String DIGMA_AUTO_INSTRUMENT_METHODS_BY_ANNOTATION_ENV_VAR = "DIGMA_AUTOINSTRUMENT_METHODS_BY_ANNOTATION";



    /**
     * argument to control injection of otel api to system class loader.
     * if it doesn't exist the agent will check if @WithSpan exists in the system class loader, if not, it will inject otel api to the system class loader.
     * if it exists and is true the agent will inject otel api to system class loader without check first if @WithSpan exists.
     * if it exists and is false the agent will not inject otel api to system class loader.
     */
    public static final String SHOULD_INJECT_OTEL_API_TO_SYSTEM_CLASS_LOADER_SYSTEM_PROPERTY = "digma.agent.injectOtelApiToSystemClassLoader";
    public static final String SHOULD_INJECT_OTEL_API_TO_SYSTEM_CLASS_LOADER_ENV_VAR = "DIGMA_AGENT_INJECT_OTEL_API_TO_SYSTEM_CLASS_LOADER";

    /**
     * turn on this flag to see debug logging from the agent. logging will always be in INFO level because the application may not
     * configure JUL for debug, but we hide debug logging behind this argument
     */
    public static final String DEBUG_SYSTEM_PROPERTY = "digma.agent.debug";
    public static final String DEBUG_ENV_VAR = "DIGMA_AGENT_DEBUG";


    public static final String JDBC_PS_PARAMS_ENABLED_SYSTEM_PROPERTY = "org.digma.otel.instrumentation.jdbc.ps.params.enabled";
    public static final String JDBC_PS_PARAMS_ENABLED_ENABLED_ENV_VAR = "ORG_DIGMA_OTEL_INSTRUMENTATION_JDBC_PS_PARAMS_ENABLED";


    private final List<String> includePackages;
    private final List<String> excludeNames;
    private final List<String> methodsAnnotations;


    private static final Configuration INSTANCE = new Configuration(new ConfigurationReader());
    private final ConfigurationReader configurationReader;


    /**
     * Configuration is not a real singleton. we don't need a real singleton because every instance will return the same results,
     * and even if there are multiple instances they don't consume a lot of resources. with that said we should not create multiple
     * instances in production code and always use the getInstance that will return the same instance.
     * the only reason this is not a real singleton is easier testing.
     * in unit tests we want different properties for each test method or class, with a singleton Configuration it is
     * necessary to mock and initialize the singleton instance for each method,which makes things more verbose.
     * keeping the constructor package protected will help preventing initialization of multiple instances by production
     * code and allows initializing new instances for each test method.
     * ConfigurationReader is a simple class that reads environment variables or system properties. it is possible to provide
     * another class in tests that will read from other resources,usually an in memory map.
     *
     * @param configurationReader a reader for environment variables and system properties
     */
    Configuration(ConfigurationReader configurationReader) {
        this.configurationReader = configurationReader;
        includePackages = loadExtendedObservabilityPackages();
        excludeNames = loadExcludeNames();
        methodsAnnotations = loadMethodsAnnotations();
    }


    //this getInstance doesn't need to be thread safe and a real singleton. nothing will happen if we have more then one instance.
    public static Configuration getInstance() {
        return INSTANCE;
    }


    private boolean getBoolean(String systemPropertyName, String envPropertyName) {
        String value = configurationReader.getEnvOrSystemProperty(systemPropertyName);
        if (value == null) {
            value = configurationReader.getEnvOrSystemProperty(envPropertyName);
        }

        return Boolean.parseBoolean(value);
    }


    public boolean shouldInjectOtelApiToSystemClassLoader() {
        return getBoolean(SHOULD_INJECT_OTEL_API_TO_SYSTEM_CLASS_LOADER_SYSTEM_PROPERTY,
                SHOULD_INJECT_OTEL_API_TO_SYSTEM_CLASS_LOADER_ENV_VAR);
    }

    public boolean shouldInjectOtelApiToSystemClassLoaderExist() {
        return configurationReader.getEnvOrSystemProperty(SHOULD_INJECT_OTEL_API_TO_SYSTEM_CLASS_LOADER_SYSTEM_PROPERTY) != null ||
                configurationReader.getEnvOrSystemProperty(SHOULD_INJECT_OTEL_API_TO_SYSTEM_CLASS_LOADER_ENV_VAR) != null;
    }

    public boolean isDebug() {
        return getBoolean(DEBUG_SYSTEM_PROPERTY, DEBUG_ENV_VAR);
    }

    public boolean isExtendedObservabilityByNamespaceEnabled(){
        return !getIncludePackages().isEmpty();
    }

    public boolean isExtendedObservabilityByAnnotationEnabled(){
        return !getMethodsAnnotations().isEmpty();
    }


    private boolean isExposePreparedStatementsParametersExist() {
        return configurationReader.getEnvOrSystemProperty(JDBC_PS_PARAMS_ENABLED_SYSTEM_PROPERTY) != null ||
                configurationReader.getEnvOrSystemProperty(JDBC_PS_PARAMS_ENABLED_ENABLED_ENV_VAR) != null;
    }

    public boolean isExposePreparedStatementsParametersEnabled() {
        return isExposePreparedStatementsParametersExist() &&
                getBoolean(JDBC_PS_PARAMS_ENABLED_SYSTEM_PROPERTY, JDBC_PS_PARAMS_ENABLED_ENABLED_ENV_VAR);
    }


    @NotNull
    public List<String> getIncludePackages() {
        return includePackages;
    }

    @NotNull
    public List<String> getMethodsAnnotations() {
        return methodsAnnotations;
    }

    @NotNull
    public List<String> getExcludeNames() {
        return excludeNames;
    }


    @NotNull
    private List<String> loadExtendedObservabilityPackages() {

        String packageNames = configurationReader.getEnvOrSystemProperty(DIGMA_AUTO_INSTRUMENT_PACKAGES_SYSTEM_PROPERTY);
        if (packageNames == null) {
            packageNames = configurationReader.getEnvOrSystemProperty(DIGMA_AUTO_INSTRUMENT_PACKAGES_ENV_VAR);
        }
        if (packageNames != null) {
            return Arrays.asList(packageNames.split(";"));
        } else {
            return Collections.emptyList();
        }
    }

    @NotNull
    private List<String> loadMethodsAnnotations() {

        String annotations = configurationReader.getEnvOrSystemProperty(DIGMA_AUTO_INSTRUMENT_METHODS_BY_ANNOTATION_SYSTEM_PROPERTY);
        if (annotations == null) {
            annotations = configurationReader.getEnvOrSystemProperty(DIGMA_AUTO_INSTRUMENT_METHODS_BY_ANNOTATION_ENV_VAR);
        }
        if (annotations != null) {
            return Arrays.asList(annotations.split(";"));
        } else {
            return Collections.emptyList();
        }
    }

    @NotNull
    private List<String> loadExcludeNames() {
        String excludeNames = configurationReader.getEnvOrSystemProperty(DIGMA_AUTO_INSTRUMENT_PACKAGES_EXCLUDE_NAMES_SYSTEM_PROPERTY);
        if (excludeNames == null) {
            excludeNames = configurationReader.getEnvOrSystemProperty(DIGMA_AUTO_INSTRUMENT_PACKAGES_EXCLUDE_NAMES_ENV_VAR);
        }
        if (excludeNames != null) {
            return Arrays.stream(excludeNames.split(";")).filter(s -> !s.isEmpty()).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }


}
