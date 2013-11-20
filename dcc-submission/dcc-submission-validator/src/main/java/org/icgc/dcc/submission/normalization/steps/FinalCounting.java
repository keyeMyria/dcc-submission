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
package org.icgc.dcc.submission.normalization.steps;

import static cascading.tuple.Fields.ALL;
import static cascading.tuple.Fields.RESULTS;
import static org.icgc.dcc.submission.normalization.NormalizationReport.NormalizationCounter.COUNT_INCREMENT;
import static org.icgc.dcc.submission.normalization.NormalizationReport.NormalizationCounter.TOTAL_END;

import org.icgc.dcc.submission.normalization.NormalizationContext;
import org.icgc.dcc.submission.normalization.NormalizationStep;
import org.icgc.dcc.submission.validation.cascading.CascadingFunctions.Counter;

import cascading.pipe.Each;
import cascading.pipe.Pipe;

/**
 * Performs final count of observations.
 * <p>
 * TODO: merge with {@link InitialCounting} by passing the counter to use.
 */
public class FinalCounting implements NormalizationStep {

  /**
   * Short name for the step.
   */
  private static final String SHORT_NAME = "final-count";

  @Override
  public String shortName() {
    return SHORT_NAME;
  }

  @Override
  public Pipe extend(Pipe pipe, NormalizationContext context) {
    return new Each(
        pipe,
        ALL,
        new Counter(TOTAL_END, COUNT_INCREMENT),
        RESULTS);
  }

}
