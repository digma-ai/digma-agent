package org.digma;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.MemberAttributeExtension;
import net.bytebuddy.description.annotation.AnnotationDescription;
import org.digma.instrumentation.digma.agent.BuildVersion;

import java.lang.annotation.Annotation;
import java.lang.instrument.Instrumentation;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.digma.OtelClassNames.WITH_SPAN_CLASS_NAME;

public class DigmaAgent {

    private static final Logger LOGGER = Logger.getLogger(DigmaAgent.class.getName());


    public static void premain(String agentArgs, Instrumentation inst) {
        startAgent(inst, true);
    }


    public static void agentmain(String agentArgs, Instrumentation inst) {
        startAgent(inst, false);
    }


    @SuppressWarnings("unused")
    private static void startAgent(Instrumentation inst, boolean fromPremain) {

        //todo: add Digma prefix to all log messages
        LOGGER.info("starting Digma agent " + BuildVersion.getVersion() + " built on " + BuildVersion.getDate());


        try {

            Configuration configuration = new Configuration();

            //maybeInjectOtelApiToSystemClassLoader returns false it means something went wrong with injection, and
            // we can't use the agent. a message will be logged and user should act according to instructions in the log
            if (!maybeInjectOtelApiToSystemClassLoader(configuration, inst)) {
                return;
            }


            if (configuration.getIncludePackages().isEmpty()) {
                LOGGER.info("No configured packages for instrumentation in Digma agent, doing nothing.");
                return;
            }


            LOGGER.info("Digma agent started with configuration: includePackages="
                    + configuration.getIncludePackages()
                    + ",excludeClasses=" + configuration.getExcludeClasses()
                    + ",excludeMethods=" + configuration.getExcludeMethods());


            //if we fail to load bytebuddy nothing will work
            Class<ByteBuddy> byteBuddyClass = ByteBuddy.class;
            LOGGER.info("byteBuddy Class " + byteBuddyClass.getName() + ", class loader: " + byteBuddyClass.getClassLoader());

            new AgentBuilder.Default()
                    .type(TypeMatchers.create(configuration))
                    .transform((builder, typeDescription, classLoader, module, protectionDomain) -> {

                        try {
                            //the WithSpan annotation should be loadable from the same class loader of the application class
                            @SuppressWarnings("unchecked")
                            AnnotationDescription annotationDescription =
                                    AnnotationDescription.Latent.Builder.ofType((Class<? extends Annotation>) Class.forName(WITH_SPAN_CLASS_NAME, false, classLoader)).build();

                            LOGGER.info("transforming " + typeDescription.getCanonicalName() + " in class loader " + classLoader);

                            return builder
                                    .visit(new MemberAttributeExtension.ForMethod().annotateMethod(annotationDescription)
                                            .on(MethodMatchers.create(typeDescription, configuration)));
                        } catch (Throwable e) {
                            LOGGER.log(Level.SEVERE, "got exception in bytebuddy transformer", e);
                            return builder;
                        }

                    }).installOn(inst);


        } catch (Throwable ex) {
            // Don't rethrow.
            LOGGER.log(Level.SEVERE, "got exception while starting Digma agent", ex);
        }
    }


    private static boolean maybeInjectOtelApiToSystemClassLoader(Configuration configuration, Instrumentation inst) {
        if (configuration.shouldInjectOtelApiToSystemClassLoader()) {
            LOGGER.info("injecting otel api to system class loader.");
            try {
                OtelApiInjector.injectOtelApiJarToSystemClassLoader(LOGGER,inst);
            } catch (UnsupportedOperationException e) {
                LOGGER.log(Level.SEVERE, "got exception trying to inject otel api to system class loader. maybe this jvm doesn't support injecting a jar to " +
                        "system class loader. please add otel api top the classpath in a different way", e);
                return false;
            } catch (Throwable e) {
                LOGGER.log(Level.SEVERE, "got exception trying to inject otel api to system class loader. " +
                        "please fix the issue and try again , or add otel api to the classpath in a different way", e);
                return false;
            }
        }

        return true;
    }

}
