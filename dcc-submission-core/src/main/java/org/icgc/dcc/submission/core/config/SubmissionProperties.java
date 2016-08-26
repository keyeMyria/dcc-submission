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
package org.icgc.dcc.submission.core.config;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Data;

@Data
public class SubmissionProperties {

  /**
   * Default value for maximum number of concurrent validations.
   */
  private static final int DEFAULT_MAX_VALIDATING = 1;

  String fsRoot;
  String fsUrl;

  HttpProperties http = new HttpProperties();
  SftpProperties sftp = new SftpProperties();
  MailProperties mail = new MailProperties();
  ShiroProperties shiro = new ShiroProperties();
  HadoopProperties hadoop = new HadoopProperties();
  MongoProperties mongo = new MongoProperties();
  PCAWGProperties pcawg = new PCAWGProperties();
  ReferenceProperties reference = new ReferenceProperties();
  NormalizerProperties normalizer = new NormalizerProperties();

  ValidatorProperties validator = new ValidatorProperties();
  List<String> validators = newArrayList();

  @Data
  public static class HadoopProperties {

    Map<String, String> properties = newHashMap();

  }

  @Data
  public static class MongoProperties {

    String uri;

  }

  @Data
  public static class ValidatorProperties {

    int maxSimultaneous = DEFAULT_MAX_VALIDATING;

  }

  @Data
  public static class PCAWGProperties {

    URL dictionaryUrl;
    URL sampleSheetUrl;

  }

  @Data
  public static class ReferenceProperties {

    String fasta;

  }

  @Data
  public static class MailProperties {

    Boolean enabled = true;

    String subject;

    String errorBody;
    String notValidatedBody;
    String invalidBody;
    String validBody;
    String signoffBody;

    String fromEmail;
    String supportEmail;
    String notificationEmail;

    String smtpHost;
    String smtpPort = "25";
    String smtpTimeout = "5000";
    String smtpConnectionTimeout = "5000";

  }

  @Data
  public static class NormalizerProperties {

    Float errorThreshold = 0.1f;

    Map<String, String> steps = newHashMap();

  }

  @Data
  public static class SftpProperties {

    String key;
    String path;
    Integer port;
    Integer nioWorkers;

  }

  @Data
  public static class HttpProperties {

    String listen;
    Integer port;
    Boolean ssl;
    Set<String> resources = newHashSet();
    String path;

  }

  @Data
  public static class ShiroProperties {

    String realm;

  }

}