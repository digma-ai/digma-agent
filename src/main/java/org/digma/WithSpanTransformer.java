package org.digma;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatcher;
import org.digma.configuration.Configuration;
import org.digma.instrumentation.ExtendedObservability;
import org.digma.matchers.NotGeneratedClassMatcher;

import java.lang.annotation.Annotation;
import java.lang.instrument.Instrumentation;

import static org.digma.OtelClassNames.WITH_SPAN_CLASS_NAME;

public abstract class WithSpanTransformer {


    protected abstract ElementMatcher<? super TypeDescription> getTypeMatcher();

    protected abstract ElementMatcher<? super MethodDescription> getMethodMatcher(TypeDescription typeDescription);

    //the different implementation may need to annotate different annotations.
    //for example in ExtendedObservabilityByAnnotation we don't need the org.digma.instrumentation.ExtendedObservability
    protected abstract DynamicType.Builder<?> annotate(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader) throws Exception;


    public void install(Instrumentation inst) {

        Log.debug("Extended observability is enabled, installing WithSpan transformer.");
        Log.debug("Digma agent started with extended observability configuration: " +
                "includePackages=" + Configuration.getInstance().getIncludePackages()
                + ",excludeNames=" + Configuration.getInstance().getExcludeNames()
                + ",methodsAnnotations=" + Configuration.getInstance().getMethodsAnnotations());


        try {
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
        } catch (Throwable e) {
            Log.error("failed injecting WithSpan to class loader", e);
        }

        try {

            //if we fail to load bytebuddy nothing will work
            Class<ByteBuddy> byteBuddyClass = ByteBuddy.class;
            Log.debug("byteBuddy Class " + byteBuddyClass.getName() + ", class loader: " + byteBuddyClass.getClassLoader());
            installTransformer(inst);

        } catch (Throwable e) {
            Log.error("failed to install WithSpanTransformer in Digma agent", e);
        }

    }


    private void makeSureWithSpanClassIsAvailable() {

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


    private void injectOtelApiToSystemClassLoader() {
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


    private void installTransformer(Instrumentation inst) {

        Log.debug("installing withSpanTransformer");

        new AgentBuilder.Default()
                .type(getTypeMatcher())
                .and(new NotGeneratedClassMatcher())
                .transform((builder, typeDescription, classLoader, module, protectionDomain) -> {

                    try {

                        Log.debug("transforming " + typeDescription.getCanonicalName() + " in class loader " + classLoader);

                        return annotate(builder, typeDescription, classLoader);

                    } catch (Throwable e) {
                        Log.error("got exception in bytebuddy transformer", e);
                        return builder;
                    }

                }).installOn(inst);
    }


    @SuppressWarnings("unchecked")
    public static AnnotationDescription getWithSpanAnnotationDescription(ClassLoader classLoader) throws Exception {

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


    public static AnnotationDescription getDigmaMarkerAnnotationDescription() {

        Log.debug("trying to load ExtendedObservability annotation class");
        AnnotationDescription annotationDescription = AnnotationDescription.Latent.Builder.ofType(ExtendedObservability.class).build();
        Log.debug("ExtendedObservability annotation class loaded");
        return annotationDescription;
    }

}
