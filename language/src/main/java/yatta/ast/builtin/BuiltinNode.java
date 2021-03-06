package yatta.ast.builtin;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.UnsupportedSpecializationException;
import com.oracle.truffle.api.frame.VirtualFrame;
import yatta.YattaException;
import yatta.ast.ExpressionNode;
import yatta.runtime.Context;
import yatta.runtime.DependencyUtils;

/**
 * Base class for all builtin functions. It contains the Truffle DSL annotation {@link NodeChild}
 * that defines the function arguments.<br>
 * Builtin functions need access to the {@link Context}. Instead of defining a Java field manually
 * and setting it in a constructor, we use the Truffle DSL annotation {@link NodeField} that
 * generates the field and constructor automatically.
 */
@NodeChild(value = "arguments", type = ExpressionNode[].class)
@GenerateNodeFactory
public abstract class BuiltinNode extends ExpressionNode {
  @Override
  public final Object executeGeneric(VirtualFrame frame) {
    try {
      return execute(frame);
    } catch (UnsupportedSpecializationException e) {
      throw YattaException.typeError(e.getNode(), e.getSuppliedValues());
    }
  }

  protected abstract Object execute(VirtualFrame frame);

  @Override
  protected String[] requiredIdentifiers() {
    return DependencyUtils.catenateRequiredIdentifiers((ExpressionNode) super.getChildren());
  }
}
