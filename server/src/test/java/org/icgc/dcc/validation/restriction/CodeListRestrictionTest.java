package org.icgc.dcc.validation.restriction;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.icgc.dcc.dictionary.model.CodeList;
import org.icgc.dcc.dictionary.model.Term;
import org.icgc.dcc.validation.cascading.TupleState;
import org.icgc.dcc.validation.restriction.CodeListRestriction.InCodeListFunction;
import org.junit.Test;

import cascading.CascadingTestCase;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;
import cascading.tuple.TupleListCollector;

public class CodeListRestrictionTest extends CascadingTestCase {

  private static final String FIELDNAME = "code";

  private static final String CODELISTNAME = "TestList";

  private static final String CODE1 = "0";

  private static final String VALUE1 = "X";

  private CodeList codeList;

  public void setup_CodeListRestriction() {
    this.codeList = new CodeList(CODELISTNAME);
    Term term1 = new Term(CODE1, VALUE1, "");
    codeList.addTerm(term1);
  }

  @Test
  public void test_CodeListRestriction() {
    setup_CodeListRestriction();
    CodeListRestriction restriction = new CodeListRestriction(FIELDNAME, codeList);

    assertEquals(String.format("codelist[%s:%s]", FIELDNAME, CODELISTNAME), restriction.describe());
  }

  public void setup_InCodeListFunction() {

  }

  @Test
  public void test_InCodeListFunction_codeInCodeList() {

    TupleState state = testRig_InCodeListFunction(CODE1);
    assertTrue(state.isValid());
  }

  @Test
  public void test_InCodeListFunction_codeNotInCodeList() {
    TupleState state = testRig_InCodeListFunction("1");

    assertFalse(state.isValid());
  }

  @Test
  public void test_InCodeListFunction_numericCodeInCodeList() {

    TupleState state = testRig_InCodeListFunction(new Integer(0));
    assertTrue(state.isValid());
  }

  @Test
  public void test_InCodeListFunction_numericCodeNotInCodeList() {
    TupleState state = testRig_InCodeListFunction(new Integer(1));

    assertFalse(state.isValid());
  }

  @Test
  public void test_InCodeListFunction_valueInCodeList() {
    TupleState state = testRig_InCodeListFunction(VALUE1);

    assertTrue(state.isValid());
  }

  @Test
  public void test_InCodeListFunction_valueNotInCodeList() {
    TupleState state = testRig_InCodeListFunction("Y");

    assertFalse(state.isValid());
  }

  @Test
  public void test_InCodeListFunction_emptyValue() {
    TupleState state = testRig_InCodeListFunction("");

    assertFalse(state.isValid());
  }

  @Test
  public void test_InCodeListFunction_nullValue() {
    TupleState state = testRig_InCodeListFunction(null);

    assertTrue(state.isValid());
  }

  private TupleState testRig_InCodeListFunction(Object tupleValue) {
    Set<String> codes = new HashSet<String>();
    codes.add(CODE1);
    Set<String> values = new HashSet<String>();
    values.add(VALUE1);

    InCodeListFunction function = new InCodeListFunction(codes, values);

    Fields incoming = new Fields(FIELDNAME, "_state");

    TupleEntry[] tuples = new TupleEntry[] { new TupleEntry(incoming, new Tuple(tupleValue, new TupleState())) };

    TupleListCollector c = CascadingTestCase.invokeFunction(function, tuples, incoming);

    Iterator<Tuple> iterator = c.iterator();

    Tuple t = iterator.next();

    assertEquals(tupleValue, t.getObject(0));
    TupleState state = (TupleState) t.getObject(1);
    return state;
  }
}
