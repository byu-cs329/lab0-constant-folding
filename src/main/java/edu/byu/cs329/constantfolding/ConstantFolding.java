package edu.byu.cs329.constantfolding;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements constant folding.
 * 
 * @author James Wasson
 * @author Eric Mercer
 *
 */
public class ConstantFolding {

  static final Logger log = LoggerFactory.getLogger(ConstantFolding.class);

  /**
   * Performs constant folding.
   *
   * @param compilationUnit ASTNode for the compliation unit. 
   * @return The root ASTNode for the constant folded version of the input.
   */
  public static ASTNode fold(ASTNode compilationUnit) {
    boolean isChanged = true;
    List<Folding> foldingList = new ArrayList<Folding>();
    foldingList.add(new ParenthesizedExpressionFolding());
    // TODO: add other ways to fold
  
    while (isChanged == true) {
      for (Folding folding : foldingList) {
        isChanged = isChanged || folding.fold(compilationUnit);
      }
      isChanged = false;
      
      log.warn("TODO: add other ways to fold");
    } 

    return compilationUnit;
  }

  /**
   * Performs constant folding an a Java file.
   * 
   * @param args The args[0] is the file to fold and the args[1] is where to write the
   *             output.
   */
  public static void main(String[] args) {
    if (args.length != 2) {
      log.error("Missing Java input file or output file or both on command line");
      System.out.println("usage: java ConstantFolding <input file> <output file>");
      System.exit(1);
    }

    File inputFile = new File(args[0]);
    ASTNode compilationUnit = Utils.getCompilationUnit(inputFile.toURI());
    compilationUnit = fold(compilationUnit);

    try {
      PrintWriter writer = new PrintWriter(args[1], "UTF-8");
      writer.print(compilationUnit.toString());
      writer.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
