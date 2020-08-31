package edu.byu.cs329.constantfolding;

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
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Utils {
  static final Logger log = LoggerFactory.getLogger(Utils.class);

  /**
   * Read the file at path and return its contents as a String.
   * 
   * @param path The location of the file to be read.
   * @return The contents of the file as a String.
   */
  private static String readFile(final URI path) {
    try {
      return String.join("\n", Files.readAllLines(Paths.get(path)));
    } catch (IOException ioe) {
      log.error("File not readable " + ioe.getMessage());
    }
    return "";
  }

  /**
   * Parse the given source.
   * 
   * @param sourceString The contents of some set of Java files.
   * @return An ASTNode representing the entire program.
   */
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
   * Replaces an existing child with a new child in the AST.
   * 
   * @param oldChild the old child.
   * @param newChild the new child.
   */
  public static void setNewChildInParent(ASTNode oldChild, ASTNode newChild) {
    StructuralPropertyDescriptor location = oldChild.getLocationInParent();
    Objects.requireNonNull(location);
    if (location.isChildProperty()) {
      oldChild.getParent().setStructuralProperty(location, newChild);
    } else if (location.isChildListProperty()) {
      @SuppressWarnings("unchecked")
      List<ASTNode> propertyListForLocation = 
          (List<ASTNode>)(oldChild.getParent().getStructuralProperty(location));
      propertyListForLocation.set(propertyListForLocation.indexOf(oldChild), newChild);
    } else {
      String msg = new String("Location \'" + location.toString() + "\' is not supported");
      RuntimeException exception = new UnsupportedOperationException(msg);
      log.error(msg, exception);
      throw exception;
    }
  }
}