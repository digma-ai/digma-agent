package org.digma;

import net.bytebuddy.asm.MemberAttributeExtension;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatcher;
import org.digma.configuration.Configuration;

import static net.bytebuddy.matcher.ElementMatchers.*;
import static org.digma.OtelClassNames.WITH_SPAN_CLASS_NAME;

public class ExtendedObservabilityByAnnotation extends WithSpanTransformer {


    @Override
    protected ElementMatcher<? super TypeDescription> getTypeMatcher() {

        ElementMatcher.Junction<? super TypeDescription> hasAnnotatedMethodsMatcher = none();
        for (String annotationName : Configuration.getInstance().getMethodsAnnotations()) {
            hasAnnotatedMethodsMatcher = hasAnnotatedMethodsMatcher.or(declaresMethod(isAnnotatedWith(named(annotationName))));
        }

        return hasAnnotatedMethodsMatcher;
    }

    @Override
    protected ElementMatcher<? super MethodDescription> getMethodMatcher(TypeDescription typeDescription) {
        ElementMatcher.Junction<MethodDescription> annotatedMethodsMatcher = none();
        for (String annotationName : Configuration.getInstance().getMethodsAnnotations()) {
            annotatedMethodsMatcher = annotatedMethodsMatcher.or(isAnnotatedWith(named(annotationName)));
        }

        return isMethod().and(isDeclaredBy(typeDescription))
                .and(not(isAnnotatedWith(named(WITH_SPAN_CLASS_NAME))))
                .and(annotatedMethodsMatcher);
    }

    @Override
    protected DynamicType.Builder<?> annotate(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader) throws Exception {
        //the WithSpan annotation should be loadable from the same class loader of the application class
        AnnotationDescription withSpanAnnotationDescription = getWithSpanAnnotationDescription(classLoader);

        return builder
                .visit(new MemberAttributeExtension.ForMethod()
                        .annotateMethod(withSpanAnnotationDescription)
                        .on(getMethodMatcher(typeDescription)));
    }
}
