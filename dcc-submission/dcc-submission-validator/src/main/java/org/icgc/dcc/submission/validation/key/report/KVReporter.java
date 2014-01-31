/*
 * Copyright (c) 2014 The Ontario Institute for Cancer Research. All rights reserved.                             
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
package org.icgc.dcc.submission.validation.key.report;

import static org.codehaus.jackson.JsonGenerator.Feature.AUTO_CLOSE_TARGET;
import static org.codehaus.jackson.map.SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS;
import static org.icgc.dcc.submission.validation.key.core.KVDictionary.getErrorFieldNames;
import static org.icgc.dcc.submission.validation.key.core.KVDictionary.getOptionalReferencedFileType;
import static org.icgc.dcc.submission.validation.key.core.KVDictionary.getPrimaryKeyNames;
import static org.icgc.dcc.submission.validation.key.core.KVDictionary.getReferencingFileType;
import static org.icgc.dcc.submission.validation.key.core.KVDictionary.getSurjectionForeignKeyNames;
import static org.icgc.dcc.submission.validation.key.enumeration.KVErrorType.PRIMARY_RELATION;
import static org.icgc.dcc.submission.validation.key.enumeration.KVErrorType.SECONDARY_RELATION;
import static org.icgc.dcc.submission.validation.key.enumeration.KVErrorType.SURJECTION;
import static org.icgc.dcc.submission.validation.key.enumeration.KVErrorType.UNIQUENESS;
import static org.icgc.dcc.submission.validation.key.surjectivity.SurjectivityValidator.SURJECTION_ERROR_LINE_NUMBER;
import static org.icgc.dcc.submission.validation.key.utils.KVConstants.MAPPER;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;
import lombok.experimental.Builder;
import lombok.extern.slf4j.Slf4j;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.icgc.dcc.submission.validation.core.ErrorType;
import org.icgc.dcc.submission.validation.key.data.KVKey;
import org.icgc.dcc.submission.validation.key.enumeration.KVErrorType;
import org.icgc.dcc.submission.validation.key.enumeration.KVFileType;

/**
 * TODO
 */
@Slf4j
public class KVReporter implements Closeable {

  /**
   * The file name of the produced key validation report.
   */
  public static final String REPORT_FILE_NAME = "all.keys--errors.json";

  private final static ObjectWriter WRITER = new ObjectMapper(new JsonFactory().disable(AUTO_CLOSE_TARGET))
      .configure(FAIL_ON_EMPTY_BEANS, false)
      .writer();

  @NonNull
  private final FileSystem fileSystem;
  @NonNull
  private final Path path;
  @NonNull
  private final OutputStream outputStream;

  @SneakyThrows
  public KVReporter(FileSystem fileSystem, Path path) {
    this.fileSystem = fileSystem;
    this.path = path;
    this.outputStream = fileSystem.create(path);
  }

  @Override
  public void close() throws IOException {
    outputStream.close();
  }

  public void reportUniquenessError(KVFileType fileType, String fileName, long lineNumber, KVKey pk) {
    reportError(fileType, fileName, lineNumber, UNIQUENESS, pk);
  }

  public void reportRelationError(KVFileType fileType, String fileName, long lineNumber, KVKey fk) {
    reportError(fileType, fileName, lineNumber, PRIMARY_RELATION, fk);
  }

  public void reportSecondaryRelationError(KVFileType fileType, String fileName, long lineNumber, KVKey secondaryFk) {
    reportError(fileType, fileName, lineNumber, SECONDARY_RELATION, secondaryFk);
  }

  public void reportSurjectionError(KVFileType fileType, String fileName, KVKey keys) {
    reportError(fileType, fileName, SURJECTION_ERROR_LINE_NUMBER, SURJECTION, keys);
  }

  private void reportError(KVFileType fileType, String fileName, long lineNumber, KVErrorType errorType, KVKey keys) {
    log.debug("Reporting '{}' error at '({}, {}, {})': '{}'",
        new Object[] { errorType, fileType, fileName, lineNumber, keys });

    persistError(KVReportError.builder()

        .fileName(fileName)
        .fieldNames(getErrorFieldNames(fileType, errorType))
        .params(getErrorParams(fileType, errorType))
        .type(errorType.getErrorType())
        .lineNumber(lineNumber)
        .value(keys.getValues())

        .build());
  }

  @SneakyThrows
  private void persistError(KVReportError error) {
    WRITER.writeValue(outputStream, error);
  }

  private Object[] getErrorParams(KVFileType fileType, KVErrorType errorType) {
    Object[] errorParams = null;

    // PRIMARY/SECONDARY RELATION:
    if (errorType == PRIMARY_RELATION || errorType == SECONDARY_RELATION) {
      val referencedFileType = getOptionalReferencedFileType(fileType).get();
      val referencedFields = getPrimaryKeyNames(referencedFileType);
      errorParams = new Object[] { referencedFileType, referencedFields };
    }

    // SURJECTION
    else if (errorType == SURJECTION) {
      val referencingFileType = getReferencingFileType(fileType);
      val referencingFields = getSurjectionForeignKeyNames(referencingFileType);
      errorParams = new Object[] { referencingFileType, referencingFields };
    }

    // UNIQUENESS: uniqueness errors don't need params

    return errorParams;
  }

  @Value
  @Builder
  public static class KVReportError {

    @JsonProperty
    String fileName;
    @JsonProperty
    List<String> fieldNames;
    @JsonProperty
    long lineNumber;
    @JsonProperty
    Object value;
    @JsonProperty
    ErrorType type;
    @JsonProperty
    Object[] params;

    private KVReportError(
        @JsonProperty("fileName") String fileName,
        @JsonProperty("fieldNames") List<String> fieldNames,
        @JsonProperty("lineNumber") long lineNumber,
        @JsonProperty("value") Object value,
        @JsonProperty("type") ErrorType type,
        @JsonProperty("params") Object[] params)
    {
      this.fileName = fileName;
      this.fieldNames = fieldNames;
      this.lineNumber = lineNumber;
      this.value = value;
      this.type = type;
      this.params = params;
    }

    @Override
    public String toString() {
      return toJsonSummaryString();
    }

    @SneakyThrows
    public String toJsonSummaryString() {
      return "\n" + MAPPER
          .writerWithDefaultPrettyPrinter()
          .writeValueAsString(this);
    }
  }

}
