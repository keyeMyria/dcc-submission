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

package org.icgc.dcc.submission.repository;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.icgc.dcc.submission.core.MailService;
import org.icgc.dcc.submission.core.morphia.BaseMorphiaService;
import org.icgc.dcc.submission.dictionary.model.CodeList;
import org.icgc.dcc.submission.dictionary.model.Dictionary;
import org.icgc.dcc.submission.dictionary.model.QDictionary;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.google.inject.Inject;

@Data
@EqualsAndHashCode(callSuper = false)
public class DictionaryRepository extends BaseMorphiaService<Dictionary> {

  @Inject
  public DictionaryRepository(Morphia morphia, Datastore datastore, MailService mailService) {
    super(morphia, datastore, QDictionary.dictionary, mailService);
    registerModelClasses(Dictionary.class, CodeList.class);
  }
}