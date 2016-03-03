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
package org.icgc.dcc.submission.loader.meta;

import static org.assertj.core.api.Assertions.assertThat;
import lombok.val;

import org.junit.Before;
import org.junit.Test;

public class ReleaseResolverIntegrationTest {

  private static final String URL = "";
  private static final String USERNAME = "";
  private static final String PASSWORD = "";

  ReleaseResolver releaseResolver;

  @Before
  public void setUp() {
    this.releaseResolver = new ReleaseResolver(URL, USERNAME, PASSWORD);
  }

  @Test
  public void getReleasesTest() throws Exception {
    val releases = releaseResolver.getReleases();
    assertThat(releases).contains("ICGC21", "ICGC19");
  }

  @Test
  public void getDictionaryVersionTest() {
    assertThat(releaseResolver.getDictionaryVersion("ICGC19")).isEqualTo("0.11c");
  }

  @Test
  public void getValidProjectsTest() {
    assertThat(releaseResolver.getValidProjects("ICGC20")).hasSize(66);
  }

}