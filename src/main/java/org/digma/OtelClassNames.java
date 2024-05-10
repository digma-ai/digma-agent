package org.digma;

public class OtelClassNames {

    //we have shadow jar relocation for io.opentelemetry because we use opentelemetry-javaagent-extension-api and we must relocate it.
    // as a result shadow plugin will relocate any string starting with io.opentelemetry, but we don't want to relocate class names that
    // we need as is, for example io.opentelemetry.instrumentation.annotations.WithSpan.
    // these constants will trick shadow plugin not to relocate those class names.

    public static final String WITH_SPAN_CLASS_NAME =  "io_opentelemetry_instrumentation_annotations_WithSpan".replaceAll("_", ".");
    public static final String SPAN_ATTRIBUTE_CLASS_NAME =  "io_opentelemetry_instrumentation_annotations_SpanAttribute".replaceAll("_", ".");

}
