/*
 * Copyright (c) 2016 The Ontario Institute for Cancer Research. All rights reserved.                             
 *                                                                                                               
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with                                  
 * this program. If not, see <http://www.gnu.org/licenses/>.                                                     
 *                                                                                                               
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY                           
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES                          
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT                           
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,                                
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED                          
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;                               
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER                              
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN                         
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.icgc.dcc.submission.validation.cascading;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import cascading.CascadingTestCase;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;
import cascading.tuple.TupleListCollector;
import org.junit.Assert;
import lombok.val;

/**
 * Completely obsolete now... FIXME
 */
public class StructuralCheckFunctionTest {

  private final Fields LINE_FIELDS = new Fields("line");

  private final List<String> DICTIONARY_FIELD_NAMES = Arrays.asList("col1", "col2", "col3", "col4");

  private final TupleEntry[] VALID_DATA_TUPLES = new TupleEntry[] {//
      new TupleEntry(LINE_FIELDS, new Tuple("v.1.1\tv.1.3\tv.1.2\tv.1.4")),//
      new TupleEntry(LINE_FIELDS, new Tuple("v.2.1\tv.2.3\tv.2.2\tv.2.4")),//
      };

  private final TupleEntry[] MISSING_DATA_TUPLES = new TupleEntry[] {//
      new TupleEntry(LINE_FIELDS, new Tuple("v.1.1\tv.1.4")),//
      new TupleEntry(LINE_FIELDS, new Tuple("v.2.1\tv.2.4")),//
      };

  private final TupleEntry[] EXTRA_DATA_TUPLES = new TupleEntry[] {//
      new TupleEntry(LINE_FIELDS, new Tuple("v.1.1\tv.1.3\tv.1.2\tv.1.5\tv.1.4\tv.1.6")),//
      new TupleEntry(LINE_FIELDS, new Tuple("v.2.1\tv.2.3\tv.2.2\tv.2.5\tv.2.4\tv.2.6")),//
      };

  private final Fields RESULT_FIELDS = new Fields("col1", "col2", "col3", "col4"); // intentionally in order (although
                                                                                   // it does not have to be)

  @Test
  public void test_parseValues() {
    val line = "1\t\t\t";
    val values = StructuralCheckFunction.parseValues(line);

    assertThat(values).hasSize(4);
    assertThat(values.get(0)).isEqualTo("1");
    assertThat(values.get(1)).isEmpty();
    assertThat(values.get(2)).isEmpty();
    assertThat(values.get(3)).isEmpty();
  }

  @Test
  @Ignore
  public void test_operate_valid() {
    StructuralCheckFunction function = new StructuralCheckFunction(DICTIONARY_FIELD_NAMES);
    TupleEntry[] tuples = VALID_DATA_TUPLES;

    Iterator<TupleEntry> iterator = callFunction(function, tuples, RESULT_FIELDS);
    checkTupleEntry(iterator, new TupleEntry(RESULT_FIELDS, new Tuple("v.1.1", "v.1.2", "v.1.3", "v.1.4")));
    checkTupleEntry(iterator, new TupleEntry(RESULT_FIELDS, new Tuple("v.2.1", "v.2.2", "v.2.3", "v.2.4")));
    Assert.assertFalse(iterator.hasNext());
  }

  @Test
  @Ignore
  public void test_operate_missingColumns() {
    StructuralCheckFunction function = new StructuralCheckFunction(DICTIONARY_FIELD_NAMES);
    TupleEntry[] tuples = MISSING_DATA_TUPLES;

    Iterator<TupleEntry> iterator = callFunction(function, tuples, RESULT_FIELDS);
    checkTupleEntry(iterator, new TupleEntry(RESULT_FIELDS, new Tuple("v.1.1", null, null, "v.1.4")));
    checkTupleEntry(iterator, new TupleEntry(RESULT_FIELDS, new Tuple("v.2.1", null, null, "v.2.4")));
    Assert.assertFalse(iterator.hasNext());
  }

  @Test
  @Ignore
  public void test_operate_extraColumns() {
    StructuralCheckFunction function = new StructuralCheckFunction(DICTIONARY_FIELD_NAMES);
    TupleEntry[] tuples = EXTRA_DATA_TUPLES;

    Iterator<TupleEntry> iterator = callFunction(function, tuples, RESULT_FIELDS);
    checkTupleEntry(iterator, new TupleEntry(RESULT_FIELDS, new Tuple("v.1.1", "v.1.2", "v.1.3", "v.1.5")));
    checkTupleEntry(iterator, new TupleEntry(RESULT_FIELDS, new Tuple("v.2.1", "v.2.2", "v.2.3", "v.2.5")));
    Assert.assertFalse(iterator.hasNext());
  }

