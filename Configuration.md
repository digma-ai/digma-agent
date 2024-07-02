

### Add digma agent to an app
-javaagent:/path/to/digma-agent.jar


### Configuration

#### instrument packages

add a vm option or environment variable with a list of packages or class names to instrument seperated by semicolon.
the list may include package names or FQN of classes.<br>

for example:<br>
-Ddigma.autoinstrument.packages=com.example.testpkg.testclasses.MyTestClass;com.test.package1.Package1Class<br>
or<br>
DIGMA_AUTOINSTRUMENT_PACKAGES=com.example.testpkg;com.test.package1;com.other.pkg.MyClass<br>


#### Exclusions
It's possible to exclude classes or methods from instrumentation<br>

-Ddigma.autoinstrument.packages.exclude.names=com.test.package2*;com.example.testpkg.testclasses.subpackage.ClassInSubPackage<br>
or<br>
DIGMA_AUTOINSTRUMENT_PACKAGES_EXCLUDE_NAMES=com.test.package*;com.example.testpkg.testclasses.subpackage.ClassInSubPackage<br>

add a vm option or environment variable containing the list of exclusions seperated by semicolon<br>
this list operates on matchers for types and matchers for methods.<br>
the list may include the following patterns:<br>
simple * patterns:<br>
a value that starts with * and ends with * means nameContainsIgnoreCase<br>
a value that starts with * means nameEndsWithIgnoreCase<br>
a value that ends with * means nameStartsWithIgnoreCase<br>
for example:<br>
`*Stub*` will exclude any class or method that has the string stub in its name including the package name.<br>
it will match: myStubMethod, com.example.stub.MyClass, com.example.MyStubClass.<br>

`*Stub` will exclude all classes and methods that end with the string stub.<br>
it will match: myMethodStub, com.example.MyStub<br>

`Stub*` will exclude all class and methods that start with the string stub, this includes the package name,<br>
it will match StubMethod. stub.example.MyClass<br>
to exclude a class called com.example.StubClass use `*.Stub*`, but it will also match com.stub.test.MyClass,
so it's  better to exclude with FQN like com.example.StubClass<br>

excluding method names has kind of the same rules.<br>
to exclude a specific method of a class use the FQN of the class
with a #. for example:<br>
com.example.MyClass#myMethod<br>
to exclude all methods in a class that start with common pattern use: com.example.testpkg.testclasses.MyTestClass#myTest*<br>

There are many examples in the project's unit tests:<br>
https://github.com/digma-ai/digma-agent/blob/e6d440ee8271f9b8e585025c6fb93205448bcccc/src/test/java/org/digma/TypeMatchersTests.java<br>
https://github.com/digma-ai/digma-agent/blob/e6d440ee8271f9b8e585025c6fb93205448bcccc/src/test/java/org/digma/MethodMatchersTests.java<br>
https://github.com/digma-ai/digma-agent/blob/e6d440ee8271f9b8e585025c6fb93205448bcccc/src/test/java/org/digma/ComplexMatchersTests.java<br>



