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
package org.icgc.dcc.submission.config;

import static com.google.common.base.Preconditions.checkState;
import static com.google.inject.name.Names.named;
import static org.icgc.dcc.common.core.model.Configurations.HADOOP_KEY;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.icgc.dcc.common.core.util.InjectionNames;

import com.google.inject.AbstractModule;
import com.typesafe.config.Config;

/**
 * Makes {@code Config} injectable instead of accessible as a singleton.
 */
@Slf4j
@RequiredArgsConstructor
public class ConfigModule extends AbstractModule {

  @NonNull
  private final Config config;

  @Override
  protected void configure() {
    bind(Config.class).toInstance(config);

    // Bind hadoop properties for use in reporter
    checkState(config.hasPath(HADOOP_KEY));
    val hadoopProperties = Configs.asStringMap(config.getObject(HADOOP_KEY));
    log.info("Hadoop properties: '{}'", hadoopProperties);
    bind(TypeLiterals.STRING_MAP)
        .annotatedWith(
            named(InjectionNames.HADOOP_PROPERTIES))
        .toInstance(hadoopProperties);
  }

}