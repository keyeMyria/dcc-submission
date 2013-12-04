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
package org.icgc.dcc.submission.fs;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.regex.Pattern.compile;

import java.io.InputStream;
import java.util.List;
import java.util.regex.Pattern;

import lombok.Value;
import lombok.val;

import org.apache.hadoop.fs.Path;
import org.icgc.dcc.core.model.SubmissionFileTypes.SubmissionFileType;
import org.icgc.dcc.hadoop.fs.HadoopUtils;
import org.icgc.dcc.submission.release.model.Release;
import org.icgc.dcc.submission.release.model.ReleaseState;
import org.icgc.dcc.submission.release.model.Submission;
import org.icgc.dcc.submission.release.model.SubmissionState;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class SubmissionDirectory {

  private final DccFileSystem dccFileSystem;

  private final Release release;

  private final String projectKey;

  private final Submission submission;

  public SubmissionDirectory(DccFileSystem dccFileSystem, Release release, String projectKey, Submission submission) {
    super();

    checkArgument(dccFileSystem != null);
    checkArgument(release != null);
    checkArgument(projectKey != null);
    checkArgument(submission != null);

    this.dccFileSystem = dccFileSystem;
    this.release = release;
    this.projectKey = projectKey;
    this.submission = submission;
  }

  /**
   * (non-recursive) TODO: confirm
   */
  public Iterable<String> listFile(Pattern pattern) {
    List<Path> pathList = HadoopUtils.lsFile(
        this.dccFileSystem.getFileSystem(),
        new Path(getSubmissionDirPath()),
        pattern);
    return HadoopUtils.toFilenameList(pathList);
  }

  public Iterable<String> listFile() {
    return this.listFile(null);
  }

  /**
   * Returns the list of files that match a file pattern in the dictionary.
   */
  public Iterable<String> listFiles(final List<String> filePatterns) {
    return Iterables.filter(listFile(), new Predicate<String>() {

      @Override
      public boolean apply(String input) {
        for (String filePattern : filePatterns) {
          if (compile(filePattern).matcher(input).matches()) {
            return true;
          }
        }
        return false;
      }
    });
  }

  /**
   * If there is a matching file for the pattern, returns the one matching file or nothing. Errors out if there are more
   * than one matching file.
   */
  public Optional<String> getFile(String filePattern) {
    Iterable<String> files = listFiles(newArrayList(filePattern));
    val iterator = files.iterator();
    if (iterator.hasNext()) {
      val optional = Optional.of(iterator.next());
      checkState(!iterator.hasNext(),
          "There should only be one matching file for pattern '{}', instead got: '{}'", filePattern, files);
      return optional;
    } else {
      return Optional.<String> absent();
    }
  }

  public String addFile(String filename, InputStream data) {
    String filepath = this.dccFileSystem.buildFileStringPath(this.release.getName(), this.projectKey, filename);
    HadoopUtils.touch(this.dccFileSystem.getFileSystem(), filepath, data);
    return filepath;
  }

  public String deleteFile(String filename) {
    String filepath = this.dccFileSystem.buildFileStringPath(this.release.getName(), this.projectKey, filename);
    HadoopUtils.rm(this.dccFileSystem.getFileSystem(), filepath);
    return filepath;
  }

  public boolean isReadOnly() {
    SubmissionState state = this.submission.getState();

    return (state.isReadOnly() || this.release.getState() == ReleaseState.COMPLETED);
  }

  public String getProjectKey() {
    return this.projectKey;
  }

  public String getSubmissionDirPath() {
    return dccFileSystem.buildProjectStringPath(release.getName(), projectKey);
  }

  public String getValidationDirPath() {
    return dccFileSystem.buildValidationDirStringPath(release.getName(), projectKey);
  }

  public String getDataFilePath(String filename) {
    return dccFileSystem.buildFileStringPath(release.getName(), projectKey, filename);
  }

  public Submission getSubmission() {
    return this.submission;
  }

  public void resetValidationDir() {
    removeValidationDir();
    createEmptyValidationDir();
  }

  /**
   * TODO: port logic in here rather than in {@link DccFileSystem}
   */
  public void removeValidationDir() {
    dccFileSystem.removeDirIfExist(getValidationDirPath());
  }

  /**
   * TODO: port logic in here rather than in {@link DccFileSystem}
   */
  public void createEmptyValidationDir() {
    dccFileSystem.createDirIfDoesNotExist(getValidationDirPath());
  }

  public List<SubmissionDirectoryFile> getSubmissionFiles() {
    return null;
  }

  /**
   * There's already a "SubmissionFile" class (for the UI)...
   */
  @Value
  public class SubmissionDirectoryFile {

    String fileName;
    SubmissionFileType type;
    Pattern pattern;
  }

}
