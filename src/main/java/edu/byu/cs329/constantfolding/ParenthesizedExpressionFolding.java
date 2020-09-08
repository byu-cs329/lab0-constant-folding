package edu.byu.cs329.constantfolding;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Replaces parenthesized literals with the literal.
 * 
 * @author Eric Mercer
 */
public class ParenthesizedExpressionFolding implements Folding {
  static final Logger log = LoggerFactory.getLogger(ParenthesizedExpressionFolding.class);
  
  class Visitor extends ASTVisitor {
    public boolean didFold = false;

    private boolean isLiteralExpression(ASTNode exp) {
      return (exp instanceof BooleanLiteral) 
        || (exp instanceof CharacterLiteral)
        || (exp instanceof NullLiteral)
        || (exp instanceof StringLiteral)
        || (exp instanceof TypeLiteral)
        || (exp instanceof NumberLiteral);
    }
    
    @Override
    public void endVisit(ParenthesizedExpression node) {
      ASTNode exp = node.getExpression();
      if (!isLiteralExpression(exp)) {
        return;
      }
      AST ast = node.getAST();
      ASTNode newExp = ASTNode.copySubtree(ast, exp);
      Utils.replaceChildInParent(node, newExp);
      didFold = true;
    }
  }

  public ParenthesizedExpressionFolding() {
  } 
  
  /**
   * Replaces parenthesized literals in the tree with the literals.
   * 
   * <p>Visits the root and any reachable nodes from the root to replace
   * any ParenthesizedExpression reachable node containing a literal
   * with the literal itself.
   * 
   * <p>top(root) := all nodes reachable from root such that each node 
   *                 is an outermost parenthesized expression that ends
   *                 in a literal
   * 
   * <p>topParents(root) := all nodes such that each one is the parent
   *                        of some node in top(root)
   * 
   * <p>between(n) := all nodes reachable from root such that each 
   *                    node is a parenthesized expression that is 
   *                    nested in some top most node
   * 
   * <p>bottom(n) := if n \in top(root), then the literal from the 
   *                 innermost expression of n and otherwise undefined.
   * 
   * @requires root != null
   * @requires (root instanceof CompilationUnit) \/ parent(root) != null
   * 
   * @ensures fold(root) == !(old(top(root)) == \emptyset)
   * @ensures nodes(root) = 
   *     old(nodes(root)) \ (old(top(root)) \cup old(between(root)))
   * @ensures \forall n \in old(top(root)),
   *        parent(old(bottom(n))) = old(parent(n)))
   *     /\ children(old(parent(n))) = 
   *          (old(children(parent(n))) \setminus {n}) \cup {old(bottom(n))}
   * @ensures \forall n \in old(topParents(root)), parent(n) = old(parent(n))
   * @ensures \forall n \in (old(nodes(root)) \setminus 
   *          (old(top(root)) \cup old(between(root)) \cup old(topParents(root))),
   *        parents(n) = old(parents(n))
   *     /\ children(n) = old(children(n))
   * @ensures top(root) = \emptyset 
   *     /\ between(root) = \emptyset 
   *     /\ topParents = \emptyset
   *  
   * @param root the root of the tree to traverse.
   * @return true if parenthesized literals were replaced in the rooted tree
   */
  public boolean fold(final ASTNode root) {
    checkRequires(root);
    Visitor visitor = new Visitor();
    root.accept(visitor);
    return visitor.didFold;
  }

  private void checkRequires(final ASTNode root) {
    Utils.requiresNonNull(root, "Null root passed to ParenthesizedExpressionFolding.fold");
 
    if (!(root instanceof CompilationUnit) && root.getParent() == null) {
      Utils.throwRuntimeException(
          "Non-CompilationUnit root with no parent passed to ParenthesizedExpressionFolding.fold"
      );
    }
  }
}