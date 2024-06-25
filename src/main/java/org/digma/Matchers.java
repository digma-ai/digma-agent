package org.digma;

import net.bytebuddy.description.NamedElement;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class Matchers {


    public static ElementMatcher.Junction<NamedElement> getNamedElementJunction(ElementMatcher.Junction<NamedElement> excludeNamesMatcher, String name) {

        if (name.startsWith("*") && name.endsWith("*") && name.length() > 2) {
            name = name.replace("*", "");
            if (!name.isEmpty()) {
                return excludeNamesMatcher.or(nameContainsIgnoreCase(name));
            }
        } else if (name.startsWith("*") && name.length() > 1) {
            name = name.replace("*", "");
            if (!name.isEmpty()) {
                return excludeNamesMatcher.or(nameEndsWithIgnoreCase(name));
            }
        } else if (name.endsWith("*") && name.length() > 1) {
            name = name.replace("*", "");
            if (!name.isEmpty()) {
                return excludeNamesMatcher.or(nameStartsWithIgnoreCase(name));
            }
        } else {
            return excludeNamesMatcher.or(named(name));
        }

        //in case the conditions above didn't meet just return the original matcher
        return excludeNamesMatcher;
    }
}
