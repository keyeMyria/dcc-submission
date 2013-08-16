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
package org.icgc.dcc.core.model;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.icgc.dcc.core.model.ClinicalType.CLINICAL_CORE_TYPE;
import static org.icgc.dcc.core.model.FeatureTypes.FeatureType.SSM_TYPE;
import static org.icgc.dcc.core.model.SubmissionDataType.SubmissionDataTypes.fromTypeName;

import java.util.HashSet;

import org.icgc.dcc.core.model.FeatureTypes.FeatureType;
import org.icgc.dcc.core.model.SubmissionDataType.SubmissionDataTypes;
import org.junit.Test;

public class SubmissionDataTypeTest {

  @Test
  public void test_SubmissionDataTypes_valid() {
    assertThat(SubmissionDataTypes.fromTypeName("ssm")).isEqualTo(SSM_TYPE);
    assertThat(SubmissionDataTypes.fromTypeName("donor")).isEqualTo(CLINICAL_CORE_TYPE);

    assertThat(SubmissionDataTypes.values().size()).
        isEqualTo(13); // 11 feature types + 1 clinical type + 1 optional (clinical) type
    assertThat(SubmissionDataTypes.values().size()).isEqualTo( // Check no duplicates
        new HashSet<SubmissionDataType>(SubmissionDataTypes.values()).size());

    assertThat(SubmissionDataTypes.isMandatoryType(ClinicalType.CLINICAL_CORE_TYPE)).isTrue();
    assertThat(SubmissionDataTypes.isMandatoryType(FeatureType.SSM_TYPE)).isFalse();
  }

  @Test(expected = IllegalStateException.class)
  public void test_SubmissionDataTypes_invalid() {
    fromTypeName("dummy");
  }

}