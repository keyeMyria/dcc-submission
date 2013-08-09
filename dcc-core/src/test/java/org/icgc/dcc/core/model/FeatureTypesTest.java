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

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static org.fest.assertions.api.Assertions.assertThat;

import org.icgc.dcc.core.model.FeatureTypes.FeatureType;
import org.junit.Test;

public class FeatureTypesTest {

  @Test
  public void test_FeatureType() {
    assertThat(FeatureType.from("ssm")).isEqualTo(FeatureType.SSM_TYPE);
    assertThat(FeatureType.from("exp")).isEqualTo(FeatureType.EXP_TYPE);
    assertThat(FeatureType.from("pexp")).isEqualTo(FeatureType.PEXP_TYPE);

    assertThat(FeatureType.complement(
        newLinkedHashSet(newArrayList(
            FeatureType.SSM_TYPE,
            FeatureType.CNSM_TYPE,
            FeatureType.SGV_TYPE,
            FeatureType.METH_TYPE,
            FeatureType.EXP_TYPE,
            FeatureType.PEXP_TYPE))))
        .isEqualTo(
            newLinkedHashSet(newArrayList(
                FeatureType.STSM_TYPE,
                FeatureType.CNGV_TYPE,
                FeatureType.STGV_TYPE,
                FeatureType.MIRNA_TYPE,
                FeatureType.JCN_TYPE)));
  }

}