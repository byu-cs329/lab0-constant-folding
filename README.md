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

Be aware that [short circuit evaluation](https://en.wikipedia.org/wiki/Short-circuit_evaluation) comes into play for constant folding. For example ```a.f() && false``` cannot be reducted to ```false``` because it is not know if the call to ```a.f()``` side effects on the state of the class, so it's call must be preseved. That said, ```a.y() && false && b.y()``` can be reduced to ```a.y() && false``` since the call to ```b.y()``` will never take place.  As another example, consider ```a.y() || b.y() || true || c.y()```. Here the call to ```c.y()``` will never take place so the expression reduces to ```a.y() || b.y() || true```.

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

But this reduction is harder than necessary for this lab, so it is not required (though we will give 20 points extra credit for its successful implementation along with at least two unit tests: one must be the above example, the other an example of your choice. Specifically mention in your pull-request that you completed the extra credit).

## Some further notes on folding

Do not fold outside of ```ParenthesizedExpressions``` unless it is something like  ```(10)``` in which case it is perfectly reasoanable to replace ```(10)```, a ```ParenthesizedExpression``` with a ```NumberLiteral```, with just ```10``` the ```NumberLiteral```

Replace if-statements or other statements that are removed because of folding with an instance of the  ```EmptyStatement```.

```java
if (false) {
  x = 10;
}
```

reduces to ```;``` which is the ```EmptyStatemest```. In this way it is no longer necessary to deal with empty blocks or empty expressions in if-statements.

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

The static type checker should support a [subset of Java](https://bitbucket.org/byucs329/byu-cs-329-lecture-notes/src/master/java-subset/java-subset.md). If something seems unusually hard then be sure it is in the [language subset](https://bitbucket.org/byucs329/byu-cs-329-lecture-notes/src/master/java-subset/java-subset.md).

To be clear, constant folding only needs to be supported in the following:

  * Numeric `InfixExpressions` (e.g., `+`, `-`,`*`, and `/`) when all operands are literals (including extended operands)
  * Binary (i.e., no extended operands---exactly two operands) relational `InfixExpressions` (e.g. `<`, `>`, `<=`, `>=`, `==`, and `!=`)
  * Binary logical `InfixExpressions` (e.g., `||`, `&&`)
  * Logical `PrefixExpressions` (e.g., `!`)
  * `IfStatement`
  * `WhileStatement`
  * `DoStatement`

Note the restriction on extended operands to numeric expressions and only in the case when all operands are literals.

# Lab Requirements

  1. Implement a test framework, with tests, using black-box functional test techniques to determine when constant folding works correctly. The tests should be organized in a way to make clear the input partitioning.
  2. Implement constant folding for the Java Subset with the [org.eclipse.jdt.core.dom](https://help.eclipse.org/neon/index.jsp?topic=%2Forg.eclipse.jdt.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fjdt%2Fcore%2Fdom%2Fpackage-summary.html)

If it highly reccomended to implement multiple visitors. For example, implemente one visitor to fold `InfixExpressions` and a second visitor te fold statements after expressions are reduced.
  
## Suggested order of attack:

Write the test framework and tests first. Then use test driven development to implement constant folding. If you write a good set of tests, then those tests will set the agenda for the implementation and signal when the implementation is done.

Organize the tests in a way that communicates the reasoning behind the partitioning. Use the `@Nested`, `@Tag`, and `@DisplayName` to convey clearly how and why the tests are grouped.

For the test framework, here are some things to consider:

  * How will you determine that the output is correct (i.e., that constant folding happened correctly)? In the [DOM-Visitor](https://bitbucket.org/byucs329/byu-cs-329-lecture-notes/src/master/DOM-Visitor/) lecture, the `NumberLiteral` expressions were recorded after the folding and verified to meet a specific criteria. That is only a starting point and not a solution. Is there one method that will work for all the ways to fold?
  * How can you partition the input space to systematically cover ways that constant folding may appear and affect code?  In the [DOM-Visitor](https://bitbucket.org/byucs329/byu-cs-329-lecture-notes/src/master/DOM-Visitor/) lecture, it starts with a single expression with only two `NumberLiteral` operands. It then adds a single binary expression, with one operand being a binary expression with only `NumberLiteral` operands. Consider such a starting point. Also consider how constant folding might change code or remove code. These too should be part of the input partitioning exercise.
  * What would a boundary value analysis look like?
  * How can you isolate things to only test one feature at time? For example, it may not be wise to write a test to see if **dead code** is removed correctly but have that test first rely on the implementation for short-circuit evaluation of boolean expressions working as expected.

Do not implement more than is required by the test, and do not be afraid to create a lot of tests. Let the test define what needs to be implemented. And create a test for everything that needs to be implemented according to the input partitions. Add code to throw `UnsupportedException` for things not yet implemented as you code along. For example, if additions is the only thing implemented so far, then expressions with other operators should throw on exception. In this way it is easier to keep track of what is yet to be implemented.

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

Breakdown of Implementation **(60 points)**

  * **(10 points)** Numeric `InfixExpressions` (e.g., `+`, `-`,`*`, and `/`) when all operands are literals (including extended operands)
  * **(5 points)** Binary (i.e., no extended operands---exactly two operands) relational `InfixExpressions` (e.g. `<`, `>`, `<=`, `>=`, `==`, and `!=`)
  * **(10 points)** Binary logical `InfixExpressions` (e.g., `||`, `&&`)
  * **(5 points)** Logical `PrefixExpressions` (e.g., `!`)
  * **(10 points)** `IfStatement`
  * **(10 points)** `WhileStatement`
  * **(10 points)** `DoStatement`
  