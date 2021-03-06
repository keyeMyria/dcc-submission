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
package org.icgc.dcc.submission.loader.file.postgres;

import static org.icgc.dcc.submission.loader.util.HdfsFiles.getCompressionAgnosticBufferedReader;

import javax.sql.DataSource;

import org.icgc.dcc.submission.loader.file.AbstractFileLoaderFactory;
import org.icgc.dcc.submission.loader.file.FileLoader;
import org.icgc.dcc.submission.loader.model.FileTypePath;
import org.icgc.dcc.submission.loader.record.PostgressRecordConverter;
import org.icgc.dcc.submission.loader.record.RecordReader;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class PostgressFileLoaderFactory extends AbstractFileLoaderFactory {

  @NonNull
  private final String release;
  @NonNull
  private final DataSource dataSource;

  @Override
  public FileLoader createFileLoader(@NonNull String project, @NonNull String release, @NonNull FileTypePath fileType) {
    val file = fileType.getPath();
    val recordReader = new RecordReader(getCompressionAgnosticBufferedReader(file));
    val type = fileType.getType();
    val jdbcInserter = createJdbcInserter(type, release);
    val codeListDecoder = createCodeListValuesDecoder(release, type);
    val converter = new PostgressRecordConverter(project, codeListDecoder);

    return new PostgressFileLoader(project, type, recordReader, jdbcInserter, converter);
  }

  private SimpleJdbcInsert createJdbcInserter(String type, String schema) {
    return new SimpleJdbcInsert(dataSource)
        .withSchemaName(schema.toLowerCase())
        .withTableName(type);
  }

}
