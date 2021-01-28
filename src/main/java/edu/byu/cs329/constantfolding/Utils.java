package edu.byu.cs329.typechecker;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {
  static final Logger log = LoggerFactory.getLogger(Utils.class);

  /**
   * Logs and throws exception if object is null.
   * 
   * @param o object to check for null
   * @param msg message for custom null-pointer exception
   */
  public static <T> void requiresNonNull(final T o, final String msg) {
    if (o != null) {
      return;
    }

    RuntimeException exception = new NullPointerException(msg);
    log.error(msg, exception);
    throw(exception);
  }

  /**
   * Log and throw custom runtime exception.
   * 
   * @param msg msg for custom eception
   */
  public static void throwRuntimeException(final String msg) {
    RuntimeException exception = new RuntimeException(msg);
    log.error(msg, exception);
    throw exception;
  }

  /**
   * Get the URI for a file from the class path.
   * 
   * @param t non-null object to get the class loader.
   * @param fileName the file to find on the class path.
   * @return URI to the file.
   */
  public static URI getUri(final Object t, final String fileName) {
    final URL url = t.getClass().getClassLoader().getResource(fileName);
    Objects.requireNonNull(url, "\'" + fileName + "\'" + " not found in classpath");
    URI uri = null;
    try {
      uri = url.toURI();
    } catch (final URISyntaxException e) {
      log.error("Failed to get URI for " + fileName);
      e.printStackTrace();
    }
    return uri;
  }

  /**
   * Get the ASTNode for program in the file.
   * 
   * @param file URI to the file.
   * @return ASTNode for the CompilationUnit in the file.
   */
  public static ASTNode getCompilationUnit(final URI file) {
    String inputFileAsString = readFile(file);
    ASTNode node = parse(inputFileAsString);
    return node;
  }

  /**
   * Get the ASTNode for the program in the file.
   * 
   * @param t reference to calling object.
   * @param name name of file.
   * @return AST node for the CompilationUnit in the file
   */
  public static ASTNode getAstNodeFor(final Object t, String name) {
    URI uri = Utils.getUri(t, name);
    requiresNonNull(uri, "failed to find " + name + "in the class path");
    ASTNode root = Utils.getCompilationUnit(uri);
    return root;
  }

  private static String readFile(final URI path) {
    try {
      return String.join("\n", Files.readAllLines(Paths.get(path)));
    } catch (IOException ioe) {
      log.error("File not readable " + ioe.getMessage());
    }
    return "";
  }

  private static ASTNode parse(final String sourceString) {
    ASTParser parser = ASTParser.newParser(AST.JLS3);
    parser.setKind(ASTParser.K_COMPILATION_UNIT);
    parser.setSource(sourceString.toCharArray());
    Map<?, ?> options = JavaCore.getOptions();
    JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
    parser.setCompilerOptions(options);
    return parser.createAST(null);
  }

  /**
   * Replaces an existing child with a new child in the AST.
   * 
   * @param oldChild the old child.
   * @param newChild the new child.
   */
  public static void replaceChildInParent(ASTNode oldChild, ASTNode newChild) {
    Objects.requireNonNull(newChild);
    StructuralPropertyDescriptor location = getLocationInParent(oldChild);
    if (location.isChildProperty()) {
      oldChild.getParent().setStructuralProperty(location, newChild);
    } else if (location.isChildListProperty()) {
      @SuppressWarnings("unchecked")
      List<ASTNode> propertyListForLocation = 
          (List<ASTNode>)(oldChild.getParent().getStructuralProperty(location));
      propertyListForLocation.set(propertyListForLocation.indexOf(oldChild), newChild);
    } 
  }

  /**
   * Removes an existing child from its parent.
   * 
   * @param child the child to remove
   */
  public static void removeChildInParent(ASTNode child) {
    StructuralPropertyDescriptor location = getLocationInParent(child);
    if (location.isChildProperty()) {
      child.getParent().setStructuralProperty(location, null);
    } else if (location.isChildListProperty()) {
      @SuppressWarnings("unchecked")
      List<ASTNode> propertyListForLocation = 
          (List<ASTNode>)(child.getParent().getStructuralProperty(location));
      propertyListForLocation.remove(child);
    }
  }

  private static StructuralPropertyDescriptor getLocationInParent(ASTNode node) {
    StructuralPropertyDescriptor location = node.getLocationInParent();
    Objects.requireNonNull(location); 
    if (location.isChildProperty() || location.isChildListProperty()) {
      return location;
    }
    String msg = new String("Location \'" + location.toString() + "\' is not supported");
    RuntimeException exception = new UnsupportedOperationException(msg);
    log.error(msg, exception);
    throw exception;
  }

  public static String buildName(String className, String name) {
    return className + "." + name;
  }

  public static String getName(TypeDeclaration classDeclaration) {
    return Utils.getName(classDeclaration.getName());
  }

  public static String getName(FieldDeclaration field) {
    return Utils.getName(field.fragments());
  }

  public static String getName(MethodDeclaration method) {
    return Utils.getName(method.getName());
  }

  public static String getName(VariableDeclaration declaration) {
    return Utils.getName(declaration.getName());
  }

  public static String getName(VariableDeclarationStatement declaration) {
    return Utils.getName(declaration.fragments());
  }

  public static String getName(SimpleName name) {
    return name.getIdentifier();
  }

  private static String getName(Object fragments) {
    VariableDeclaration declaration = Utils.getFragment(fragments);
    return Utils.getName(declaration.getName());
  }

  public static String getType(FieldDeclaration field) {
    return Utils.getType(field.getType());
  }

  public static String getType(MethodDeclaration method) {
    return Utils.getType(method.getReturnType2());
  }
  
  public static String getType(SingleVariableDeclaration declaration) {
    return Utils.getType(declaration.getType());
  }

  public static String getType(VariableDeclarationStatement declaration) {
    return Utils.getType(declaration.getType());
  }

  private static String getType(Type type) {
    String typeName = null;
    if (type.isPrimitiveType()) {
      typeName = getType((PrimitiveType)type);
    } else if (type.isSimpleType()) {
      typeName = getType((SimpleType)type);
    } else {
      Utils.throwRuntimeException(type.toString() + " is not a simple or primitive type");
    }
    return typeName;
  }

  private static String getType(SimpleType type) {
    Name name = type.getName();
    String typeName = null;
    if (name instanceof SimpleName) {
      typeName = Utils.getName((SimpleName)name);
    } else {
      Utils.throwRuntimeException(name.getFullyQualifiedName() 
          + " is not a SimpleName");
    }
    return typeName;
  }

  private static String getType(PrimitiveType type) {
    String typeName = null;
    if (type.getPrimitiveTypeCode() == PrimitiveType.INT) {
      typeName = TypeCheckTypes.INT;
    } else if (type.getPrimitiveTypeCode() == PrimitiveType.BOOLEAN) {
      typeName = TypeCheckTypes.BOOL;
    } else if (type.getPrimitiveTypeCode() == PrimitiveType.VOID) {
      typeName = TypeCheckTypes.VOID;
    } else {
      // Not tested
      Utils.throwRuntimeException("primitive type " 
          + type.toString() 
          + " is not an int, boolean, or void");
    }
    return typeName;
  }

  public static Expression getInitializer(VariableDeclarationStatement declarationStatement) {
    VariableDeclaration declaration = Utils.getFragment(declarationStatement.fragments());
    return declaration.getInitializer();   
  }

  public static SimpleName getSimpleName(VariableDeclarationStatement declarationStatement) {
    VariableDeclaration declaration = Utils.getFragment(declarationStatement.fragments());
    return declaration.getName();   
  }
  
  private static VariableDeclaration getFragment(Object fragments) {
    @SuppressWarnings("unchecked")
    List<VariableDeclaration> fragmentList = (List<VariableDeclaration>) fragments;
    if (fragmentList.size() > 1) {
      Utils.throwRuntimeException("only one VariableDeclaration is allowed in fragments");
    }

    return fragmentList.get(0);
  }
}
