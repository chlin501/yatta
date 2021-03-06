package yatta.ast.binary;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import yatta.YattaException;
import yatta.runtime.Dict;
import yatta.runtime.Set;
import yatta.runtime.async.Promise;

@NodeInfo(shortName = "^")
public abstract class BitwiseXorNode extends BinaryOpNode {
  @Specialization
  public long longs(long left, long right) {
    return left ^ right;
  }

  @Specialization
  public Set sets(Set left, Set right) {
    return left.symmetricDifference(right);
  }

  @Specialization
  public Dict dicts(Dict left, Dict right) {
    return left.symmetricDifference(right);
  }

  protected Promise promise(Object left, Object right) {
    Promise all = Promise.all(new Object[]{left, right}, this);
    return all.map(args -> {
      Object[] argValues = (Object[]) args;

      if (!argValues[0].getClass().equals(argValues[1].getClass())) {
        return YattaException.typeError(this, argValues);
      }

      if (argValues[0] instanceof Long && argValues[1] instanceof Long) {
        return (long) argValues[0] ^ (long) argValues[1];
      } else if (argValues[0] instanceof Set && argValues[1] instanceof Set) {
        return sets((Set) argValues[0], (Set) argValues[1]);
      } else if (argValues[0] instanceof Dict && argValues[1] instanceof Dict) {
        return dicts((Dict) argValues[0], (Dict) argValues[1]);
      } else {
        return YattaException.typeError(this, argValues);
      }
    }, this);
  }

  @Specialization
  public Promise leftPromise(Promise left, Object right) {
    return promise(left, right);
  }

  @Specialization
  public Promise rightPromise(Object left, Promise right) {
    return promise(left, right);
  }
}
