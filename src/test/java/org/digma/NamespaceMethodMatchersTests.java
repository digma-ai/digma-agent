package org.digma;

import com.example.testpkg.testclasses.MyStub;
import com.example.testpkg.testclasses.MyTestClass;
import com.example.testpkg.testclasses.StubClass;
import com.test.package1.ClassInPackage1;
import com.test.package1.Package1Class;
import com.test.package1.Package1Class2;
import com.test.package2.Package2Class;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.digma.configuration.AbstractConfigurationTest;
import org.digma.configuration.Configuration;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NamespaceMethodMatchersTests extends AbstractConfigurationTest {


    /*
    Note: matching a class simple name that starts with a string is not easy with bytebuddy matchers, probably only with a regexp.
    currently to match classes that start with a string it's better to include all the package,
    for example to match all classes that start with the string 'Generated':
    com.example.Generated*
    or *.Generated* ,but this will also include com.pkg.generated.MyClass , so it's better to include the package name as above.
     */


    @Test
    public void testIncludeFQN() throws NoSuchMethodException {
        Map<String,String> props = new HashMap<>();
        props.put(Configuration.DIGMA_AUTO_INSTRUMENT_PACKAGES_SYSTEM_PROPERTY,"com.nonexist;com.example.testpkg.testclasses.MyTestClass;com.test.package1.Package1Class");
        Configuration configuration = withProperties(props);

        ElementMatcher<? super MethodDescription> matcher = NamespaceMethodMatchers.create(TypeDescription.ForLoadedType.of(MyTestClass.class),configuration);

        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(MyTestClass.class.getDeclaredMethod("myTestMethod1"))));
        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(MyTestClass.class.getDeclaredMethod("myTestMethod2"))));
        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(MyTestClass.class.getDeclaredMethod("test"))));
        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(MyTestClass.class.getDeclaredMethod("test2"))));

        matcher = NamespaceMethodMatchers.create(TypeDescription.ForLoadedType.of(Package1Class.class),configuration);

        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(Package1Class.class.getDeclaredMethod("myPackage1ClassMethod1"))));
        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(Package1Class.class.getDeclaredMethod("myPackage1ClassMethod2"))));
        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(Package1Class.class.getDeclaredMethod("test"))));
    }

    @Test
    public void testExcludeMethodByFQN() throws NoSuchMethodException {
        Map<String,String> props = new HashMap<>();
        props.put(Configuration.DIGMA_AUTO_INSTRUMENT_PACKAGES_SYSTEM_PROPERTY,"com.nonexist;com.example.testpkg.testclasses.MyTestClass;com.test.package1.Package1Class");
        props.put(Configuration.DIGMA_AUTO_INSTRUMENT_PACKAGES_EXCLUDE_NAMES_SYSTEM_PROPERTY,"com.example.testpkg.testclasses.MyTestClass#myTestMethod2;com.test.package1.Package1Class#myPackage1ClassMethod2");
        Configuration configuration = withProperties(props);

        ElementMatcher<? super MethodDescription> matcher = NamespaceMethodMatchers.create(TypeDescription.ForLoadedType.of(MyTestClass.class),configuration);

        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(MyTestClass.class.getDeclaredMethod("myTestMethod1"))));
        assertFalse(matcher.matches(new MethodDescription.ForLoadedMethod(MyTestClass.class.getDeclaredMethod("myTestMethod2"))));

        matcher = NamespaceMethodMatchers.create(TypeDescription.ForLoadedType.of(Package1Class.class),configuration);

        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(Package1Class.class.getDeclaredMethod("myPackage1ClassMethod1"))));
        assertFalse(matcher.matches(new MethodDescription.ForLoadedMethod(Package1Class.class.getDeclaredMethod("myPackage1ClassMethod2"))));
    }

    @Test
    public void testExcludeMethodThatEndWithString() throws NoSuchMethodException {
        Map<String,String> props = new HashMap<>();
        props.put(Configuration.DIGMA_AUTO_INSTRUMENT_PACKAGES_SYSTEM_PROPERTY,"com.nonexist;com.example.testpkg.testclasses.MyTestClass;com.test.package1.Package1Class");
        props.put(Configuration.DIGMA_AUTO_INSTRUMENT_PACKAGES_EXCLUDE_NAMES_SYSTEM_PROPERTY,"*Method2");
        Configuration configuration = withProperties(props);

        ElementMatcher<? super MethodDescription> matcher = NamespaceMethodMatchers.create(TypeDescription.ForLoadedType.of(MyTestClass.class),configuration);

        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(MyTestClass.class.getDeclaredMethod("myTestMethod1"))));
        assertFalse(matcher.matches(new MethodDescription.ForLoadedMethod(MyTestClass.class.getDeclaredMethod("myTestMethod2"))));

        matcher = NamespaceMethodMatchers.create(TypeDescription.ForLoadedType.of(Package1Class.class),configuration);

        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(Package1Class.class.getDeclaredMethod("myPackage1ClassMethod1"))));
        assertFalse(matcher.matches(new MethodDescription.ForLoadedMethod(Package1Class.class.getDeclaredMethod("myPackage1ClassMethod2"))));
    }

    @Test
    public void testExcludeMethodThatStartWithString() throws NoSuchMethodException {
        Map<String,String> props = new HashMap<>();
        props.put(Configuration.DIGMA_AUTO_INSTRUMENT_PACKAGES_SYSTEM_PROPERTY,"com.nonexist;com.example.testpkg.testclasses.MyTestClass;com.test.package1.Package1Class");
        props.put(Configuration.DIGMA_AUTO_INSTRUMENT_PACKAGES_EXCLUDE_NAMES_SYSTEM_PROPERTY,"myTest*");
        Configuration configuration = withProperties(props);

        ElementMatcher<? super MethodDescription> matcher = NamespaceMethodMatchers.create(TypeDescription.ForLoadedType.of(MyTestClass.class),configuration);

        assertFalse(matcher.matches(new MethodDescription.ForLoadedMethod(MyTestClass.class.getDeclaredMethod("myTestMethod1"))));
        assertFalse(matcher.matches(new MethodDescription.ForLoadedMethod(MyTestClass.class.getDeclaredMethod("myTestMethod2"))));

        matcher = NamespaceMethodMatchers.create(TypeDescription.ForLoadedType.of(Package1Class.class),configuration);

        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(Package1Class.class.getDeclaredMethod("myPackage1ClassMethod1"))));
        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(Package1Class.class.getDeclaredMethod("myPackage1ClassMethod2"))));
    }

    @Test
    public void testExcludeMethodThatContainString() throws NoSuchMethodException {
        Map<String,String> props = new HashMap<>();
        props.put(Configuration.DIGMA_AUTO_INSTRUMENT_PACKAGES_SYSTEM_PROPERTY,"com.nonexist;com.example;com.test");
        props.put(Configuration.DIGMA_AUTO_INSTRUMENT_PACKAGES_EXCLUDE_NAMES_SYSTEM_PROPERTY,"*method*");
        Configuration configuration = withProperties(props);

        ElementMatcher<? super MethodDescription> matcher = NamespaceMethodMatchers.create(TypeDescription.ForLoadedType.of(MyTestClass.class),configuration);
        assertFalse(matcher.matches(new MethodDescription.ForLoadedMethod(MyTestClass.class.getDeclaredMethod("myTestMethod1"))));
        assertFalse(matcher.matches(new MethodDescription.ForLoadedMethod(MyTestClass.class.getDeclaredMethod("myTestMethod2"))));
        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(MyTestClass.class.getDeclaredMethod("test"))));

        matcher = NamespaceMethodMatchers.create(TypeDescription.ForLoadedType.of(Package1Class.class),configuration);
        assertFalse(matcher.matches(new MethodDescription.ForLoadedMethod(Package1Class.class.getDeclaredMethod("myPackage1ClassMethod1"))));
        assertFalse(matcher.matches(new MethodDescription.ForLoadedMethod(Package1Class.class.getDeclaredMethod("myPackage1ClassMethod2"))));
        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(Package1Class.class.getDeclaredMethod("test"))));

        matcher = NamespaceMethodMatchers.create(TypeDescription.ForLoadedType.of(StubClass.class),configuration);
        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(StubClass.class.getDeclaredMethod("stubFunc"))));
        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(StubClass.class.getDeclaredMethod("myNoneStub"))));

        matcher = NamespaceMethodMatchers.create(TypeDescription.ForLoadedType.of(MyStub.class),configuration);
        assertFalse(matcher.matches(new MethodDescription.ForLoadedMethod(MyStub.class.getDeclaredMethod("myStubMethod"))));
        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(MyStub.class.getDeclaredMethod("myStubFunction"))));
        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(MyStub.class.getDeclaredMethod("myTest"))));
    }



    @Test
    public void testMultipleExcludeForMethods() throws NoSuchMethodException {
        Map<String,String> props = new HashMap<>();
        props.put(Configuration.DIGMA_AUTO_INSTRUMENT_PACKAGES_SYSTEM_PROPERTY,"com.nonexist;com.example;com.test");
        props.put(Configuration.DIGMA_AUTO_INSTRUMENT_PACKAGES_EXCLUDE_NAMES_SYSTEM_PROPERTY,"*method*;bbb*;*func;com.example.testpkg.testclasses.MyTestClass#myTestMethod2;");
        Configuration configuration = withProperties(props);

        ElementMatcher<? super MethodDescription> matcher = NamespaceMethodMatchers.create(TypeDescription.ForLoadedType.of(MyTestClass.class),configuration);
        assertFalse(matcher.matches(new MethodDescription.ForLoadedMethod(MyTestClass.class.getDeclaredMethod("myTestMethod1"))));
        assertFalse(matcher.matches(new MethodDescription.ForLoadedMethod(MyTestClass.class.getDeclaredMethod("myTestMethod2"))));
        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(MyTestClass.class.getDeclaredMethod("test"))));
        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(MyTestClass.class.getDeclaredMethod("test2"))));

        matcher = NamespaceMethodMatchers.create(TypeDescription.ForLoadedType.of(Package1Class.class),configuration);
        assertFalse(matcher.matches(new MethodDescription.ForLoadedMethod(Package1Class.class.getDeclaredMethod("myPackage1ClassMethod1"))));
        assertFalse(matcher.matches(new MethodDescription.ForLoadedMethod(Package1Class.class.getDeclaredMethod("myPackage1ClassMethod2"))));
        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(Package1Class.class.getDeclaredMethod("test"))));

        matcher = NamespaceMethodMatchers.create(TypeDescription.ForLoadedType.of(Package1Class2.class),configuration);
        assertFalse(matcher.matches(new MethodDescription.ForLoadedMethod(Package1Class2.class.getDeclaredMethod("bbbbFunc"))));

        matcher = NamespaceMethodMatchers.create(TypeDescription.ForLoadedType.of(ClassInPackage1.class),configuration);
        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(ClassInPackage1.class.getDeclaredMethod("aaaa"))));

        matcher = NamespaceMethodMatchers.create(TypeDescription.ForLoadedType.of(Package2Class.class),configuration);
        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(Package2Class.class.getDeclaredMethod("myFunctionOnPackage2Class"))));

        matcher = NamespaceMethodMatchers.create(TypeDescription.ForLoadedType.of(StubClass.class),configuration);
        assertFalse(matcher.matches(new MethodDescription.ForLoadedMethod(StubClass.class.getDeclaredMethod("stubFunc"))));
        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(StubClass.class.getDeclaredMethod("myNoneStub"))));

        matcher = NamespaceMethodMatchers.create(TypeDescription.ForLoadedType.of(MyStub.class),configuration);
        assertFalse(matcher.matches(new MethodDescription.ForLoadedMethod(MyStub.class.getDeclaredMethod("myStubMethod"))));
        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(MyStub.class.getDeclaredMethod("myStubFunction"))));
        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(MyStub.class.getDeclaredMethod("myTest"))));
    }

    @Test
    public void testExcludeMethodsOnClassThatStartWith() throws NoSuchMethodException {
        Map<String,String> props = new HashMap<>();
        props.put(Configuration.DIGMA_AUTO_INSTRUMENT_PACKAGES_SYSTEM_PROPERTY,"com.nonexist;com.example;com.test");
        props.put(Configuration.DIGMA_AUTO_INSTRUMENT_PACKAGES_EXCLUDE_NAMES_SYSTEM_PROPERTY,";myPackage1ClassMethod*;bbb*;*func;com.example.testpkg.testclasses.MyTestClass#myTest*;");
        Configuration configuration = withProperties(props);

        ElementMatcher<? super MethodDescription> matcher = NamespaceMethodMatchers.create(TypeDescription.ForLoadedType.of(MyTestClass.class),configuration);
        assertFalse(matcher.matches(new MethodDescription.ForLoadedMethod(MyTestClass.class.getDeclaredMethod("myTestMethod1"))));
        assertFalse(matcher.matches(new MethodDescription.ForLoadedMethod(MyTestClass.class.getDeclaredMethod("myTestMethod2"))));
        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(MyTestClass.class.getDeclaredMethod("test"))));
        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(MyTestClass.class.getDeclaredMethod("test2"))));

        matcher = NamespaceMethodMatchers.create(TypeDescription.ForLoadedType.of(Package1Class.class),configuration);
        assertFalse(matcher.matches(new MethodDescription.ForLoadedMethod(Package1Class.class.getDeclaredMethod("myPackage1ClassMethod1"))));
        assertFalse(matcher.matches(new MethodDescription.ForLoadedMethod(Package1Class.class.getDeclaredMethod("myPackage1ClassMethod2"))));
        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(Package1Class.class.getDeclaredMethod("test"))));

        matcher = NamespaceMethodMatchers.create(TypeDescription.ForLoadedType.of(Package1Class2.class),configuration);
        assertFalse(matcher.matches(new MethodDescription.ForLoadedMethod(Package1Class2.class.getDeclaredMethod("bbbbFunc"))));

        matcher = NamespaceMethodMatchers.create(TypeDescription.ForLoadedType.of(ClassInPackage1.class),configuration);
        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(ClassInPackage1.class.getDeclaredMethod("aaaa"))));

        matcher = NamespaceMethodMatchers.create(TypeDescription.ForLoadedType.of(Package2Class.class),configuration);
        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(Package2Class.class.getDeclaredMethod("myFunctionOnPackage2Class"))));

        matcher = NamespaceMethodMatchers.create(TypeDescription.ForLoadedType.of(StubClass.class),configuration);
        assertFalse(matcher.matches(new MethodDescription.ForLoadedMethod(StubClass.class.getDeclaredMethod("stubFunc"))));
        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(StubClass.class.getDeclaredMethod("myNoneStub"))));

        matcher = NamespaceMethodMatchers.create(TypeDescription.ForLoadedType.of(MyStub.class),configuration);
        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(MyStub.class.getDeclaredMethod("myStubMethod"))));
        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(MyStub.class.getDeclaredMethod("myStubFunction"))));
        assertTrue(matcher.matches(new MethodDescription.ForLoadedMethod(MyStub.class.getDeclaredMethod("myTest"))));
    }


}
