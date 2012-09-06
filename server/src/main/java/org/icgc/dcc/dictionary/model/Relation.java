/**
 * Copyright 2012(c) The Ontario Institute for Cancer Research. All rights reserved.
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
package org.icgc.dcc.dictionary.model;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.icgc.dcc.dictionary.visitor.DictionaryElement;
import org.icgc.dcc.dictionary.visitor.DictionaryVisitor;

import com.google.code.morphia.annotations.Embedded;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@Embedded
public class Relation implements DictionaryElement {

  private final List<String> fields;

  private final Cardinality cardinality;

  private final String other;

  private final List<String> otherFields;

  private final List<Integer> optionals;

  public Relation() {
    fields = new ArrayList<String>();
    otherFields = new ArrayList<String>();
    cardinality = null;
    other = null;
    optionals = new ArrayList<Integer>();
  }

  public Relation(Iterable<String> leftFields, String right, Iterable<String> rightFields, Cardinality lhsCardinality) {
    this(leftFields, right, rightFields, lhsCardinality, ImmutableList.<Integer> of());
  }

  public Relation(Iterable<String> leftFields, String right, Iterable<String> rightFields, Cardinality lhsCardinality,
      Iterable<Integer> optionals) {
    this.fields = Lists.newArrayList(leftFields);
    this.cardinality = lhsCardinality;
    this.other = right;
    this.otherFields = Lists.newArrayList(rightFields);
    this.optionals = Lists.newArrayList(optionals);

    checkArgument(this.fields != null);
    checkArgument(this.other != null);
    checkArgument(this.otherFields != null);
    checkArgument(this.optionals != null);

    checkArgument(this.fields.isEmpty() == false, this.fields.size());
    checkArgument(this.fields.size() == this.otherFields.size());
    checkArgument(this.fields.size() > this.optionals.size(), this.fields.size() + ", " + this.optionals.size());

    if(this.isFieldsValid() == false) {
      throw new DataModelException(String.format("fields in relation \"%s\" are not valid", this.describe()));
    }

    if(this.isOptionalValid() == false) {
      throw new DataModelException(String.format("optionals (%s) in relation \"%s\" are not valid", this.optionals,
          this.describe()));
    }

    if(this.optionals.isEmpty() == false && lhsCardinality == Cardinality.ONE_OR_MORE) { // see comment DCC-289: only
                                                                                         // allowing one or the other
      throw new DataModelException(String.format(
          "invalid relation \"%s\" specified: cannot specify both optional fields (%s) and a cardinality of %s",
          describe(), this.optionals, Cardinality.ONE_OR_MORE));
    }
  }

  public String describe() {
    return String.format("?.%s (cardinality: %s) --> %s.%s [optionals: %s]", fields, cardinality, other, otherFields,
        optionals);
  }

  @Override
  public void accept(DictionaryVisitor dictionaryVisitor) {
    dictionaryVisitor.visit(this);
  }

  public List<String> getFields() {
    return fields;
  }

  public Cardinality getCardinality() {
    return cardinality;
  }

  public String getOther() {
    return other;
  }

  public List<String> getOtherFields() {
    return otherFields;
  }

  public List<Integer> getOptionals() {
    return optionals;
  }

  private boolean isOptionalValid() {
    // optionals should be strictly less than fields
    if(this.optionals.size() >= this.fields.size() || this.optionals.size() >= this.otherFields.size()) {
      return false;
    }

    Set<Integer> set = Sets.newHashSet();
    for(Integer optional : this.optionals) {
      // check for valid indices
      if(optional.intValue() < 0 || optional.intValue() >= this.fields.size()) {
        return false;
      }
      // check for repetition
      if(!set.add(optional)) {
        return false;
      }
    }

    return true;
  }

  private boolean isFieldsValid() {

    Set<String> set = Sets.newHashSet();
    for(String field : this.fields) {
      // check for repetition
      if(!set.add(field)) {
        return false;
      }
    }
    set.clear();
    for(String field : this.otherFields) {
      // check for repetition
      if(!set.add(field)) {
        return false;
      }
    }
    return true;
  }
}
