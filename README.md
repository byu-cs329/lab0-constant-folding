# Objective

The objective of this lab is to implement [constant folding](https://en.wikipedia.org/wiki/Constant_folding) for a subset of Java and use black-box testing to test its functional correctness. The implementation will use the [org.eclipse.jdt.core.dom](https://help.eclipse.org/neon/index.jsp?topic=%2Forg.eclipse.jdt.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fjdt%2Fcore%2Fdom%2Fpackage-summary.html) to represent and manipulate Java.  The [constant folding](https://en.wikipedia.org/wiki/Constant_folding) itself should be accomplished with a specialized [ASTVisitor](https://help.eclipse.org/neon/index.jsp?topic=%2Forg.eclipse.jdt.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fjdt%2Fcore%2Fdom%2Fpackage-summary.html). The program will take two arguments as input:

  1. the Java file on which to do constant folding; and
  2. an output file to write the result.

The program should only apply to a limited subset of Java defined below. It should fold any and all constants as much as possible. It should not implement **constant propagation** which is the topic of the next lab for the course.
 
The part of the program that does the actual folding should be specified and tested via black-box testing. A significant part of the grade is dedicated to the test framework and the tests (more so than the actual implementation), so be sure to spend time accordingly. In the end, the test framework and supporting tests are more interesting than the actual constant folding in regards to the grade.

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
if (a.f()) {
  x = 10;
}
```

Which would then reduce to

```java
a.f();
x = 10;
```

That said however, the following example cannot reduct at all because if the call to ```a.y()``` returns true, then ```b.y()``` is never called. 

```java
if (a.f() || b.y() || true) {
  x = 10;
}
```

# Java Subset

The program should take as input only a subset of Java defined as follows:

  * `int` and `boolean` are the only primitive values
  * No generics, lambda-expressions, or anonymous classes
  * No interfaces, inheritance, or polymorphism
  * No reflection
  * No imports
  * No shift operators: ```<<```, ```>>```, and ```>>>```
  * No binary operators: ```^```, ```&```, and ```|```
  * No ```for```-statements
  
So what is left? More than enough to write interesting programs.

```java
package edu.byu.cs329.javasubset;

public class BinarySearchTreeSubset {

  class Node {
    
    public final int value;
    Node left;
    Node right;
    
    
    public Node(int value) {
      this.value = value;
      this.left = null;
      this.right = null;
    }

    public Node(int value, Node left, Node right) {
      this.value = value;
      this.left = left;
      this.right = right;
    }

  }
  
  protected Node root;

  private boolean search(final Node node, final int value, final boolean doAddValue) {
    if (value == node.value) {
      return true;
    }
    
    if (value < node.value) {
      if (node.left != null) {
        return search(node.left, value, doAddValue);
      }
      if (doAddValue == true) {
        node.left = new Node(value);
      }
      return false;
    }
    
    if (node.right != null) {
      return search(node.right, value, doAddValue);
    }
    if (doAddValue == true) {
      node.right = new Node(value);
    }
    return false;
  }
  
  public BinarySearchTreeSubset(int value) {
    this.root = new Node(value);
  }

  public boolean contains(int value) {
    return search(this.root, value, false);
  }
  
  public boolean add(int value) {
    return !search(root, value, true);
  }
  
  /**
   * Main method.
   * 
   * @param args  none required.
   */
  public static void main(String[] args) {
    BinarySearchTreeSubset bst = new BinarySearchTreeSubset(10);
    bst.add(11);
    System.out.print(10);
  }
}
```

# Lab Requirements

  1. Implement a test framework, with tests, using black-box functional test techniques to determine when constant folding works correctly. The tests should be organized in a way to make clear the input partitioning.
  2. Implement constant folding for the Java Subset with the [org.eclipse.jdt.core.dom](https://help.eclipse.org/neon/index.jsp?topic=%2Forg.eclipse.jdt.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fjdt%2Fcore%2Fdom%2Fpackage-summary.html)
  

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
| Consistent, readable, and descriptive naming of tests | 10 | |
| Appropriate use of `@Nested`, `@Tag`, and `@DisplayName` to organize and communicate the test methodology | 30 | | 
| Implementation of constant folding | 40 | |
| Adherence to best practices (e.g., no errors, no warnings, documented code, well grouped commits, appropriate commit messages, etc.) | 20 | |
