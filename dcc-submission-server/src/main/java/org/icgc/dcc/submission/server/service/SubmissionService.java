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
package org.icgc.dcc.submission.server.service;

import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableMap;
import static org.icgc.dcc.submission.release.model.SubmissionState.SIGNED_OFF;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import lombok.NonNull;
import lombok.val;

import org.icgc.dcc.submission.release.model.Submission;
import org.icgc.dcc.submission.server.repository.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Optional;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class SubmissionService extends AbstractService {

  private final SubmissionRepository submissionRepository;

  @Autowired
  public SubmissionService(
      @NonNull final MailService mailService,
      @NonNull final SubmissionRepository submissionRepository) {
    super(mailService);
    this.submissionRepository = submissionRepository;
  }

  public Map<String, Submission> findProjectKeysToSubmissions(@NonNull String releaseName,
      @NonNull Collection<String> projectKeys) {
    val submissions = submissionRepository.findSubmissions(releaseName, projectKeys);
    return submissions.stream()
        .collect(toImmutableMap(Submission::getProjectKey, submission -> submission));
  }

  public Map<String, Submission> findSubmissionsByProjectKey(@NonNull String releaseName) {
    return findSubmissions(releaseName).stream()
        .collect(toImmutableMap(Submission::getProjectKey, submission -> submission));
  }

  public List<Submission> findSubmissionStateByReleaseName(@NonNull String releaseName) {
    return submissionRepository.findSubmissionStateByReleaseName(releaseName);
  }

  public Optional<Submission> findSubmission(@NonNull String releaseName, @NonNull String projectKey) {
    return Optional.fromNullable(submissionRepository.findSubmission(releaseName, projectKey));
  }

  public List<Submission> findSubmissions(@NonNull String releaseName) {
    return submissionRepository.findSubmissions(releaseName);
  }

  public List<Submission> findSubmissionByProjectKey(@NonNull String projectKey) {
    return submissionRepository.findSubmissionByProjectKey(projectKey);
  }

  public Multimap<String, Submission> findReleaseNameToSubmissions() {
    val submissions = submissionRepository.findSubmissions();
    val releaseSubmissions = ArrayListMultimap.<String, Submission> create();
    for (val submission : submissions) {
      releaseSubmissions.put(submission.getReleaseName(), submission);
    }

    return releaseSubmissions;
  }

  public List<String> findReleaseProjectKeys(@NonNull String releaseName) {
    return findSubmissions(releaseName).stream()
        .map(Submission::getProjectKey)
        .collect(toImmutableList());
  }

  /**
   * Updates existing submissions. Does not create new ones if the submission doesn't exist.
   */
  public void updateExistingSubmissions(@NonNull Iterable<Submission> submissions) {
    submissionRepository.updateExistingSubmissions(submissions);
  }

  /**
   * Updates an existing submission. Does not create new ones if the submission doesn't exist.
   */
  public void updateSubmission(@NonNull Submission submission) {
    submissionRepository.updateSubmission(submission);
  }

  public void addSubmissions(@NonNull Iterable<Submission> submissions) {
    submissionRepository.addSubmissions(submissions);
  }

  public void addSubmission(@NonNull Submission submission) {
    submissionRepository.addSubmission(submission);
  }

  public void deleteUnsignedSubmissions(@NonNull String releaseName) {
    submissionRepository.deleteByReleaseAndNotState(releaseName, SIGNED_OFF);
  }

}
