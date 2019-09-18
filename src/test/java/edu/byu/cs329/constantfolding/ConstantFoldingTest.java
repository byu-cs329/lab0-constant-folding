package edu.byu.cs329.constantfolding;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.core.dom.ASTNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.byu.cs329.constantfolding.ConstantFolding;

public class ConstantFoldingTest {

  static final Logger log = LoggerFactory.getLogger(ConstantFoldingTest.class);
  
  private URI getURI(final String fileName) {
    URL url = ClassLoader.getSystemResource(fileName);
    Objects.requireNonNull(url, "\'" + fileName + "\'" + " not found in classpath");
    URI uri = null;
    try {
      uri = url.toURI();
    } catch (URISyntaxException e) {
      log.error("Failed to get URI for" + fileName);
      e.printStackTrace();
    }
    return uri;    
  }
  
  @ParameterizedTest(name = "Should return only literal {0} when given file {1}")
  @CsvSource({"7, BinaryAdd.java", "14, OneNestedBinaryAdd.java", "11, VariableWithOneNestedBinaryAdd.java"})
  void Should_ReturnOneLiteral_When_GivenFileWithOneLiteralExpression(String expected, String fileName) {
    URI uri = getURI(fileName);
    Objects.requireNonNull(uri, "Failed to get URI for " + fileName);

    // TODO: add folding test code here
  }
  
}
