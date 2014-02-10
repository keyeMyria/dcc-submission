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
package org.icgc.dcc.submission.core.state;

import static lombok.AccessLevel.PACKAGE;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.val;

import org.icgc.dcc.core.model.DataType;
import org.icgc.dcc.submission.release.model.Release;
import org.icgc.dcc.submission.release.model.Submission;
import org.icgc.dcc.submission.release.model.SubmissionState;

@NoArgsConstructor(access = PACKAGE)
public class ValidState extends AbstractState {

  @Override
  public boolean isReadOnly() {
    return false;
  }

  @Override
  public void queueRequest(@NonNull StateContext context, @NonNull Iterable<DataType> dataTypes) {
    context.setState(SubmissionState.QUEUED);

    val report = context.getReport();
    report.updateFiles(context.getSubmissionFiles());
    report.reset(dataTypes);
    report.setState(SubmissionState.QUEUED, dataTypes);
  }

  @Override
  public void signOff(@NonNull StateContext context) {
    context.setState(SubmissionState.SIGNED_OFF);
  }

  @Override
  public Submission performRelease(StateContext context, Release nextRelease) {
    val nextSubmission =
        new Submission(context.getProjectKey(), context.getProjectName(), nextRelease.getName(), SubmissionState.VALID);
    nextSubmission.setReport(context.getReport());

    return nextSubmission;
  }

}