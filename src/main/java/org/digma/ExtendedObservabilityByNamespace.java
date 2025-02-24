package org.digma;

import net.bytebuddy.asm.MemberAttributeExtension;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatcher;
import org.digma.configuration.Configuration;

public class ExtendedObservabilityByNamespace extends WithSpanTransformer {

    @Override
    protected ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return NamespaceTypeMatchers.create(Configuration.getInstance());
    }

    @Override
    protected ElementMatcher<? super MethodDescription> getMethodMatcher(TypeDescription typeDescription) {
        return NamespaceMethodMatchers.create(typeDescription, Configuration.getInstance());
    }

    @Override
    protected DynamicType.Builder<?> annotate(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader) throws Exception {

        //the WithSpan annotation should be loadable from the same class loader of the application class
        AnnotationDescription withSpanAnnotationDescription = getWithSpanAnnotationDescription(classLoader);

        AnnotationDescription digmaMarkerAnnotationDescription = getDigmaMarkerAnnotationDescription();

        return builder
                .visit(new MemberAttributeExtension.ForMethod()
                        .annotateMethod(withSpanAnnotationDescription)
                        .annotateMethod(digmaMarkerAnnotationDescription)
                        .on(getMethodMatcher(typeDescription)));
    }
}
