package org.digma;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.lang.annotation.Annotation;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DigmaAgent {

    private static final Logger LOGGER = Logger.getLogger(DigmaAgent.class.getName());


    public static void premain(String agentArgs, Instrumentation inst) {
        startAgent(inst, true);
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        startAgent(inst, false);
    }


    private static void startAgent(Instrumentation inst, boolean fromPremain) {

        LOGGER.info("starting Digma agent");

        try {

            Configuration configuration = new Configuration();

            List<String> packages = configuration.getExtendedObservabilityPackages();

            Class<ByteBuddy> byteBuddyClass = ByteBuddy.class;
            LOGGER.info("byteBuddy Class loader: " + byteBuddyClass.getClassLoader());

            new AgentBuilder.Default()
//                    .type(ElementMatchers.named("com.digma.otel.javaagent.extension.instrumentation.methods.test.TestClass"))
                    .type(TypeMatchers.create(packages))
                    .transform(new AgentBuilder.Transformer() {
                        @Override
                        public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain) {

                            try {
                                String withSpan = "io_opentelemetry_instrumentation_annotations_WithSpan".replaceAll("_", ".");
                                AnnotationDescription annotationDescription = AnnotationDescription.Latent.Builder.ofType((Class<? extends Annotation>) Class.forName(withSpan, false, classLoader)).build();

                                LOGGER.info("transforming "+typeDescription.getCanonicalName());
                                System.out.println("transforming "+typeDescription.getCanonicalName());

                                return builder
//                                        .method(ElementMatchers.isDeclaredBy(typeDescription))
                                        .method(MethodMatchers.create(typeDescription))
                                        .intercept(SuperMethodCall.INSTANCE)
                                        .annotateMethod(annotationDescription);
                            } catch (Throwable e) {
                                LOGGER.log(Level.SEVERE, "got exception in bytebuddy transformer", e);
                                return builder;
                            }

                        }
                    }).installOn(inst);


        } catch (Throwable ex) {
            // Don't rethrow.
            LOGGER.log(Level.SEVERE, "got exception while starting Digma agent", ex);
        }
    }




    public static void main(String[] args) {

    }

}
