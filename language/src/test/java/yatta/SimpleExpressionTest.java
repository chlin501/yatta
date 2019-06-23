package yatta;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleExpressionTest {

  private Context context;

  @BeforeEach
  public void initEngine() {
    context = Context.create();
  }

  @AfterEach
  public void dispose() {
    context.close();
  }

  @Test
  public void longValueTest() {
    long ret = context.eval(YattaLanguage.ID, "5").asLong();
    assertEquals(5l, ret);
  }

  @Test
  public void byteValueTest() {
    byte ret = context.eval(YattaLanguage.ID, "5b").asByte();
    assertEquals(5, ret);
  }

  @Test
  public void floatValueTest() {
    double ret = context.eval(YattaLanguage.ID, "5.0").asDouble();
    assertEquals(5.0, ret, 0);
  }

  @Test
  public void unitValueTest() {
    assertEquals("NONE", context.eval(YattaLanguage.ID, "()").toString());
  }

  @Test
  public void stringValueTest() {
    String ret = context.eval(YattaLanguage.ID, "\"yatta-string\"").asString();
    assertEquals("yatta-string", ret);
  }

  @Test
  public void symbolValueTest() {
    String ret = context.eval(YattaLanguage.ID, ":yattaSymbol").asString();
    assertEquals("yattaSymbol", ret);
  }

  @Test
  public void tupleValueTest() {
    Value tuple = context.eval(YattaLanguage.ID, "(1, 2, 3)");
    assertEquals(3, tuple.getArraySize());

    Object[] array = tuple.as(Object[].class);
    assertEquals(1l, array[0]);
    assertEquals(2l, array[1]);
    assertEquals(3l, array[2]);
  }

  @Test
  public void emptySequenceValueTest() {
    Value sequence = context.eval(YattaLanguage.ID, "[]");
    assertEquals(0, sequence.getArraySize());
  }

  @Test
  public void oneSequenceValueTest() {
    Value sequence = context.eval(YattaLanguage.ID, "[1]");
    assertEquals(1, sequence.getArraySize());

    Object[] array = sequence.as(Object[].class);
    assertEquals(1l, array[0]);
  }

  @Test
  public void twoSequenceValueTest() {
    Value sequence = context.eval(YattaLanguage.ID, "[1, 2]");
    assertEquals(2, sequence.getArraySize());

    Object[] array = sequence.as(Object[].class);
    assertEquals(1l, array[0]);
    assertEquals(2l, array[1]);
  }

  @Test
  public void threeSequenceValueTest() {
    Value sequence = context.eval(YattaLanguage.ID, "[1, 2, 3]");
    assertEquals(3, sequence.getArraySize());

    Object[] array = sequence.as(Object[].class);
    assertEquals(1l, array[0]);
    assertEquals(2l, array[1]);
    assertEquals(3l, array[2]);
  }

  @Test
  public void zeroArgFunctionTest() {
    long ret = context.eval(YattaLanguage.ID, "\\ -> 5").execute().asLong();
    assertEquals(5l, ret);
  }

  @Test
  public void oneArgFunctionTest() {
    long ret = context.eval(YattaLanguage.ID, "\\arg -> arg").execute(6).asLong();
    assertEquals(6l, ret);
  }

  @Test
  public void twoArgFunctionFirstTest() {
    long ret = context.eval(YattaLanguage.ID, "\\argone argtwo -> argone").execute(5, 6).asLong();
    assertEquals(5l, ret);
  }

  @Test
  public void twoArgFunctionSecondTest() {
    long ret = context.eval(YattaLanguage.ID, "\\argone argtwo -> argtwo").execute(5, 6).asLong();
    assertEquals(6l, ret);
  }

  @Test
  public void moduleTest() {
    String src = "module Test exports fun as\n" +
        "fun = 6\n" +
        "other_fun = 7";
    Value modVal = context.eval(YattaLanguage.ID, src);

    assertTrue(modVal.hasMember("fun"));
    assertTrue(modVal.hasMember("other_fun"));
    assertFalse(modVal.hasMember("whatever"));
  }

  @Test
  public void letOneAliasTest() {
    long ret = context.eval(YattaLanguage.ID, "\\test -> let alias = test in alias").execute(5).asLong();
    assertEquals(5l, ret);
  }

  @Test
  public void letTwoAliasesTest() {
    Value ret = context.eval(YattaLanguage.ID, "\\test -> let \n" +
        "    alias = test\n" +
        "    aliastwo = 6\n" +
        "in\n" +
        "(alias, aliastwo)").execute(5l);
    assertEquals(2, ret.getArraySize());

    Object[] array = ret.as(Object[].class);
    assertEquals(5l, array[0]);
    assertEquals(6l, array[1]);
  }

  @Test
  public void letNotInFunctionTest() {
    long ret = context.eval(YattaLanguage.ID, "let alias = 6 in alias").asLong();
    assertEquals(6l, ret);
  }

  @Test
  public void letFunctionAliasTest() {
    long ret = context.eval(YattaLanguage.ID, "let funalias = \\arg -> arg in funalias").execute(5).asLong();
    assertEquals(5l, ret);
  }

  @Test
  public void lambdaInLetTest() {
    long ret = context.eval(YattaLanguage.ID, "let\n" +
        "alias = 6\n" +
        "funalias = \\arg -> alias\n" +
        "in funalias").execute(5l).asLong();
    assertEquals(6l, ret);
  }

  @Test
  public void invocationInLetTest() {
    long ret = context.eval(YattaLanguage.ID, "let\n" +
        "funone = \\arg -> arg\n" +
        "alias = 6\n" +
        "funalias = \\arg -> funone alias\n" +
        "in funalias 5").asLong();
    assertEquals(6l, ret);
  }

  @Test
  public void curriedLambdaInLetTest() {
    long ret = context.eval(YattaLanguage.ID, "let\n" +
        "curriedFun = \\argone argtwo -> argone\n" +
        "curriedOne = \\curriedOneArg -> curriedFun curriedOneArg 6\n" +
        "curried = \\curriedArg -> curriedOne curriedArg\n" +
        "in curried 5").asLong();
    assertEquals(5l, ret);
  }

  @Test
  public void curriedLambdaInLetSecondArgTest() {
    long ret = context.eval(YattaLanguage.ID, "let\n" +
        "curriedFun = \\argone argtwo -> argtwo\n" +
        "curriedOne = \\curriedOneArg -> curriedFun curriedOneArg 6\n" +
        "curried = \\curriedArg -> curriedOne curriedArg\n" +
        "in curried 6").asLong();
    assertEquals(6l, ret);
  }

  @Test
  public void curriedLambdaInLetOutOfScopeTest() {
    long ret = context.eval(YattaLanguage.ID, "let\n" +
        "curriedFullFun = \\-> let fullFun = \\argone argtwo argthree -> argthree in fullFun 1\n" +
        "curriedFun = curriedFullFun\n" +
        "curried = \\curriedArg -> curriedFun 3 4 curriedArg\n" +
        "in curried 6").asLong();
    assertEquals(6l, ret);
  }

  @Test
  public void curriedLambdaInLetZeroArgsTest() {
    long ret = context.eval(YattaLanguage.ID, "let\n" +
        "curriedFun = \\ -> 1\n" +
        "curried = \\curriedArg -> curriedFun\n" +
        "in curried").execute(6).asLong();
    assertEquals(1l, ret);
  }

  @Test
  public void zeroArgApplicationInLetTest() {
    long ret = context.eval(YattaLanguage.ID, "let\n" +
        "zeroArgFun = \\-> 5\n" +
        "in zeroArgFun").asLong();
    assertEquals(5l, ret);
  }

  @Test
  public void moduleCallPrivateInLetTest() {
    long ret = context.eval(YattaLanguage.ID, "let\n" +
        "testMod = module TestMod exports funone as\n" +
        "funone argone = funtwo argone\n" +
        "funtwo argone = argone\n" +
        "in testMod.funone 6").asLong();
    assertEquals(6l, ret);
  }

  @Test
  public void intSumTest() {
    long ret = context.eval(YattaLanguage.ID, "2 + 5").asLong();
    assertEquals(7l, ret);
  }

  @Test
  public void simpleDoTest() {
    long ret = context.eval(YattaLanguage.ID, "do\n" +
        "one = 1\n" +
        "println one\n" +
        "two = 2\n" +
        "one + two\n" +
        "end\n").asLong();
    assertEquals(3l, ret);
  }

  @Test
  public void simpleRaiseTest() {
    assertThrows(PolyglotException.class, () -> {
      try {
        context.eval(YattaLanguage.ID, "raise :random_error \"something happened\"\n");
      } catch (PolyglotException ex) {
        assertEquals(ex.getMessage(), "YattaError <random_error>: something happened");
        throw ex;
      }
    });
  }

  @Test
  public void simpleTryCatchTest() {
    String ret = context.eval(YattaLanguage.ID, "try\n" +
        "raise :random_error \"something happened\"\n" +
        "catch\n" +
        "(:not_this, _, _) -> \"nothing\"\n" +
        "(:random_error, message, stacktrace) -> message\n" +
        "end\n").asString();

    assertEquals("YattaError <random_error>: something happened", ret);
  }

  @Test
  public void simpleTryCatchNoErrorTest() {
    String ret = context.eval(YattaLanguage.ID, "try\n" +
        "\"no error\"\n" +
        "catch\n" +
        "(:not_this, _, _) -> \"nothing\"\n" +
        "(:random_error, message, stacktrace) -> message\n" +
        "end\n").asString();

    assertEquals("no error", ret);
  }

  @Test
  public void noMatchExceptionTest() {
    String ret = context.eval(YattaLanguage.ID, "try case 1 of\n" +
        "2 -> 0\n" +
        "end\n" +
        "catch\n" +
        "(:not_this, _, _) -> \"nothing\"\n" +
        "(:nomatch, message, stacktrace) -> message\n" +
        "end\n").asString();

    assertEquals("NoMatchException", ret);
  }

  //docs state:
  //If the {@link #HAS_SIZE} message
  //     * returns <code>true</code> implementations for {@link #READ} and {@link #WRITE} messages with
  //     * {@link Integer} parameters from range <code>0</code> to <code>GET_SIZE - 1</code> are
  //     * required.
  //which dictionary-like structures can't support by their nature
  /*@Test
  public void emptyDictValueTest() {
    Value dict = context.eval(YattaLanguage.ID, "{}");
    assertEquals(0, dict.getArraySize());
  }

  @Test
  public void dictValueTest() {
    Value dict = context.eval(YattaLanguage.ID, "{:aa = 1, :bb = 2}");
    assertEquals(2, dict.getArraySize());
  }*/
}