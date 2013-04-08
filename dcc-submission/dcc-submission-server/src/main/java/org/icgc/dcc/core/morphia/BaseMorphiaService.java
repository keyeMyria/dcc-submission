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
package org.icgc.dcc.core.morphia;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.google.inject.Inject;
import com.mysema.query.mongodb.MongodbQuery;
import com.mysema.query.mongodb.morphia.MorphiaQuery;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Predicate;

public abstract class BaseMorphiaService<T> {

  private final Morphia morphia;

  private final Datastore datastore;

  private final EntityPath<T> entityPath;

  @Inject
  public BaseMorphiaService(Morphia morphia, Datastore datastore, EntityPath<T> entityPath) {
    super();
    checkArgument(morphia != null);
    checkArgument(datastore != null);
    this.morphia = morphia;
    this.datastore = datastore;
    this.entityPath = entityPath;
  }

  public Datastore datastore() {
    return datastore;
  }

  public Morphia morphia() {
    return morphia;
  }

  public MongodbQuery<T> query() {
    return new MorphiaQuery<T>(morphia(), datastore(), entityPath);
  }

  public MongodbQuery<T> where(Predicate predicate) {
    return query().where(predicate);
  }

  protected void registerModelClasses(Class<?>... entities) {
    if(entities != null) {
      for(Class<?> e : entities) {
        morphia.map(e);
        datastore.ensureIndexes(e);
      }
    }
  }
}