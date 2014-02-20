/*
 * Copyright (c) 2013 The Ontario Institute for Cancer Research. All rights reserved.                             
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
package org.icgc.dcc.submission.validation.key.data;

import static com.google.common.base.Charsets.US_ASCII;

import java.nio.ByteBuffer;
import java.util.List;

import lombok.Value;
import lombok.val;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

/**
 * Represents the values for a given key (a key may be composite).
 */
// TODO: efficient equals/hashCode (maybe lombok is ok for the latter)
@Value
public class KVKey implements Comparable<KVKey> {

  public static final KVKey KEY_NOT_APPLICABLE = null;

  // TODO: Move to another class? Clean up?
  public static final Interner<ByteBuffer> interner = Interners.newWeakInterner();

  /**
   * Values for the key.
   */
  private final ByteBuffer[] values;

  /**
   * Size of the key for optimization purposes (may be 1 if the key is not a composite one).
   */
  private final short size;

  public static KVKey from(List<String> row, List<Integer> indices) {
    short size = (short) indices.size();
    val values = new ByteBuffer[size];
    for (int index = 0; index < size; index++) {
      val text = row.get(indices.get(index));
      ByteBuffer bytes = ByteBuffer.wrap(text.getBytes(US_ASCII));
      val canonicalized = interner.intern(bytes);

      values[index] = canonicalized;
    }
    // TODO: checks
    return new KVKey(values, size);
  }

  /**
   * Somewhat optimized...
   */
  @Override
  public int compareTo(KVKey keys) {
    // TODO: efficient guava way?
    if (size == 1) {
      val compared0 = this.values[0].compareTo(keys.values[0]);
      if (compared0 != 0) {
        return compared0;
      }
    } else if (size == 2) {
      val compared0 = this.values[0].compareTo(keys.values[0]);
      if (compared0 != 0) {
        return compared0;
      }
      val compared1 = this.values[1].compareTo(keys.values[1]);
      if (compared1 != 0) {
        return compared1;
      }
    } else if (size == 3) {
      val compared0 = this.values[0].compareTo(keys.values[0]);
      if (compared0 != 0) {
        return compared0;
      }
      val compared1 = this.values[1].compareTo(keys.values[1]);
      if (compared1 != 0) {
        return compared1;
      }
      val compared2 = this.values[2].compareTo(keys.values[2]);
      if (compared2 != 0) {
        return compared2;
      }
    }

    // TODO: general case!!!!
    return 0;
  }

  public String[] getValues() {
    String[] result = new String[values.length];
    for (int i = 0; i < values.length; i++) {
      result[i] = new String(values[i].array());
    }

    return result;
  }

}