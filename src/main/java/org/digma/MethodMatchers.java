package org.digma;

import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.BooleanMatcher;
import net.bytebuddy.matcher.ElementMatcher;
import org.digma.configuration.Configuration;

import java.util.Objects;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.hasSuperMethod;
import static net.bytebuddy.matcher.ElementMatchers.*;
import static net.bytebuddy.matcher.ElementMatchers.isAnnotatedWith;
import static org.digma.Matchers.getNamedElementJunction;
import static org.digma.OtelClassNames.SPAN_ATTRIBUTE_CLASS_NAME;
import static org.digma.OtelClassNames.WITH_SPAN_CLASS_NAME;

public class MethodMatchers {

    private MethodMatchers() {
    }

    public static ElementMatcher<? super MethodDescription> create(TypeDescription typeDescription, Configuration configuration) {

        ElementMatcher.Junction<NamedElement> excludeNamesMatcher = none();

        for (String name : configuration.getExcludeNames()) {
            if (name.contains("#")) {
                String className = name.substring(0, name.lastIndexOf("#"));
                String methodName = name.substring(name.lastIndexOf("#") + 1);
                if(methodName.endsWith("*") && methodName.length() > 1) {
                    methodName = methodName.replace("*", "");
                    if (!methodName.isEmpty()) {
                        excludeNamesMatcher = excludeNamesMatcher.or(
                                nameStartsWithIgnoreCase(methodName).and(BooleanMatcher.of(Objects.equals(typeDescription.getCanonicalName(), className))));
                    }
                }else{
                    excludeNamesMatcher = excludeNamesMatcher.or(
                            named(methodName).and(BooleanMatcher.of(Objects.equals(typeDescription.getCanonicalName(), className))));
                }

            } else {
                excludeNamesMatcher = getNamedElementJunction(excludeNamesMatcher, name);
            }
        }


        return isMethod()
                .and(isDeclaredBy(typeDescription))
                .and(not(excludeNamesMatcher))
                .and(not(isSetter()))
                .and(not(isGetter()))
                .and(not(isMain()))
                .and(not(isConstructor()))
                .and(not(isFinalizer()))
                .and(not(isHashCode()))
                .and(not(isEquals()))
                .and(not(isClone()))
                .and(not(isAbstract()))
                .and(not(isToString()))
                .and(not(isTypeInitializer()))
                .and(not(isNative()))
                .and(not(isSynthetic()))
                .and(not(isBridge()))
                .and(not(methodsFilterByAnnotation()))
                .and(not(springScheduled()))
                .and(not(returns(named("kotlinx.coroutines.flow.Flow"))))
                .and(not(nameContains("$")));
    }

    private static ElementMatcher<? super MethodDescription> springScheduled() {
        return isAnnotatedWith(named("org.springframework.scheduling.annotation.Scheduled"));
    }


    private static ElementMatcher<? super MethodDescription> methodsFilterByAnnotation() {

        return isAnnotatedWith(namedOneOf(
                "org.springframework.web.bind.annotation.RequestMapping",
                "org.springframework.web.bind.annotation.GetMapping",
                "org.springframework.web.bind.annotation.PostMapping",
                "org.springframework.web.bind.annotation.DeleteMapping",
                "org.springframework.web.bind.annotation.PutMapping",
                "org.springframework.web.bind.annotation.PatchMapping"
        )).or(
                hasSuperMethod(
                        isAnnotatedWith(namedOneOf(
                                "javax.ws.rs.Path",
                                "javax.ws.rs.DELETE",
                                "javax.ws.rs.GET",
                                "javax.ws.rs.HEAD",
                                "javax.ws.rs.OPTIONS",
                                "javax.ws.rs.PATCH",
                                "javax.ws.rs.POST",
                                "javax.ws.rs.PUT"
                        )))
        ).or(
                isAnnotatedWith(namedOneOf(WITH_SPAN_CLASS_NAME, SPAN_ATTRIBUTE_CLASS_NAME))
        ).or(
                isAnnotatedWith(namedOneOf(
                        "org.junit.jupiter.api.Test",
                        "org.junit.jupiter.api.Disabled",
                        "org.junit.jupiter.api.BeforeEach",
                        "org.junit.jupiter.api.BeforeEach",
                        "org.junit.jupiter.api.BeforeAll",
                        "org.junit.jupiter.api.AfterAll",
                        "org.junit.jupiter.api.RepeatedTest",
                        "org.junit.jupiter.params.ParameterizedTest",
                        "org.junit.jupiter.api.TestFactory",
                        "org.junit.jupiter.api.TestTemplate",
                        "kotlin.test.Test",
                        "kotlin.test.BeforeEach",
                        "kotlin.test.BeforeEach",
                        "kotlin.test.Ignore",
                        "org.junit.Test",
                        "org.junit.Ignore",
                        "org.junit.Rule",
                        "org.junit.ClassRule",
                        "org.junit.BeforeClass",
                        "org.junit.Before",
                        "org.junit.AfterClass",
                        "org.junit.After",
                        "org.junit.runners.Parameterized.AfterParam",
                        "org.junit.runners.Parameterized.BeforeParam",
                        "org.junit.runners.Parameterized.Parameters"))
        ).or(
                isAnnotatedWith(namedOneOf(
                        "io.micrometer.tracing.annotation.NewSpan",
                        "io.micrometer.tracing.annotation.ContinueSpan",
                        "io.micrometer.observation.annotation.Observed"))
        );

    }


}
