package org.digma;

import com.example.testpkg.testclasses.MyStub;
import com.example.testpkg.testclasses.StubClass;
import com.test.package1.ClassInPackage1;
import com.test.package1.Package1Class;
import com.test.package1.Package1Class2;
import com.test.package2.Package2Class;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.digma.configuration.AbstractConfigurationTest;
import org.digma.configuration.Configuration;
import com.example.testpkg.testclasses.MyTestClass;
import com.example.testpkg.testclasses.subpackage.ClassInSubPackage;
import com.example.testpkg.testclasses.subpackage.OtherClassInSubPackage;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NamespaceTypeMatchersTests extends AbstractConfigurationTest {


    /*
    Note: matching a class simple name that starts with a string is not easy with bytebuddy matchers, probably only with a regexp.
    currently to match classes that start with a string its better to include all the package,
    for example to match all classes that start with the string 'Generated':
    com.example.Generated*
    or *.Generated* ,but this will also include com.pkg.generated.MyClass , so it's better to include the package name as above.
     */


    @Test
    public void testIncludeFQN(){
        Map<String,String> props = new HashMap<>();
        props.put(Configuration.DIGMA_AUTO_INSTRUMENT_PACKAGES_SYSTEM_PROPERTY,"com.nonexist;com.example.testpkg.testclasses.MyTestClass;com.test.package1.Package1Class");
        Configuration configuration = withProperties(props);
        ElementMatcher<? super TypeDescription> matcher =  NamespaceTypeMatchers.create(configuration);

        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(MyTestClass.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(Package1Class.class)));

        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(ClassInSubPackage.class)));
        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(OtherClassInSubPackage.class)));
        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(Package1Class2.class)));
        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(ClassInPackage1.class)));
        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(Package2Class.class)));
        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(MyStub.class)));
        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(StubClass.class)));
        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(Object.class)));
    }


    @Test
    public void testIncludePackage(){
        Map<String,String> props = new HashMap<>();
        props.put(Configuration.DIGMA_AUTO_INSTRUMENT_PACKAGES_SYSTEM_PROPERTY,"com.nonexist;com.example.testpkg;com.test.package1");
        Configuration configuration = withProperties(props);

        ElementMatcher<? super TypeDescription> matcher =  NamespaceTypeMatchers.create(configuration);

        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(MyTestClass.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(ClassInSubPackage.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(OtherClassInSubPackage.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(StubClass.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(MyStub.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(Package1Class.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(Package1Class2.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(ClassInPackage1.class)));

        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(Package2Class.class)));
        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(Object.class)));
    }

    @Test
    public void testIncludePackageAndFQN(){
        Map<String,String> props = new HashMap<>();
        props.put(Configuration.DIGMA_AUTO_INSTRUMENT_PACKAGES_SYSTEM_PROPERTY,"com.nonexist;com.example.testpkg;com.test.package1.Package1Class");
        Configuration configuration = withProperties(props);

        ElementMatcher<? super TypeDescription> matcher =  NamespaceTypeMatchers.create(configuration);

        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(MyTestClass.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(ClassInSubPackage.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(OtherClassInSubPackage.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(Package1Class.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(StubClass.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(MyStub.class)));

        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(Package1Class2.class)));
        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(ClassInPackage1.class)));
        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(Package2Class.class)));
        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(Object.class)));
    }

    @Test
    public void testIncludeTopLevelPackages(){
        Map<String,String> props = new HashMap<>();
        props.put(Configuration.DIGMA_AUTO_INSTRUMENT_PACKAGES_SYSTEM_PROPERTY,"com.nonexist;com.example;com.test");
        Configuration configuration = withProperties(props);

        ElementMatcher<? super TypeDescription> matcher =  NamespaceTypeMatchers.create(configuration);

        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(MyTestClass.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(ClassInSubPackage.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(OtherClassInSubPackage.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(Package2Class.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(Package1Class.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(ClassInPackage1.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(Package1Class2.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(StubClass.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(MyStub.class)));

        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(Object.class)));
    }


    @Test
    public void testExcludeClassByFQNAndClassesByPackage(){
        Map<String,String> props = new HashMap<>();
        props.put(Configuration.DIGMA_AUTO_INSTRUMENT_PACKAGES_SYSTEM_PROPERTY,"com.nonexist;com.example;com.test");
        props.put(Configuration.DIGMA_AUTO_INSTRUMENT_PACKAGES_EXCLUDE_NAMES_SYSTEM_PROPERTY,"com.test.package2*;com.example.testpkg.testclasses.subpackage.ClassInSubPackage");
        Configuration configuration = withProperties(props);
        ElementMatcher<? super TypeDescription> matcher =  NamespaceTypeMatchers.create(configuration);

        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(MyTestClass.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(OtherClassInSubPackage.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(Package1Class.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(Package1Class2.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(ClassInPackage1.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(StubClass.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(MyStub.class)));

        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(ClassInSubPackage.class)));
        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(Package2Class.class)));
        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(Object.class)));
    }

    @Test
    public void testExcludeClassByFQNAndClassesByPartialPackage(){
        Map<String,String> props = new HashMap<>();
        props.put(Configuration.DIGMA_AUTO_INSTRUMENT_PACKAGES_SYSTEM_PROPERTY,"com.nonexist;com.example;com.test");
        props.put(Configuration.DIGMA_AUTO_INSTRUMENT_PACKAGES_EXCLUDE_NAMES_SYSTEM_PROPERTY,"com.test.package*;com.example.testpkg.testclasses.subpackage.ClassInSubPackage");
        Configuration configuration = withProperties(props);
        ElementMatcher<? super TypeDescription> matcher =  NamespaceTypeMatchers.create(configuration);

        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(MyTestClass.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(OtherClassInSubPackage.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(StubClass.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(MyStub.class)));

        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(ClassInSubPackage.class)));
        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(Package2Class.class)));
        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(Package1Class2.class)));
        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(Package1Class.class)));
        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(ClassInPackage1.class)));
        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(Object.class)));
    }


    @Test
    public void testExcludeClassesBySimpleNameAndClassesByPartialPackage(){
        Map<String,String> props = new HashMap<>();
        props.put(Configuration.DIGMA_AUTO_INSTRUMENT_PACKAGES_SYSTEM_PROPERTY,"com.nonexist;com.example;com.test");
        //*.Stub* is the pattern to exclude a class by simple name, the dot makes sure the simple name is matched,
        // although it can also be a part of the package name
        props.put(Configuration.DIGMA_AUTO_INSTRUMENT_PACKAGES_EXCLUDE_NAMES_SYSTEM_PROPERTY,"com.test.package1*;*ClassInSubPackage;*.Stub*");
        Configuration configuration = withProperties(props);
        ElementMatcher<? super TypeDescription> matcher =  NamespaceTypeMatchers.create(configuration);

        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(MyTestClass.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(MyStub.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(Package2Class.class)));

        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(StubClass.class)));
        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(OtherClassInSubPackage.class)));
        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(ClassInSubPackage.class)));
        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(Package1Class.class)));
        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(Package1Class2.class)));
        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(ClassInPackage1.class)));
        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(Object.class)));
    }


    @Test
    public void testExcludeAllClassesContainingString(){
        Map<String,String> props = new HashMap<>();
        props.put(Configuration.DIGMA_AUTO_INSTRUMENT_PACKAGES_SYSTEM_PROPERTY,"com.nonexist;com.example;com.test");
        //*.Stub* is the pattern to exclude a class by simple name, the dot makes sure the simple name is matched,
        // although it can also be a part of the package name
        props.put(Configuration.DIGMA_AUTO_INSTRUMENT_PACKAGES_EXCLUDE_NAMES_SYSTEM_PROPERTY,"*package1*;*Stub*");
        Configuration configuration = withProperties(props);
        ElementMatcher<? super TypeDescription> matcher =  NamespaceTypeMatchers.create(configuration);

        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(MyTestClass.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(OtherClassInSubPackage.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(ClassInSubPackage.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(Package2Class.class)));

        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(Package1Class.class)));
        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(Package1Class2.class)));
        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(ClassInPackage1.class)));
        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(MyStub.class)));
        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(StubClass.class)));
        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(Object.class)));
    }



    @Test
    public void testExcludeAllClassesEndingWithString(){
        Map<String,String> props = new HashMap<>();
        props.put(Configuration.DIGMA_AUTO_INSTRUMENT_PACKAGES_SYSTEM_PROPERTY,"com.nonexist;com.example;com.test");
        //*.Stub* is the pattern to exclude a class by simple name, the dot makes sure the simple name is matched,
        // although it can also be a part of the package name
        props.put(Configuration.DIGMA_AUTO_INSTRUMENT_PACKAGES_EXCLUDE_NAMES_SYSTEM_PROPERTY,"*Class;*Stub");
        Configuration configuration = withProperties(props);
        ElementMatcher<? super TypeDescription> matcher =  NamespaceTypeMatchers.create(configuration);

        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(OtherClassInSubPackage.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(ClassInSubPackage.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(Package1Class2.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(ClassInPackage1.class)));

        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(StubClass.class)));
        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(Package2Class.class)));
        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(MyTestClass.class)));
        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(Package1Class.class)));
        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(MyStub.class)));
        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(Object.class)));
    }


    @Test
    public void testExcludeAllClassesStartingWithString(){
        Map<String,String> props = new HashMap<>();
        props.put(Configuration.DIGMA_AUTO_INSTRUMENT_PACKAGES_SYSTEM_PROPERTY,"com.nonexist;com.example;com.test");
        //*.Stub* is the pattern to exclude a class by simple name, the dot makes sure the simple name is matched,
        // although it can also be a part of the package name
        props.put(Configuration.DIGMA_AUTO_INSTRUMENT_PACKAGES_EXCLUDE_NAMES_SYSTEM_PROPERTY,"com.test.package1.Class*");
        Configuration configuration = withProperties(props);
        ElementMatcher<? super TypeDescription> matcher =  NamespaceTypeMatchers.create(configuration);

        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(MyTestClass.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(ClassInSubPackage.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(OtherClassInSubPackage.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(StubClass.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(MyStub.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(Package2Class.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(Package1Class.class)));
        assertTrue(matcher.matches(TypeDescription.ForLoadedType.of(Package1Class2.class)));

        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(ClassInPackage1.class)));
        assertFalse(matcher.matches(TypeDescription.ForLoadedType.of(Object.class)));
    }

}
