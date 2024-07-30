package org.digma;

import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.digma.configuration.Configuration;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.extendsClass;
import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.implementsInterface;
import static net.bytebuddy.matcher.ElementMatchers.*;
import static org.digma.Matchers.getNamedElementJunction;

public class TypeMatchers {

    private TypeMatchers() {
    }

    public static ElementMatcher<? super TypeDescription> create(Configuration configuration) {

        ElementMatcher.Junction<? super TypeDescription> packageMatcher = none();

        for (String packageName : configuration.getIncludePackages()) {
            packageMatcher = packageMatcher.or(nameStartsWith(packageName + ".")).or(named(packageName));
        }

        ElementMatcher.Junction<NamedElement> excludeNamesMatcher = none();
        for (String name : configuration.getExcludeNames()) {
            excludeNamesMatcher = getNamedElementJunction(excludeNamesMatcher, name);
        }

        return digmaTypeMatcher(packageMatcher, excludeNamesMatcher);
    }


    public static ElementMatcher<? super TypeDescription> digmaTypeMatcher(ElementMatcher.Junction<? super TypeDescription> packageMatcher, ElementMatcher.Junction<? super TypeDescription> excludeNamesMatcher) {
        return packageMatcher
                .and(not(excludeNamesMatcher))
                .and(not(typeFilterByAnnotation()))
                .and(not(isSynthetic()))
                .and(not(isRecord()))
                .and(not(isEnum()))
                .and(not(extendsClass(named("io.grpc.ServerBuilder"))))
                .and(not(jaxrsTypes()))
                .and(not(kafkaTypes()))
                .and(not(implementsInterface(named("org.apache.camel.CamelContext"))))
                .and(not(hibernate6Types()))
                .and(not(hibernate4Types()))
                .and(not(isKotlinDataClass()))
                .and(not(isSpringGeneratedClass()))
                .and(not(nameContains("$")));
    }


    //todo: research and add more spring interfaces implemented by generated classes.
    // see for example in spring data:
    // org.springframework.data.mapping.model.ClassGeneratingEntityInstantiator
    // org.springframework.data.mapping.model.ClassGeneratingPropertyAccessorFactory
    private static ElementMatcher<? super TypeDescription> isSpringGeneratedClass() {
        return implementsInterface(namedOneOf(
                "org.springframework.data.mapping.PersistentPropertyAccessor",
                "org.springframework.data.mapping.model.ClassGeneratingEntityInstantiator$ObjectInstantiator"));
    }


    private static ElementMatcher<? super TypeDescription> hibernate6Types() {
        return implementsInterface(namedOneOf(
                "org.hibernate.query.CommonQueryContract",
                "org.hibernate.SessionFactory",
                "org.hibernate.SessionBuilder",
                "org.hibernate.SharedSessionContract",
                "org.hibernate.Transaction"));
    }

    private static ElementMatcher<? super TypeDescription> hibernate4Types() {
        return implementsInterface(namedOneOf(
                "org.hibernate.Criteria",
                "org.hibernate.Query",
                "org.hibernate.SessionFactory",
                "org.hibernate.SessionBuilder",
                "org.hibernate.SharedSessionContract",
                "org.hibernate.Transaction"));
    }

    private static ElementMatcher<? super TypeDescription> jaxrsTypes() {
        return hasSuperType(
                isAnnotatedWith(named("javax.ws.rs.Path"))
                        .or(declaresMethod(isAnnotatedWith(named("javax.ws.rs.Path")))));
    }

    private static ElementMatcher<? super TypeDescription> kafkaTypes() {
        return declaresMethod(isAnnotatedWith(named("org.springframework.kafka.annotation.KafkaListener")))
                .or(
                        extendsClass(
                                declaresMethod(isAnnotatedWith(named("org.springframework.kafka.annotation.KafkaListener")))));
    }


    private static ElementMatcher<? super TypeDescription> typeFilterByAnnotation() {
        return isAnnotatedWith(namedOneOf(
                "org.junit.jupiter.api.Disabled",
                "org.junit.Ignore")
        );
    }


    //some characteristics of kotlin data classes. if found in regular classes these classes will not be instrumented.
    private static ElementMatcher<? super TypeDescription> isKotlinDataClass(){
        return declaresMethod(named("copy"))
                .and(declaresMethod(nameStartsWith("component")));
    }




}
