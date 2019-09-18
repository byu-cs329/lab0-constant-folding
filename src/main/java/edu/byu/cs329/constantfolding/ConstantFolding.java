package edu.byu.cs329.constantfolding;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimplePropertyDescriptor;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO.
 * 
 * @author James Wasson
 * @author Eric Mercer
 *
 */
public class ConstantFolding {

  static final Logger log = LoggerFactory.getLogger(ConstantFolding.class);

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
      log.error(ioe.getMessage());
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
   * Performs constant folding.
   * 
   * @param file URI to the input Java file
   * @return the root ASTNode for the constant folded version of the input
   */
  public static ASTNode fold(URI file) {

    String inputFileAsString = readFile(file);
    ASTNode node = parse(inputFileAsString);

    // TODO: complete constant folding

    return node;
  }

  /**
   * Performs constant folding an a Java file.
   * 
   * @param args args[0] is the file to fold and args[1] is where to write the
   *             output
   */
  public static void main(String[] args) {
    if (args.length != 2) {
      log.error("Missing Java input file or output file on command line");
      System.out.println("usage: java DomViewer <java file to parse> <html file to write>");
      System.exit(1);
    }

    File inputFile = new File(args[0]);
    ASTNode folded = ConstantFolding.fold(inputFile.toURI());

    try {
      PrintWriter writer = new PrintWriter(args[1], "UTF-8");
      writer.print(folded.toString());
      writer.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
