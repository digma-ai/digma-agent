package org.digma;

import com.example.testpkg.testclasses.MyTestClass;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.matcher.ElementMatchers;
import org.digma.matchers.NotGeneratedClassMatcher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GeneratedClassMatchersTests {

    @Test
    public void testNotGeneratedClass() {

        TypeDescription typeDescription = TypeDescription.ForLoadedType.of(MyTestClass.class);

        assertTrue(new NotGeneratedClassMatcher().matches(typeDescription, MyTestClass.class.getClassLoader(), null, MyTestClass.class, MyTestClass.class.getProtectionDomain()));
    }


    @Test
    public void testGeneratedClass() throws InstantiationException, IllegalAccessException {

        Class<?> dynamicType = new ByteBuddy()
                .subclass(Object.class)
                .method(ElementMatchers.named("toString"))
                .intercept(FixedValue.value("Hello World!"))
                .make()
                .load(getClass().getClassLoader())
                .getLoaded();

        assertEquals("Hello World!", dynamicType.newInstance().toString());


        TypeDescription typeDescription = TypeDescription.ForLoadedType.of(dynamicType);

        assertFalse(new NotGeneratedClassMatcher().matches(typeDescription, dynamicType.getClassLoader(), null, dynamicType, dynamicType.getProtectionDomain()));
    }


}
