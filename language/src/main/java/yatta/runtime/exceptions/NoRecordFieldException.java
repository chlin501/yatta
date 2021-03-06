package yatta.runtime.exceptions;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.nodes.Node;
import yatta.YattaException;
import yatta.runtime.annotations.ExceptionSymbol;

@ExceptionSymbol("norecordfield")
public final class NoRecordFieldException extends YattaException {
  @CompilerDirectives.TruffleBoundary
  public NoRecordFieldException(String recordType, String fieldName, Node location) {
    super("NoRecordFieldException: " + recordType + '(' + fieldName + ')' , location);
  }
}
