package org.digma;

import com.example.testpkg.testclasses.MyStub;
import com.example.testpkg.testclasses.MyTestClass;
import com.example.testpkg.testclasses.StubClass;
import com.example.testpkg.testclasses.subpackage.ClassInSubPackage;
import com.example.testpkg.testclasses.subpackage.OtherClassInSubPackage;
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

public class ComplexMatchersTests extends AbstractConfigurationTest {


    @Test
    void testComplexMatchers() {
        Map<String, String> props = new HashMap<>();
        props.put(Configuration.DIGMA_AUTO_INSTRUMENT_PACKAGES_SYSTEM_PROPERTY, "com.nonexist;com.example.testpkg.testclasses.MyTestClass;com.test.package1;com.test.package2");
        props.put(Configuration.DIGMA_AUTO_INSTRUMENT_PACKAGES_EXCLUDE_NAMES_SYSTEM_PROPERTY, "");
        Configuration configuration = withProperties(props);

        ElementMatcher<? super TypeDescription> typeMatcher = TypeMatchers.create(configuration);

        assertTrue(typeMatcher.matches(TypeDescription.ForLoadedType.of(MyTestClass.class)));
        assertTrue(typeMatcher.matches(TypeDescription.ForLoadedType.of(Package1Class.class)));
        assertTrue(typeMatcher.matches(TypeDescription.ForLoadedType.of(Package1Class2.class)));
        assertTrue(typeMatcher.matches(TypeDescription.ForLoadedType.of(ClassInPackage1.class)));
        assertTrue(typeMatcher.matches(TypeDescription.ForLoadedType.of(Package2Class.class)));

        assertFalse(typeMatcher.matches(TypeDescription.ForLoadedType.of(MyStub.class)));
        assertFalse(typeMatcher.matches(TypeDescription.ForLoadedType.of(StubClass.class)));
        assertFalse(typeMatcher.matches(TypeDescription.ForLoadedType.of(ClassInSubPackage.class)));
        assertFalse(typeMatcher.matches(TypeDescription.ForLoadedType.of(OtherClassInSubPackage.class)));
        assertFalse(typeMatcher.matches(TypeDescription.ForLoadedType.of(Object.class)));


    }


    @Test
    void testComplexMatchersWithExcludes() throws NoSuchMethodException {
        Map<String, String> props = new HashMap<>();
        props.put(Configuration.DIGMA_AUTO_INSTRUMENT_PACKAGES_SYSTEM_PROPERTY, "com.nonexist;com.example.testpkg.testclasses.MyTestClass;com.test.package1;com.test.package2");
        props.put(Configuration.DIGMA_AUTO_INSTRUMENT_PACKAGES_EXCLUDE_NAMES_SYSTEM_PROPERTY, "com.test.package1.Package1Class;*method*;*Test;Stub*;*Stub;bbbbFunc");
        Configuration configuration = withProperties(props);

        ElementMatcher<? super TypeDescription> typeMatcher = TypeMatchers.create(configuration);

        assertTrue(typeMatcher.matches(TypeDescription.ForLoadedType.of(MyTestClass.class)));
        assertTrue(typeMatcher.matches(TypeDescription.ForLoadedType.of(Package1Class2.class)));
        assertTrue(typeMatcher.matches(TypeDescription.ForLoadedType.of(ClassInPackage1.class)));
        assertTrue(typeMatcher.matches(TypeDescription.ForLoadedType.of(Package2Class.class)));

        assertFalse(typeMatcher.matches(TypeDescription.ForLoadedType.of(Package1Class.class)));
        assertFalse(typeMatcher.matches(TypeDescription.ForLoadedType.of(MyStub.class)));
        assertFalse(typeMatcher.matches(TypeDescription.ForLoadedType.of(StubClass.class)));
        assertFalse(typeMatcher.matches(TypeDescription.ForLoadedType.of(ClassInSubPackage.class)));
        assertFalse(typeMatcher.matches(TypeDescription.ForLoadedType.of(OtherClassInSubPackage.class)));
        assertFalse(typeMatcher.matches(TypeDescription.ForLoadedType.of(Object.class)));


        ElementMatcher<? super MethodDescription> methodMatcherMyTestClass = MethodMatchers.create(TypeDescription.ForLoadedType.of(MyTestClass.class), configuration);
        assertFalse(methodMatcherMyTestClass.matches(new MethodDescription.ForLoadedMethod(MyTestClass.class.getDeclaredMethod("myTestMethod1"))));
        assertFalse(methodMatcherMyTestClass.matches(new MethodDescription.ForLoadedMethod(MyTestClass.class.getDeclaredMethod("myTestMethod2"))));
        assertFalse(methodMatcherMyTestClass.matches(new MethodDescription.ForLoadedMethod(MyTestClass.class.getDeclaredMethod("test"))));
        assertTrue(methodMatcherMyTestClass.matches(new MethodDescription.ForLoadedMethod(MyTestClass.class.getDeclaredMethod("test2"))));


        ElementMatcher<? super MethodDescription> methodMatcherMyStub = MethodMatchers.create(TypeDescription.ForLoadedType.of(MyStub.class), configuration);
        assertFalse(methodMatcherMyStub.matches(new MethodDescription.ForLoadedMethod(MyStub.class.getDeclaredMethod("myStubMethod"))));
        assertTrue(methodMatcherMyStub.matches(new MethodDescription.ForLoadedMethod(MyStub.class.getDeclaredMethod("myStubFunction"))));
        assertFalse(methodMatcherMyStub.matches(new MethodDescription.ForLoadedMethod(MyStub.class.getDeclaredMethod("myTest"))));

        ElementMatcher<? super MethodDescription> methodMatcherStubClass = MethodMatchers.create(TypeDescription.ForLoadedType.of(StubClass.class), configuration);
        assertFalse(methodMatcherStubClass.matches(new MethodDescription.ForLoadedMethod(StubClass.class.getDeclaredMethod("stubFunc"))));
        assertFalse(methodMatcherStubClass.matches(new MethodDescription.ForLoadedMethod(StubClass.class.getDeclaredMethod("myNoneStub"))));

        ElementMatcher<? super MethodDescription> methodMatcherPackage2Class = MethodMatchers.create(TypeDescription.ForLoadedType.of(Package2Class.class), configuration);
        assertTrue(methodMatcherPackage2Class.matches(new MethodDescription.ForLoadedMethod(Package2Class.class.getDeclaredMethod("myFunctionOnPackage2Class"))));

        ElementMatcher<? super MethodDescription> methodMatcherPackage1Class2 = MethodMatchers.create(TypeDescription.ForLoadedType.of(Package1Class2.class), configuration);
        assertFalse(methodMatcherPackage1Class2.matches(new MethodDescription.ForLoadedMethod(Package1Class2.class.getDeclaredMethod("bbbbFunc"))));

    }


