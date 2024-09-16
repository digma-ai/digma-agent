package org.digma;

import org.digma.configuration.Configuration;
import org.digma.instrumentation.digma.agent.BuildVersion;
import org.digma.jdbc.JDBCTrackingTransformer;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;

import static org.digma.configuration.Configuration.JAVA_VERSION;
import static org.digma.configuration.Configuration.OS_NAME;

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

            //this must be the first thing the agent does, other classes rely on non-null
            InstrumentationHolder.instrumentation = inst;

            boolean agentActivated = false;

            if (configuration.isExposePreparedStatementsParametersEnabled()) {
                System.setProperty("otel.instrumentation.jdbc.statement-sanitizer.enabled","false");
                JDBCTrackingTransformer.install(inst);
                agentActivated = true;
            } else {
                Log.debug("otel statement sanitizer is enabled , not installing jdbc tracking transformer.");
            }


            if (configuration.isExtendedObservabilityEnabled()) {
                WithSpanTransformer.install(inst);
                agentActivated = true;
            } else {
                Log.debug("Extended observability is not configured, not installing WithSpan transformer.");
            }


            if (agentActivated) {
                installInstrumentationOnBytebuddyAgent(inst);
            }

        } catch (Throwable ex) {
            // Don't rethrow so users don't get an exception in their application.
            Log.error("got exception while starting Digma agent", ex);
        }
    }




    //see : https://github.com/raphw/byte-buddy/discussions/1658
    private static void installInstrumentationOnBytebuddyAgent(Instrumentation myInstrumentation) {

        if (!OS_NAME.toLowerCase().startsWith("mac")) {
            return;
        }
        if (!JAVA_VERSION.startsWith("17")) {
            return;
        }

        try {
            Log.debug("Installing Instrumentation on ByteBuddy Installer");
            //need to change the Installer fq name otherwise gradle shadow will relocate it
            Class<?> byteBuddyInstaller = Class.forName("net_bytebuddy_agent_Installer".replaceAll("_", "."), false, ClassLoader.getSystemClassLoader());
            Field instrumentationField = byteBuddyInstaller.getDeclaredField("instrumentation");
            boolean isAccessible = instrumentationField.isAccessible();
            instrumentationField.setAccessible(true);

            Instrumentation instrumentation = (Instrumentation) instrumentationField.get(null);
            if (instrumentation == null) {
                instrumentationField.set(null, myInstrumentation);
            }
            instrumentationField.setAccessible(isAccessible);
            Log.debug("Installation of Instrumentation on ByteBuddy Installer succeeded");
        } catch (Exception e) {
            Log.debug("Could not install instrumentation on bytebuddy Installer " + e);
        }
    }






}
