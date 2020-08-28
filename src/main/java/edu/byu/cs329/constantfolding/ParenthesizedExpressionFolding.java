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
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Replaces parenthesized literals with the literal.
 * 
 * @author Eric Mercer
 */
public class ParenthesizedExpressionFolding {
  static final Logger log = LoggerFactory.getLogger(ParenthesizedExpressionFolding.class);
  
  class Visitor extends ASTVisitor {

    private ASTNode newExp = null;
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
    public boolean visit(ParenthesizedExpression node) {
      node.getExpression().accept(this);
      ASTNode exp = node.getExpression();
      if (isLiteralExpression(exp)) {
        AST ast = node.getAST();
        newExp = ASTNode.copySubtree(ast, exp);
      } else {
        newExp = null;
      }
      return false;
    }

    @Override
    public void endVisit(ParenthesizedExpression node) {
      if (newExp == null) {
        return;
      }

      StructuralPropertyDescriptor location = node.getLocationInParent();
      Utils.requiresNonNull(location, "The location cannot be null in Visitor.endVisit with a new expression to set.");
      Utils.setNewChildInParent(node, newExp);
      newExp = null;
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
   * <p>topParents(n) := all nodes such that each done in the parent
   *                     of some node in top(root)
   * 
   * <p>inbetween(n) := all nodes reachable from root such that each 
   *                    node is a parenthesized expression that is 
   *                    nested in some top most node
   * 
   * <p>bottom(n) := if n \in top(root), then the literal from the 
   *                 innermost expression of n and otherwise undefined.
   * 
   * @requires root != null
   * @requires (root instanceof CompilationUnit) \/ parent(root) != null
   * 
   * @ensures nodes(root) = 
   *   old(nodes(root)) \ old(top(root)) \cup old(between(root))
   * @ensures \forall n \in old(top(root)),
   *      parent(old(bottom(n))) = old(parent(n)))
   *   /\ children(old(parent(n))) = 
   *        (old(children(parent(n))) \setminus {n}) \cup {old(bottom(n))}
   * @ensures \forall n \in (old(nodes(root)) \setminus 
   *    (old(top(root)) \cup old(between(root)) \cup old(topParents(root))),
   *      parents(n) = old(parents(n))
   *   /\ children(n) = old(children(n))
   * @ensures top(root) = \emptyset 
   *   /\ between(root) = \emptyset 
   *   /\ topParents = \emptyset
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