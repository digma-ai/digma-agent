package org.digma;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.MemberAttributeExtension;
import net.bytebuddy.description.annotation.AnnotationDescription;
import org.digma.configuration.Configuration;
import org.digma.instrumentation.ExtendedObservability;

import java.lang.annotation.Annotation;
import java.lang.instrument.Instrumentation;

import static org.digma.OtelClassNames.WITH_SPAN_CLASS_NAME;

public class WithSpanTransformer {


    public static void install(Instrumentation inst) {

        Log.debug("installing withSpanTransformer");

        new AgentBuilder.Default()
                .type(TypeMatchers.create(Configuration.getInstance()))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) -> {

                    try {
                        //the WithSpan annotation should be loadable from the same class loader of the application class
                        AnnotationDescription withSpanAnnotationDescription = getWithSpanAnnotationDescription(classLoader);

                        AnnotationDescription digmaMarkerAnnotationDescription = getDigmaMarkerAnnotationDescription();

                        Log.debug("transforming " + typeDescription.getCanonicalName() + " in class loader " + classLoader);

                        return builder
                                .visit(new MemberAttributeExtension.ForMethod()
                                        .annotateMethod(withSpanAnnotationDescription)
                                        .annotateMethod(digmaMarkerAnnotationDescription)
                                        .on(MethodMatchers.create(typeDescription, Configuration.getInstance())));
                    } catch (Throwable e) {
                        Log.error("got exception in bytebuddy transformer", e);
                        return builder;
                    }

                }).installOn(inst);
    }


    @SuppressWarnings("unchecked")
    private static AnnotationDescription getWithSpanAnnotationDescription(ClassLoader classLoader) throws Exception {

//        if (classLoader.getResource(WITH_SPAN_CLASS_NAME.replace('.', '/') + ".class") == null) {
//            Log.debug("class loader "+classLoader+" doesn't have WithSpan class resource, trying to inject to system class loader");
//            OtelApiInjector.injectOtelApiJarToSystemClassLoader();
//        }else{
//            Log.debug("class loader "+classLoader+" has WithSpan class resource");
//        }

        Log.debug("trying to load WithSpan class with class loader " + classLoader);
        AnnotationDescription annotationDescription = AnnotationDescription.Latent.Builder.ofType((Class<? extends Annotation>) Class.forName(WITH_SPAN_CLASS_NAME, true, classLoader)).build();
        Log.debug("WithSpan class loaded from the application class loader " + classLoader);
        return annotationDescription;
    }


   private static AnnotationDescription getDigmaMarkerAnnotationDescription() {

        Log.debug("trying to load ExtendedObservability annotation class");
        AnnotationDescription annotationDescription = AnnotationDescription.Latent.Builder.ofType(ExtendedObservability.class).build();
        Log.debug("ExtendedObservability annotation class loaded");
        return annotationDescription;
    }

}
