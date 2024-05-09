package org.digma;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.List;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.extendsClass;
import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.implementsInterface;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class TypeMatchers {

    private TypeMatchers() {
    }

    public static ElementMatcher<? super TypeDescription> create(List<String> packageNames){

        ElementMatcher.Junction<? super TypeDescription> packageMatcher = none();

        for (String packageName : packageNames) {
            packageMatcher = packageMatcher.or(ElementMatchers.nameStartsWith(packageName + "."));
        }

        return digmaTypeMatcher(packageMatcher);
    }



    public static ElementMatcher<? super TypeDescription> digmaTypeMatcher(ElementMatcher.Junction<? super TypeDescription> packageMatcher) {
//        return ElementMatchers.nameStartsWith(packageName + ".")
        return packageMatcher
                .and(not(typeFilterByAnnotation()))
                .and(not(isSynthetic()))
                .and(not(isEnum()))
                .and(not(extendsClass(named("io.grpc.ServerBuilder"))))
                .and(not(jaxrsTypes()))
                .and(not(kafkaTypes()))
                .and(not(implementsInterface(named("org.apache.camel.CamelContext"))))
                .and(not(hibernate6Types()))
                .and(not(hibernate4Types()))
                .and(not(nameContains("$")));
    }



    private static ElementMatcher<? super TypeDescription> hibernate6Types(){
        return implementsInterface(namedOneOf(
                "org.hibernate.query.CommonQueryContract",
                "org.hibernate.SessionFactory",
                "org.hibernate.SessionBuilder",
                "org.hibernate.SharedSessionContract",
                "org.hibernate.Transaction"));
    }

    private  static ElementMatcher<? super TypeDescription> hibernate4Types(){
        return implementsInterface(namedOneOf(
                "org.hibernate.Criteria",
                "org.hibernate.Query",
                "org.hibernate.SessionFactory",
                "org.hibernate.SessionBuilder",
                "org.hibernate.SharedSessionContract",
                "org.hibernate.Transaction"));
    }

    private  static ElementMatcher<? super TypeDescription> jaxrsTypes(){
        return hasSuperType(
                isAnnotatedWith(named("javax.ws.rs.Path"))
                        .or(declaresMethod(isAnnotatedWith(named("javax.ws.rs.Path")))));
    }

    private  static ElementMatcher<? super TypeDescription> kafkaTypes(){
        return declaresMethod(isAnnotatedWith(named("org.springframework.kafka.annotation.KafkaListener")))
                .or(
                        extendsClass(
                                declaresMethod(isAnnotatedWith(named("org.springframework.kafka.annotation.KafkaListener")))));
    }


    private  static ElementMatcher<? super TypeDescription> typeFilterByAnnotation() {
        return isAnnotatedWith(namedOneOf(
                "org.junit.jupiter.api.Disabled",
                "org.junit.Ignore")
        );
    }



}