    @Test
    void testComplexMatchersWithTopPackagesAndExcludes() throws NoSuchMethodException {
        Map<String, String> props = new HashMap<>();
        props.put(Configuration.DIGMA_AUTO_INSTRUMENT_PACKAGES_SYSTEM_PROPERTY, "com.nonexist;com.example;com.test");
        props.put(Configuration.DIGMA_AUTO_INSTRUMENT_PACKAGES_EXCLUDE_NAMES_SYSTEM_PROPERTY, "com.test.package1.Package1Class;*SubPackage;*method*;*Test;Stub*;*Stub;bbbbFunc");
        Configuration configuration = withProperties(props);

        ElementMatcher<? super TypeDescription> typeMatcher = TypeMatchers.create(configuration);

        assertTrue(typeMatcher.matches(TypeDescription.ForLoadedType.of(MyTestClass.class)));
        assertTrue(typeMatcher.matches(TypeDescription.ForLoadedType.of(Package1Class2.class)));
        assertTrue(typeMatcher.matches(TypeDescription.ForLoadedType.of(ClassInPackage1.class)));
        assertTrue(typeMatcher.matches(TypeDescription.ForLoadedType.of(Package2Class.class)));
        assertTrue(typeMatcher.matches(TypeDescription.ForLoadedType.of(StubClass.class)));

        assertFalse(typeMatcher.matches(TypeDescription.ForLoadedType.of(Package1Class.class)));
        assertFalse(typeMatcher.matches(TypeDescription.ForLoadedType.of(MyStub.class)));
        assertFalse(typeMatcher.matches(TypeDescription.ForLoadedType.of(Object.class)));
        assertFalse(typeMatcher.matches(TypeDescription.ForLoadedType.of(ClassInSubPackage.class)));
        assertFalse(typeMatcher.matches(TypeDescription.ForLoadedType.of(OtherClassInSubPackage.class)));

        ElementMatcher<? super MethodDescription> methodMatcherMyTestClass = MethodMatchers.create(TypeDescription.ForLoadedType.of(MyTestClass.class), configuration);
        assertFalse(methodMatcherMyTestClass.matches(new MethodDescription.ForLoadedMethod(MyTestClass.class.getDeclaredMethod("myTestMethod1"))));
        assertFalse(methodMatcherMyTestClass.matches(new MethodDescription.ForLoadedMethod(MyTestClass.class.getDeclaredMethod("myTestMethod2"))));
        assertFalse(methodMatcherMyTestClass.matches(new MethodDescription.ForLoadedMethod(MyTestClass.class.getDeclaredMethod("test"))));
        assertTrue(methodMatcherMyTestClass.matches(new MethodDescription.ForLoadedMethod(MyTestClass.class.getDeclaredMethod("test2"))));


        ElementMatcher<? super MethodDescription> methodMatcherMyStub = MethodMatchers.create(TypeDescription.ForLoadedType.of(MyStub.class), configuration);
        assertFalse(methodMatcherMyStub.matches(new MethodDescription.ForLoadedMethod(MyStub.class.getDeclaredMethod("myStubMethod"))));
        assertTrue(methodMatcherMyStub.matches(new MethodDescription.ForLoadedMethod(MyStub.class.getDeclaredMethod("myStubFunction"))));
        assertFalse(methodMatcherMyStub.matches(new MethodDescription.ForLoadedMethod(MyStub.class.getDeclaredMethod("myTest"))));

        ElementMatcher<? super MethodDescription> methodMatcherStubClass = MethodMatchers.create(TypeDescription.ForLoadedType.of(StubClass.class), configuration);
        assertFalse(methodMatcherStubClass.matches(new MethodDescription.ForLoadedMethod(StubClass.class.getDeclaredMethod("stubFunc"))));
        assertFalse(methodMatcherStubClass.matches(new MethodDescription.ForLoadedMethod(StubClass.class.getDeclaredMethod("myNoneStub"))));

        ElementMatcher<? super MethodDescription> methodMatcherPackage2Class = MethodMatchers.create(TypeDescription.ForLoadedType.of(Package2Class.class), configuration);
        assertTrue(methodMatcherPackage2Class.matches(new MethodDescription.ForLoadedMethod(Package2Class.class.getDeclaredMethod("myFunctionOnPackage2Class"))));

        ElementMatcher<? super MethodDescription> methodMatcherPackage1Class2 = MethodMatchers.create(TypeDescription.ForLoadedType.of(Package1Class2.class), configuration);
        assertFalse(methodMatcherPackage1Class2.matches(new MethodDescription.ForLoadedMethod(Package1Class2.class.getDeclaredMethod("bbbbFunc"))));

    }


}
