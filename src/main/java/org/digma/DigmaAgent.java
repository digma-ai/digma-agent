package org.digma;

import net.bytebuddy.ByteBuddy;
import org.digma.instrumentation.digma.agent.BuildVersion;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;

import static org.digma.Configuration.JAVA_VERSION;
import static org.digma.Configuration.OS_NAME;
import static org.digma.OtelClassNames.WITH_SPAN_CLASS_NAME;

public class DigmaAgent {


    public static void premain(String agentArgs, Instrumentation inst) {
        startAgent(inst, true);
    }


    public static void agentmain(String agentArgs, Instrumentation inst) {
        startAgent(inst, false);
    }


    @SuppressWarnings("unused")
    private static void startAgent(Instrumentation inst, boolean fromPremain) {

        Log.info("starting Digma agent " + BuildVersion.getVersion() + " built on " + BuildVersion.getDate() + ", os: " + OS_NAME + ", java version: " + JAVA_VERSION);


        try {

            Configuration configuration = Configuration.getInstance();


            if (configuration.getIncludePackages().isEmpty()) {
                Log.debug("No configured packages for instrumentation in Digma agent, doing nothing.");
                return;
            }

            installInstrumentationOnBytebuddyAgent(inst);


            //this must be the first thing the agent does, other classes rely on non-null
            InstrumentationHolder.instrumentation = inst;


            //todo: this is temporary until a more clever injection is implemented.
            // when trying to inject otel api lazy just before transforming, it works but otel doesn't
            // instrument the methods. its because otel agent has a class loader optimization, if it tried once to check
            // if class loader has WIthSpan and it was false it will not instrument classes from this class loader anymore.
            // if otel transformer was executed before our transformer encountered a relevant type then it will be too late
            // to inject.
            // one solution may be to add a transformer that does nothing but registers class loaders and checks if they have
            // a relevant package for us and if yes inject otel api if necessary.

            //even if injection fails the agent will still install the transformer,
            // maybe WithSpan is available in higher class loaders, worst thing transformation will fail
            // and user should see the logs.
            makeSureWithSpanClassIsAvailable();


            Log.debug("Digma agent started with configuration: includePackages="
                    + configuration.getIncludePackages()
                    + ",excludeClasses=" + configuration.getExcludeClasses()
                    + ",excludeMethods=" + configuration.getExcludeMethods());


            //if we fail to load bytebuddy nothing will work
            Class<ByteBuddy> byteBuddyClass = ByteBuddy.class;
            Log.debug("byteBuddy Class " + byteBuddyClass.getName() + ", class loader: " + byteBuddyClass.getClassLoader());

            WithSpanTransformer.install(inst);

        } catch (Throwable ex) {
            // Don't rethrow.
            Log.error("got exception while starting Digma agent", ex);
        }
    }


    //see : https://github.com/raphw/byte-buddy/discussions/1658
    private static void installInstrumentationOnBytebuddyAgent(Instrumentation myInstrumentation) {

        if (!OS_NAME.toLowerCase().startsWith("mac")) {
            return;
        }
        if(!JAVA_VERSION.startsWith("17")){
            return;
        }

        try {
            Log.debug("Installing Instrumentation on ByteBuddy Installer");
            //need to change the Installer fq name otherwise gradle shadow will relocate it
            Class<?> byteBuddyInstaller = Class.forName("net_bytebuddy_agent_Installer".replaceAll("_", "."), false, ClassLoader.getSystemClassLoader());
            Field instrumentationField = byteBuddyInstaller.getDeclaredField("instrumentation");
            instrumentationField.setAccessible(true);

            Instrumentation instrumentation = (Instrumentation) instrumentationField.get(null);
            if (instrumentation == null) {
                instrumentationField.set(null, myInstrumentation);
            }
            instrumentationField.setAccessible(false);
            Log.debug("Installation of Instrumentation on ByteBuddy Installer succeeded");
        } catch (Exception e) {
            Log.debug("Could not install instrumentation on bytebuddy Installer " + e);
        }
    }


    private static void makeSureWithSpanClassIsAvailable() {

        //if configuration is true inject and return
        if (Configuration.getInstance().shouldInjectOtelApiToSystemClassLoader()) {
            Log.debug("configuration for injecting otel api to system class loader is true, injecting otel api to system class loader.");
            injectOtelApiToSystemClassLoader();
            return;
        }


        //if configuration exists and is false quit and don't inject
        if (Configuration.getInstance().shouldInjectOtelApiToSystemClassLoaderExist() &&
                !Configuration.getInstance().shouldInjectOtelApiToSystemClassLoader()) {
            Log.debug("configuration for injecting otel api to system class loader is false, not injecting.");
            return;
        }

        //else try to inject if WithSpan is not in system classpath
        try {
            Log.debug("checking if WithSpan class is available in classpath");
            Class.forName(WITH_SPAN_CLASS_NAME);
            Log.debug("WithSpan class is available in classpath");

        } catch (ClassNotFoundException e) {
            Log.debug("WithSpan class is NOT available in classpath, trying to inject otel api");
            injectOtelApiToSystemClassLoader();
        }
    }

    private static void injectOtelApiToSystemClassLoader() {
        if (Configuration.getInstance().shouldInjectOtelApiToSystemClassLoaderExist() &&
                !Configuration.getInstance().shouldInjectOtelApiToSystemClassLoader()) {
            Log.debug("injectOtelApiToSystemClassLoader was called but configuration is false, not injecting");
            return;
        }

        Log.debug("injecting otel api to system class loader.");
        try {
            OtelApiInjector.injectOtelApiJarToSystemClassLoader();
        } catch (UnsupportedOperationException e) {
            Log.error("got exception trying to inject otel api to system class loader. maybe this jvm doesn't support injecting a jar to " +
                    "system class loader. please add otel api top the classpath in a different way", e);
        } catch (Throwable e) {
            Log.error("got exception trying to inject otel api to system class loader. " +
                    "please fix the issue and try again , or add otel api to the classpath in a different way", e);
        }

    }

}
