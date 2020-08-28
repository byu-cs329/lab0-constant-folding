package edu.byu.cs329.constantfolding;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Tests for folding ParenthesizedExpression types")
public class ParenthesizedExpressionFoldingTests {
  ParenthesizedExpressionFolding folderUnderTest = null;

  @BeforeEach
  void beforeEach() {
    folderUnderTest = new ParenthesizedExpressionFolding();
  }

  @Test
  @DisplayName("Should throw RuntimeException when root is null")
  void should_ThrowRuntimeException_when_RootIsNull() {
    assertThrows(RuntimeException.class, () -> {
      folderUnderTest.fold(null);
    });
  }

  @Test
  @DisplayName("Should throw RuntimeException when root is not a CompilationUnit and has no parent")
  void should_ThrowRuntimeException_when_RootIsNotACompilationUnitAndHasNoParent() {
    assertThrows(RuntimeException.class, () -> {
      URI uri = Utils.getUri(this, "");
      ASTNode compilationUnit = Utils.getCompilationUnit(uri);
      ASTNode root = compilationUnit.getAST().newNullLiteral();
      folderUnderTest.fold(root);
    });
  }

  @Test
  @DisplayName("Should only fold parenthesized literals when given multiple types")
  void should_OnlyFoldParenthesizedLiterals_when_GivenMultipleTypes() {
    String rootName = "parenthesizedLiterals/should_OnlyFoldParenthesizedLiterals_when_GivenMultipleTypes-root.java";
    URI rootUri = Utils.getUri(this, rootName);
    ASTNode root = Utils.getCompilationUnit(rootUri);
    assertTrue(folderUnderTest.fold(root));

    String expectedName = "parenthesizedLiterals/should_OnlyFoldParenthesizedLiterals_when_GivenMultipleTypes.java";
    URI expectedUri = Utils.getUri(this, expectedName);
    ASTNode expected = Utils.getCompilationUnit(expectedUri);
    assertTrue(expected.subtreeMatch(new ASTMatcher(), root));
  }
}