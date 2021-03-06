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
package org.icgc.dcc.submission.validation.first.file;

import static org.icgc.dcc.common.core.util.Formats.formatCount;
import static org.icgc.dcc.submission.core.report.Error.error;
import static org.icgc.dcc.submission.core.report.ErrorType.LINE_TERMINATOR_MISSING_ERROR;
import static org.icgc.dcc.submission.validation.core.Validators.checkInterrupted;

import java.io.BufferedInputStream;

import org.icgc.dcc.submission.dictionary.model.FileSchema;
import org.icgc.dcc.submission.validation.first.core.AbstractDelegatingChecker;
import org.icgc.dcc.submission.validation.first.core.FileChecker;
import org.icgc.dcc.submission.validation.first.core.RowChecker;

import com.google.common.base.Stopwatch;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class DelegatingFileRowChecker extends AbstractDelegatingChecker implements RowChecker, FileChecker {

  /**
   * Number of bytes to buffer when reading submission files.
   * 
   * @see http
   * ://stackoverflow.com/questions/236861/how-do-you-determine-the-ideal-buffer-size-when-using-fileinputstream
   */
  private static final int LINE_BUFFER_SIZE = 8192;

  /**
   * Number of lines checked between status logging.
   */
  private static final long LINE_STATUS_THRESHOLD = 1000L * 1000L;

  /**
   * Constants.
   */
  private static final char LINE_SEPARATOR_CHAR = '\n';

  @NonNull
  protected final RowChecker delegate;

  public DelegatingFileRowChecker(RowChecker delegate, boolean failFast) {
    super(delegate, failFast);
    this.delegate = delegate;
  }

  public DelegatingFileRowChecker(RowChecker nestedChecker) {
    this(nestedChecker, false);
  }

  @Override
  @SneakyThrows
  public void checkFile(String fileName) {
    log.info(banner());

    log.info("Start performing {} validation...", name);
    val fileSchema = getFileSchema(fileName);

    @Cleanup
    val inputStream = new BufferedInputStream(
        getFileSystem().getDecompressingInputStream(fileName),
        LINE_BUFFER_SIZE);
    val watch = Stopwatch.createStarted();
    val line = new StringBuilder(512);
    long lineNumber = 1;

    int nextByte = 0;
    while ((nextByte = inputStream.read()) > 0) {
      if ((char) nextByte == LINE_SEPARATOR_CHAR) {

        // Delegate
        checkRow(fileName, fileSchema, line, lineNumber);

        // Book-keeping
        ++lineNumber;

        if (lineNumber % 10000 == 0) {
          // Check for cancellation
          checkInterrupted(name);
        }

        if (lineNumber % LINE_STATUS_THRESHOLD == 0L) {
          // Log status
          log.info("Checked {} lines of '{}' in {}",
              new Object[] { formatCount(lineNumber), fileName, watch });
        }

        // Reset
        line.setLength(0);
      } else {
        // Buffer
        line.appendCodePoint(nextByte);
      }
    }

    // Check buffer to be empty, otherwise we have a file with no trailing new line
    if (line.length() > 0) {
      log.info("Missing new line at end of file '{}'", fileName);

      reportError(error()
          .fileName(fileName)
          .lineNumber(lineNumber)
          .type(LINE_TERMINATOR_MISSING_ERROR)
          .build());
    }

    log.info("Finishing performing {} validation...", name);
    finish(fileName, fileSchema);

    log.info("Completed '{}' validation on '{}' in {}. Number of errors found: {}",
        new Object[] { name, fileName, watch, formatCount(checkErrorCount) });
  }

  @Override
  public void checkRow(String filename, FileSchema fileSchema, CharSequence row, long lineNumber) {
    delegate.checkRow(filename, fileSchema, row, lineNumber);
    if (delegate.canContinue()) {
      if (log.isDebugEnabled()) {
        log.debug(
            "Start performing {} validation for row '{}'...", row, name);
      }

      performSelfCheck(filename, fileSchema, row, lineNumber);

      if (log.isDebugEnabled()) {
        log.debug("End performing {} validation for row '{}'", row, name);
      }
    }
  }

  @Override
  public void finish(String fileName, FileSchema fileSchema) {
    delegate.finish(fileName, fileSchema);

    performSelfFinish(fileName, fileSchema);
  }

  /**
   * Template methods
   */
  abstract void performSelfCheck(String filename, FileSchema fileSchema, CharSequence row, long lineNumber);

  abstract void performSelfFinish(String fileName, FileSchema fileSchema);

}
