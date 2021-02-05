# Objective

The objective of this lab is to implement [constant folding](https://en.wikipedia.org/wiki/Constant_folding) for a subset of Java and use black-box testing to test its functional correctness. The implementation will use the [org.eclipse.jdt.core.dom](https://help.eclipse.org/neon/index.jsp?topic=%2Forg.eclipse.jdt.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fjdt%2Fcore%2Fdom%2Fpackage-summary.html) to represent and manipulate Java.  The [constant folding](https://en.wikipedia.org/wiki/Constant_folding) itself should be accomplished with a specialized [ASTVisitor](https://help.eclipse.org/neon/index.jsp?topic=%2Forg.eclipse.jdt.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fjdt%2Fcore%2Fdom%2Fpackage-summary.html). The program will take two arguments as input:

  1. the Java file on which to do constant folding; and
  2. an output file to write the result.

The program should only apply to a limited subset of Java defined below. It should fold any and all constants as much as possible. It should not implement **constant propagation** which is the topic of the next lab for the course.
 
The part of the program that does the actual folding should be specified and tested via black-box testing. A significant part of the grade is dedicated to the test framework and the tests (more so than the actual implementation), so be sure to spend time accordingly. In the end, the test framework and supporting tests are more interesting than the actual constant folding in regards to the grade.

# Reading

The [DOM-Visitor lecture](https://bitbucket.org/byucs329/byu-cs-329-lecture-notes/src/master/DOM-Visitor/) is a must read before starting this lab. You will also need the [DOMViewer](https://github.com/byu-cs329/DOMViewer.git) installed and working to view the AST for any given input file. Alternatively, there is an [ASTView plugin](https://www.eclipse.org/jdt/ui/astview/) for Eclipse available on the Eclipse Market Place that works really well. There is something similar for IntelliJ too.

# Constant Folding

[Constant folding](https://en.wikipedia.org/wiki/Constant_folding) is the process where constant expressions are reduced by the compiler before generating code. 
 
Examples:

  * `x = 3 + 7` becomes `x = 10`
  * `x = 3 + (7 + 4)` becomes `x = 14`
  * `x = y + (7 + 4)` becomes `x = y + 11`

Not that constant folding does not include replacing variables that reduce to constants. 

```java
x = 3 + 7;
y = x;
```

Constant folding for the above gives:

```java
x = 10;
y = x;
```

Constant folding may also apply to Boolean values.

```java
if (3 > 4) {
  x = 5;
} else {
  x = 10;
}
```

Should reduce to

```java
if (false) {
  x = 5;
} else {
  x = 10;
}
```

Which should reduce to

```java
x = 10;
```

In the above example, the constant folding removed **dead code** that was not reachable on any input.

For this lab, you don't need to remove the braces -- it is more difficult that is necessary. So the above should look like:

```java
{ x = 10;
}
```

# Advandced Folding (Optional)

Be aware that [short circuit evaluation](https://en.wikipedia.org/wiki/Short-circuit_evaluation) comes into play for constant folding. For example ```a.f() && false``` cannot be reducted to ```false``` because it is not known if the call to ```a.f()``` side effects on the state of the class, so it's call must be preseved. That said, ```a.y() && false && b.y()``` can be reduced to ```a.y() && false``` since the call to ```b.y()``` will never take place.  As another example, consider ```a.y() || b.y() || true || c.y()```. Here the call to ```c.y()``` will never take place so the expression reduces to ```a.y() || b.y() || true```.

It is also possible to remove literals that have no effect on the expression. For example ```a.y() && true``` reduces to ```a.y()```. Similarly, ```a.y() || false || b.y()``` reduces to ```a.y() || b.y()```.  Always be aware of [short circuit evaluation](https://en.wikipedia.org/wiki/Short-circuit_evaluation) in constant folding since method calls can side-effect on the state of the class, those calls must be presered as shown in the examples above. 

Here is another example of short circuit evaluation that requires some careful thought.

```java
if (a.f() || true) {
  x = 10;
}
```

This code could reduce to

```java
a.f();
if (true) {
  x = 10;
}
```

Which would then reduce to

```java
a.f();
x = 10;
```

That said however, the following example cannot reduce in the same way because if the call to ```a.y()``` returns true, then ```b.y()``` is never called. 

```java
if (a.f() || b.y() || true) {
  x = 10;
}
```

It could reduce to

```java
if (a.f() || b.y()) {
   ;
}
if (true) {
  x = 10;
}
```

Which would then reduce to

```java
if (!a.f()) {
  b.y();
}
x = 10;
```

If you implement short-circuiting of two parameters with appropriate tests, notify the instructor to receive extra credit.

# Java Subset

This course is only going to consider a very narrow [subset of Java](https://bitbucket.org/byucs329/byu-cs-329-lecture-notes/src/master/java-subset/java-subset.md) as considering all of Java is way beyond the scope and intent of this course (e.g., it would be a nightmare). The [subset of Java](https://bitbucket.org/byucs329/byu-cs-329-lecture-notes/src/master/java-subset/java-subset.md) effectively reduces to single-file programs with all the interesting language features (e.g., generics, lambdas, anonymous classes, etc.) removed. **As a general rule**, if something seems unusually hard or has an unusually large number of cases to deal with, then it probably is excluded from the [subset of Java](https://bitbucket.org/byucs329/byu-cs-329-lecture-notes/src/master/java-subset/java-subset.md) or should be excuded, so stop and ask before spending a lot of time on it.

# Required Folding

Constant folding is **only required** for the following types of `ASTNode` expressions and statements:

  * `ParenthesizedExpression` that contain only a literal of any type (given as part of the base code)
  * Logical not `PrefixExpression`, `!`, when applied to `BooleanLiteral`
  * Numeric `InfixExpression` for `+` if and only if all the operands are of type `NumberLiteral` including the extended operands
  * Binary relational `InfixExpression`, `<`, if and only if both operands are of type `NumberLiteral`
  * `IfStatement` if and only if the predicate is a `BooleanLiteral`
 
Assume that any `NumberLiteral` instance is of type `int` always. This set of expressions and statements are much more narrow than what is allowed in the Java subset. That is OK. Folding is only applied to the above program features.  Also, folding should be applied iteratively until no furter reduction is possible.

# Lab Requirements

For each required application of constant folding:

  0. Write a specification
  1. Write black-box tests that coincide with the specification
  2. Implement the folding in its own class that implements `Folding`
  3. Add the new folding technique to `ConstantFolding.fold`

After each required application of constant folding is implemented, then write some system tests for `ConstantFolding.fold` that check the iterative application of all the ways to folding to reduce an input to an expected value.

As a note, there should not be much more than 25 to 30 tests total (e.g., around 4 to 5 tests for each type of folding and one system test  for `ConstantFolding.fold`) as black-box test is very functional. Excess tests will degrade your score for the lab. 

## Suggested order of attack:

Approach the lab in small steps starting with the easiest type starting with `PrefixExpression` as it is similar to `ParenthesizedExpressions` that is given as part of the lab distribution. 

   0. Read and understand everything related to `ParenthesizedExpression`
   1. Write the specification for `PrefixExpression`
   2. Create the tests from the specification
   3. Implement the actual visitor. 

Repeat for the other ways to fold. It is **important** that the **implementation is created after the specification and tests.** Specification. Then tests. And finally the implementation.

# POM Notes

The `mvn test` uses the Surefire plugin to generat console reports and additional reports in `./target/surefire-reports`. The console report extension is configured to use the `@DisplayName` for the tests and generally works well except in the case of tests in `@Nested`, tests in `@ParameterizedTest`, or `@DynamicTest`. For these, the console report extension is less than ideal as it does not use the `@DisplayName` all the time and groups `@ParameterisedTest` and `@DynamicTest` into a single line report.

The `./target/surefire-reports/TEST-<fully qualified classname>.xml` file is the detailed report of all the tests in the class that uses the correct `@DisplayName`. The file is very useful for isolating failed parameterized or dynamic tests. The regular text files in the directory only show what Maven shows. That said, many IDEs present a tree view of the tests with additional information for `@Nested`, `@ParameterizedTest`, `@DynamicTest`, `@RepeatTest`, etc. This tree view can be generated with the JUnit `ConsoleLauncher`. 

The POM in the project is setup to run the [JUnit Platform Console Standalone](https://mvnrepository.com/artifact/org.junit.platform/junit-platform-console-standalone) on the `mvn exec:java` goal in the build phase. The POM sets the arguments to scan for tests, `--scan-classpath`, with `./target/test-classes` being added to the class path. The equivalent command line (and the default defined in the POM):

```
mvn exec:java -Dexec.mainClass=org.junit.platform.console.ConsoleLauncher -Dexec.args="--class-path=./target/test-classes --scan-classpath"
```

The above is what is run with just the command `mvn exec:java`.

The `ConsoleLauncher` is able to run specific tests and classes, so it is possible to change the `--scan-path` argument, either in the POM file or by typing in the above on the command line. [Section 4.3.1](https://junit.org/junit5/docs/current/user-guide/#running-tests-console-launcher) of the JUnit 5 users lists all the options.

# What to turn in?

Create a pull request when the lab is done. Submit to Canvas the URL of the repository.

# Rubric

  * **(40 points)** for each required constant folding not including `ParenthesizedExpressions`
  * **(20 points)** for the system test framework for `ConstantFolding.fold`
  * **(20 points)** for best practice (e.g., good names, no errors, no warnings, documented code, well grouped commits, appropriate commit messages, etc.)
  
Breakdown of **(40 points)** for each required folding technique 

  * **(15 points)** for the specification
  * **(15 points)** for the tests
  * **(10 points)** for the implementation
  
# Notes

  * What would a boundary value analysis look like? 
  * Watch carefully what imports are added by the IDE in the testing code to be sure it is importing from the Jupiter API as behavior changes in the IDE if it grabs from the wrong JUnit API and annotations will not work as expected. 
  * Be sure the logger imports use the `slf4j` interface.