  @Test
  @Ignore
  public void test_operate_missingHeaders() {
    StructuralCheckFunction function = new StructuralCheckFunction(DICTIONARY_FIELD_NAMES);
    TupleEntry[] tuples = VALID_DATA_TUPLES;

    Iterator<TupleEntry> iterator = callFunction(function, tuples, RESULT_FIELDS);
    checkTupleEntry(iterator, new TupleEntry(RESULT_FIELDS, new Tuple("v.1.1", "v.1.2", "v.1.4", "v.1.3")));
    checkTupleEntry(iterator, new TupleEntry(RESULT_FIELDS, new Tuple("v.2.1", "v.2.2", "v.2.4", "v.2.3")));
    Assert.assertFalse(iterator.hasNext());
  }

  @Test
  @Ignore
  public void test_operate_extraHeaders() {
    StructuralCheckFunction function = new StructuralCheckFunction(DICTIONARY_FIELD_NAMES);
    TupleEntry[] tuples = VALID_DATA_TUPLES;

    Iterator<TupleEntry> iterator = callFunction(function, tuples, RESULT_FIELDS);
    checkTupleEntry(iterator, new TupleEntry(RESULT_FIELDS, new Tuple("v.1.1", "v.1.2", "v.1.3", "v.1.4")));
    checkTupleEntry(iterator, new TupleEntry(RESULT_FIELDS, new Tuple("v.2.1", "v.2.2", "v.2.3", "v.2.4")));
    Assert.assertFalse(iterator.hasNext());
  }

  @Test
  @Ignore
  public void test_operate_missingData() {
    StructuralCheckFunction function = new StructuralCheckFunction(DICTIONARY_FIELD_NAMES);
    TupleEntry[] tuples = MISSING_DATA_TUPLES;

    Iterator<TupleEntry> iterator = callFunction(function, tuples, RESULT_FIELDS);
    checkTupleEntry(iterator, new TupleEntry(RESULT_FIELDS, new Tuple("v.1.1", null, "v.1.4", null)));
    checkTupleEntry(iterator, new TupleEntry(RESULT_FIELDS, new Tuple("v.2.1", null, "v.2.4", null)));
    Assert.assertFalse(iterator.hasNext());
  }

  @Test
  @Ignore
  public void test_operate_extraData() {
    StructuralCheckFunction function = new StructuralCheckFunction(DICTIONARY_FIELD_NAMES);
    TupleEntry[] tuples = EXTRA_DATA_TUPLES;

    Iterator<TupleEntry> iterator = callFunction(function, tuples, RESULT_FIELDS);
    checkTupleEntry(iterator, new TupleEntry(RESULT_FIELDS, new Tuple("v.1.1", "v.1.2", "v.1.3", "v.1.5")));
    checkTupleEntry(iterator, new TupleEntry(RESULT_FIELDS, new Tuple("v.2.1", "v.2.2", "v.2.3", "v.2.5")));
    Assert.assertFalse(iterator.hasNext());
  }

  @Test
  @Ignore
  public void test_operate_chaos() { // missing and extra headers, missing and extra data!
    StructuralCheckFunction function = new StructuralCheckFunction(DICTIONARY_FIELD_NAMES);
    TupleEntry[] tuples = new TupleEntry[] {//
        new TupleEntry(LINE_FIELDS, new Tuple("v.1.1\tv.1.4")),//
        new TupleEntry(LINE_FIELDS, new Tuple("v.2.1\tv.2.3\tv.2.2\tv.2.5\tv.2.4\tv.2.6")),//
        };

    Iterator<TupleEntry> iterator = callFunction(function, tuples, RESULT_FIELDS);
    checkTupleEntry(iterator, new TupleEntry(RESULT_FIELDS, new Tuple("v.1.1", null, null, "v.1.4")));
    checkTupleEntry(iterator, new TupleEntry(RESULT_FIELDS, new Tuple("v.2.1", "v.2.2", "v.2.5", "v.2.3")));
    Assert.assertFalse(iterator.hasNext());
  }

  private Iterator<TupleEntry> callFunction(StructuralCheckFunction function, TupleEntry[] tuples, Fields resultFields) {
    TupleListCollector c = CascadingTestCase.invokeFunction(function, tuples, resultFields);
    Assert.assertEquals(c.size(), tuples.length);
    Iterator<TupleEntry> iterator = c.entryIterator();
    return iterator;
  }

  private void checkTupleEntry(Iterator<TupleEntry> iterator, TupleEntry expectedTupleEntry) {
    Assert.assertTrue(iterator.hasNext());
    TupleEntry next = iterator.next();
    Assert.assertEquals(4, next.size());
    Assert.assertEquals(//
        expectedTupleEntry.toString(),//
        next.toString());
  }
}
