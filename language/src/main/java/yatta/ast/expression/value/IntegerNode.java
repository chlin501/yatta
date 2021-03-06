package yatta.ast.expression.value;

import yatta.ast.ExpressionNode;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.UnexpectedResultException;

import java.util.Objects;

@NodeInfo
public final class IntegerNode extends LiteralValueNode {
  public final long value;

  public IntegerNode(long value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    IntegerNode integerNode = (IntegerNode) o;
    return Objects.equals(value, integerNode.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return "IntegerNode{" +
        "value=" + value +
        '}';
  }

  @Override
  public Object executeGeneric(VirtualFrame frame) {
    return value;
  }

  @Override
  public long executeLong(VirtualFrame frame) throws UnexpectedResultException {
    return value;
  }
}
