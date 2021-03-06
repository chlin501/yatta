package yatta;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleException;
import com.oracle.truffle.api.TruffleStackTrace;
import com.oracle.truffle.api.TruffleStackTraceElement;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.source.SourceSection;
import yatta.runtime.*;

public class YattaException extends RuntimeException implements TruffleException {
  private static final long serialVersionUID = -6799734410727348507L;

  private final Node location;

  @TruffleBoundary
  public YattaException(String message, Node location) {
    super(message);
    this.location = location;
  }

  @TruffleBoundary
  public YattaException(String message, Throwable cause, Node location) {
    super(message, cause);
    this.location = location;
  }

  @TruffleBoundary
  public YattaException(Seq message, Node location) {
    this(message.asJavaString(location), location);
  }

  @TruffleBoundary
  public YattaException(Seq message, Throwable cause, Node location) {
    this(message.asJavaString(location), cause, location);
  }

  @TruffleBoundary
  public YattaException(Throwable cause, Node location) {
    super(cause);
    this.location = location;
  }

  @SuppressWarnings("sync-override")
  @Override
  public final Throwable fillInStackTrace() {
    return this;
  }

  public Node getLocation() {
    return location;
  }

  @TruffleBoundary
  public Tuple asTuple() {
    return new Tuple(Context.getCurrent().lookupExceptionSymbol(this.getClass()), getMessage(), stacktraceToSequence(this));
  }

  @TruffleBoundary
  public static String prettyPrint(String message, org.graalvm.polyglot.SourceSection sourceLocation) {
    StringBuilder sb = new StringBuilder();
    sb.append(message);
    sb.append(" at ");
    sb.append(sourceLocation.getSource().getName());
    sb.append(":\n");
    sb.append(sourceLocation.getCharacters());
    return sb.toString();
  }

  /**
   * Provides a user-readable message for run-time type errors. Yatta is strongly typed, i.e., there
   * are no automatic type conversions of values.
   */
  @TruffleBoundary
  public static YattaException typeError(Node operation, Object... values) {
    StringBuilder result = new StringBuilder();
    result.append("Type error");

    if (operation != null) {
      SourceSection ss = operation.getEncapsulatingSourceSection();
      if (ss != null && ss.isAvailable()) {
        result.append(" at ").append(ss.getSource().getName()).append(" line ").append(ss.getStartLine()).append(" col ").append(ss.getStartColumn());
      }
    }

    result.append(": operation");
    if (operation != null) {
      NodeInfo nodeInfo = Context.lookupNodeInfo(operation.getClass());
      if (nodeInfo != null) {
        result.append(" \"").append(nodeInfo.shortName()).append("\"");
      }
    }

    result.append(" not defined for");

    String sep = " ";
    for (int i = 0; i < values.length; i++) {
      Object value = values[i];
      result.append(sep);
      sep = ", ";
      if (value == null || InteropLibrary.getFactory().getUncached().isNull(value)) {
        result.append(YattaLanguage.toString(value));
      } else {
        result.append(YattaLanguage.getMetaObject(value));
        result.append(" ");
        if (InteropLibrary.getFactory().getUncached().isString(value)) {
          result.append("\"");
        }
        result.append(YattaLanguage.toString(value));
        if (InteropLibrary.getFactory().getUncached().isString(value)) {
          result.append("\"");
        }
      }
    }
    return new YattaException(result.toString(), operation);
  }

  public static Seq stacktraceToSequence(Throwable throwable) {
    Seq stackTraceSequence = Seq.EMPTY;

    for (TruffleStackTraceElement stackTraceElement : TruffleStackTrace.getStackTrace(throwable)) {
      Node location = stackTraceElement.getLocation();
      if (location != null && location.getSourceSection() != null) {
        stackTraceSequence = stackTraceSequence.insertFirst(new Tuple(
            Seq.fromCharSequence(stackTraceElement.getTarget().getRootNode().getSourceSection().getSource().getName()),
            Seq.fromCharSequence(stackTraceElement.getTarget().getRootNode().getQualifiedName()),
            location.getSourceSection().getStartLine(),
            location.getSourceSection().getStartColumn()
        ));
      } else {
        stackTraceSequence = stackTraceSequence.insertFirst(new Tuple (
            Seq.fromCharSequence(stackTraceElement.getTarget().getRootNode().getSourceSection().getSource().getName()),
            Seq.fromCharSequence(stackTraceElement.getTarget().getRootNode().getQualifiedName()),
            Unit.INSTANCE,
            Unit.INSTANCE
        ));
      }
    }

    return stackTraceSequence;
  }
}
