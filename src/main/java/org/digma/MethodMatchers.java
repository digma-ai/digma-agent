package org.digma;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.hasSuperMethod;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class MethodMatchers {


    public static ElementMatcher<? super MethodDescription> create(TypeDescription typeDescription) {

        return isMethod()
                .and(isDeclaredBy(typeDescription))
                .and(not(namedIgnoreCase("get")))
                .and(not(methodsFilterByAnnotation()))
                .and(not(isSynthetic()))
                .and(not(isBridge()))
                .and(not(isMain()))
                .and(not(isFinalizer()))
                .and(not(isHashCode()))
                .and(not(isEquals()))
                .and(not(isClone()))
                .and(not(isToString()))
                .and(not(isTypeInitializer()))
                .and(not(isSetter()))
                .and(not(isGetter()))
                .and(not(isNative()))
                .and(not(nameContains("$")));


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
                //for some reason otel in WithSpanInstrumentation checks for application.io.opentelemetry.instrumentation.annotations.WithSpan
                isAnnotatedWith(namedOneOf("io.opentelemetry.instrumentation.annotations.WithSpan",
                        "application.io.opentelemetry.instrumentation.annotations.WithSpan"))
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
