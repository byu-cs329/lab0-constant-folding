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

This level of reduction (short-circuiting with three operands) is not required for this lab. It can be implemented, with appropriate tests, for extra credit if so desired.

## Some further notes on folding

Do not fold outside of `ParenthesizedExpressions` unless that expression contains as single `NumberLiteral` (e.g., `(10)`). If such is the case, replace the `ParenthesizedExpression` with a `NumberLiteral` (e.g. `(10)` becomes `10`).

Replace if-statements or other statements that are removed because of folding with an instance of the  `EmptyStatement`. Consider the below example.

```java
if (false) {
  x = 10;
}
```

The above code reduces to `;` which is the `EmptyStatement`. Using the `EmptyStatement` in the DOM avoids having to deal with empty blocks or empty expressions in if-statements as in the next example.

```java
if (x)
  if (false);
```

The above example reduces to

```java
if (x);
```

Removing empty statements is not required---completely uncompensated and not required.

# Java Subset

This course is only going to consider a very narrow [subset of Java](https://bitbucket.org/byucs329/byu-cs-329-lecture-notes/src/master/java-subset/java-subset.md) as considering all of Java is way beyond the scope and intent of this course (e.g., it would be a nightmare). The [subset of Java](https://bitbucket.org/byucs329/byu-cs-329-lecture-notes/src/master/java-subset/java-subset.md) effectively reduces to single-file programs with all the interesting language features (e.g., generics, lambdas, anonymous classes, etc.) removed. **As a general rule**, if something seems unusually hard then it probably is excluded from the [subset of Java](https://bitbucket.org/byucs329/byu-cs-329-lecture-notes/src/master/java-subset/java-subset.md) or should be excuded, so stop and ask before spending a lot of time on it.

# Where to Apply Constant Folding

Constant folding **only applied** to the following types of `ASTNode` expressions and statements:

  * `ParenthesizedExpression` that contain only a literal
  * Logical `PrefixExpression` for `!`
  * Numeric `InfixExpression` for `+` if and only if all the operands are of type `NumberLiteral` including the extended operands
  * Binary relational `InfixExpression` for `<` and `==`
  * Binary logical `InfixExpression` for `||` and `&&`
  * `IfStatement`
  * `WhileStatement`

This set of expressions and statemenst are more narrow than what is allowed in the Java subset. That is OK. Folding is only applied to the above program features.  Also, folding should be applied iteratively until no furter reduction is possible.

# Lab Requirements

  0. Write a specification for each type of supported folding. The input to each supported folding is an instance of an  `ASTNode` (usually a `CompilationUnit`) in the [org.eclipse.jdt.core.dom](https://help.eclipse.org/neon/index.jsp?topic=%2Forg.eclipse.jdt.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fjdt%2Fcore%2Fdom%2Fpackage-summary.html) that may (or may not) be changed in some way by the folding. Assume (no need to check) that the input conforms to the Java subset for this lab. 
  1. Implement a black-box functional test framework for each type folding that can be applied in this lab. The tests should be organized in a way to make clear the black-box test methodology and its relation to the specification. Do not include redundant tests or tests not part of the black-box method as these will be penalized.
  2. Implement constant folding for each supported feature with a vistor. As such, each folding feature should have its own unique visitor implementation. For example, there should be a first visitor to fold `ParenthesizedExpressions`, a second visitor to fold `PrefixExpressions`, a third visitor to fold numeric `InfixExpressions` with only literals, etc. The visitors are applied iteratively until no further reductions take place.
  3. Add each visitor to `ConstantFolding.fold` as it is completed.

## Suggested order of attack:

Approach the lab in small steps starting with the easiest type starting with `PrefixExpression` as it is similar to `ParenthesizedExpressions` that is given as part of the lab distribution. 

   0. Read and understand everything related to `ParenthesizedExpression`
   1. Write the specification for `PrefixExpression`
   2. Create the tests from the specification
   3. Implement the actual visitor. 

Repeat for the other ways to fold. As a note, (3) can be delayed until all the specification and tests are written. What is important is that the **implementation is written after the specification and tests.**

# POM Notes

The `mvn test` uses the Surefire plugin to generat console reports and additional reports in `./target/surefire-reports`. The console report extension is configure to use the `@DisplayName` for the tests and generally works well except in the case of tests in `@Nested`, tests in `@ParameterizedTest`, or `@DynamicTest`. For these, the console report extension is less than ideal as it does not use the `@DisplayName` all the time a groups `@ParameterisedTest` and `@DynamicTest` into a single line report.

The `./target/surefire-reports/TEST-<fully qualified classname>.xml` file is the detailed report of all the tests in the class that uses the correct `@DisplayName`. The file is very useful for isolating failed parameterized or dynamic tests. The regular text files in the directory only show what Maven shows. That said, many IDEs present a tree view of the tests with additional information for `@Nested`, `@ParameterizedTest`, `@DynamicTest`, `@RepeatTest`, etc. This tree view can be generated with the JUnit `ConsoleLauncher`. 

The POM in the project is setup to run the [JUnit Platform Console Standalone](https://mvnrepository.com/artifact/org.junit.platform/junit-platform-console-standalone) on the `mvn exec:java` goal in the build phase. The POM sets the arguments to scan for tests, `--scan-classpath`, with `./target/test-classes` being added to the class path. The equivalent command line (and the defauld defined in the POM):

```
mvn exec:java -Dexec.mainClass=org.junit.platform.console.ConsoleLauncher -Dexec.args="--class-path=./target/test-classes --scan-classpath"
```

The above is what is run with just the command `mvn exec:java`.

The `ConsoleLauncher` is able to run specific tests and classes, so it is possible to change the `--scan-path` argument, either in the POM file or by typing in the above on the command line. [Section 4.3.1](https://junit.org/junit5/docs/current/user-guide/#running-tests-console-launcher) of the JUnit 5 users lists all the options.

# What to turn in?

Create a pull request when the lab is done. Submit to Canvas the URL of the repository.

# Rubric

| Item | Point Value | Your Score |
| ------- | ----------- | ---------- |
| Oracle(s) strong enough to determine when output is valid | 50 | |
| Tests that divide the input space in a reasoned, systematic, and somewhat complete way | 50 |
| Appropriate use of `@Nested`, `@Tag`, and `@DisplayName` to organize and communicate the test methodology | 30 | | 
| Implementation of constant folding | 60 | |
| Adherence to best practices (e.g., good names, no errors, no warnings, documented code, well grouped commits, appropriate commit messages, etc.) | 10 | |
| Extra credit short circuit reduction, at least two tests (+20) | 0 | |

Breakdown of Implementation **(60 points)**

  * **(10 points)** Numeric `InfixExpressions` (e.g., `+`, `-`,`*`, and `/`) when all operands are literals (including extended operands)
  * **(5 points)** Binary (i.e., no extended operands---exactly two operands) relational `InfixExpressions` (e.g. `<`, `>`, `<=`, `>=`, `==`, and `!=`)
  * **(10 points)** Binary logical `InfixExpressions` (e.g., `||`, `&&`)
  * **(5 points)** Logical `PrefixExpressions` (e.g., `!`)
  * **(10 points)** `IfStatement` (reduction and short-circuiting with two operands)
  * **(10 points)** `WhileStatement` (reduction and short-circuiting with two operands)
  * **(10 points)** `DoStatement` (reduction and short-circuiting with two operands)
  
# Notes

For the test framework, here are some things to consider:

  * The valid input space is vast. When creating the test for valid input, how can you choose an interesting input? 
  * What would a boundary value analysis look like? 
  * Watch carefully what imports are added by the IDE in the testing code to be sure it is importing from the Jupiter API as behavior changes in the IDE if it grabs from the wrong JUnit API and annotations will not work as expected. 
  * Be sure the logger imports use the `slf4j` interface.
