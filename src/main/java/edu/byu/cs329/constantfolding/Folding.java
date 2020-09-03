package edu.byu.cs329.constantfolding;

import org.eclipse.jdt.core.dom.ASTNode;

public interface Folding {
  public boolean fold(final ASTNode root);
}
