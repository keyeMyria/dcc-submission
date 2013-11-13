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
import static cascading.tuple.Fields.ARGS;
import static cascading.tuple.Fields.REPLACE;
import static com.google.common.base.Preconditions.checkState;
import static org.icgc.dcc.core.model.FieldNames.SubmissionFieldNames.SUBMISSION_OBSERVATION_CONTROL_GENOTYPE;
import static org.icgc.dcc.core.model.FieldNames.SubmissionFieldNames.SUBMISSION_OBSERVATION_MUTATED_FROM_ALLELE;
import static org.icgc.dcc.core.model.FieldNames.SubmissionFieldNames.SUBMISSION_OBSERVATION_MUTATED_TO_ALLELE;
import static org.icgc.dcc.core.model.FieldNames.SubmissionFieldNames.SUBMISSION_OBSERVATION_REFERENCE_GENOME_ALLELE;
import static org.icgc.dcc.core.model.FieldNames.SubmissionFieldNames.SUBMISSION_OBSERVATION_TUMOUR_GENOTYPE;
import static org.icgc.dcc.submission.normalization.NormalizationReport.NormalizationCounter.COUNT_INCREMENT;
import static org.icgc.dcc.submission.normalization.NormalizationReport.NormalizationCounter.MARKED_AS_CONTROLLED;
import static org.icgc.dcc.submission.normalization.NormalizationReport.NormalizationCounter.MASKED;
import static org.icgc.dcc.submission.normalization.steps.Masking.CONTROLLED;
import static org.icgc.dcc.submission.normalization.steps.Masking.NORMALIZER_MASKING_FIELD;
import static org.icgc.dcc.submission.normalization.steps.Masking.OPEN;
import static org.icgc.dcc.submission.validation.cascading.CascadingFunctions.NO_VALUE;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.icgc.dcc.hadoop.cascading.TupleEntries;
import org.icgc.dcc.submission.normalization.NormalizationConfig;
import org.icgc.dcc.submission.normalization.NormalizationConfig.OptionalStep;
import org.icgc.dcc.submission.normalization.NormalizationContext;
import org.icgc.dcc.submission.normalization.NormalizationStep;

import cascading.flow.FlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Function;
import cascading.operation.FunctionCall;
import cascading.pipe.Each;
import cascading.pipe.Pipe;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.typesafe.config.Config;

/**
 * TODO
 */
@Slf4j
@RequiredArgsConstructor
public final class AlleleMasking implements NormalizationStep, OptionalStep {

  public static final String STEP_NAME = "masking";

  static final Fields REFERENCE_GENOME_ALLELE_FIELD = new Fields(SUBMISSION_OBSERVATION_REFERENCE_GENOME_ALLELE);
  static final Fields CONTROL_GENOTYPE_FIELD = new Fields(SUBMISSION_OBSERVATION_CONTROL_GENOTYPE);
  static final Fields TUMOUR_GENOTYPE_FIELD = new Fields(SUBMISSION_OBSERVATION_TUMOUR_GENOTYPE);
  static final Fields MUTATED_FROM_ALLELE_FIELD = new Fields(SUBMISSION_OBSERVATION_MUTATED_FROM_ALLELE);
  static final Fields MUTATED_TO_ALLELE_FIELD = new Fields(SUBMISSION_OBSERVATION_MUTATED_TO_ALLELE);

  private final Config config;

  @Override
  public String shortName() {
    return STEP_NAME;
  }

  @Override
  public Pipe extend(Pipe pipe, NormalizationContext context) {
    {
      Fields argumentSelector =
          REFERENCE_GENOME_ALLELE_FIELD
              .append(MUTATED_FROM_ALLELE_FIELD)
              .append(NORMALIZER_MASKING_FIELD);
      pipe =
          new Each(
              pipe,
              argumentSelector,
              new SensitiveRowMarker(),
              REPLACE);
    }

    if (!NormalizationConfig.isMarkOnly(config)) {
      pipe = new Each(
          pipe,
          ALL,
          new MaskedRowGenerator(),
          REPLACE);
    }

    return pipe;
  }

  /**
   * TODO expects flag already
   */
  @VisibleForTesting
  static final class SensitiveRowMarker extends BaseOperation<Void> implements Function<Void> {

    @VisibleForTesting
    SensitiveRowMarker() {
      super(ARGS);
    }

    @Override
    public void operate(
        @SuppressWarnings("rawtypes") FlowProcess flowProcess,
        FunctionCall<Void> functionCall) {

      val entry = functionCall.getArguments();
      {
        val existingMasking = Masking.getMasking(entry.getString(Masking.NORMALIZER_MASKING_FIELD));
        checkState(existingMasking.isPresent() && existingMasking.get() == Masking.OPEN,
            "Masking flag is expected to have been set to '%s' already", OPEN);
      }

      val referenceGenomeAllele = entry.getString(REFERENCE_GENOME_ALLELE_FIELD);
      val mutatedFromAllele = entry.getString(MUTATED_FROM_ALLELE_FIELD);

      final Masking masking;
      if (isSensitive(
          referenceGenomeAllele,
          mutatedFromAllele)) {
        log.info("Marking sensitive row: '{}'", entry); // Should be rare enough
        masking = CONTROLLED;

        flowProcess.increment(MARKED_AS_CONTROLLED, COUNT_INCREMENT);
      } else {
        log.debug("Marking open-access row: '{}'", entry);
        masking = OPEN;
      }

      functionCall
          .getOutputCollector()
          .add(new Tuple(referenceGenomeAllele, mutatedFromAllele, masking.getTupleValue()));
    }

    private boolean isSensitive(String referenceGenomeAllele, String mutatedFromAllele) {
      return !referenceGenomeAllele.equals(mutatedFromAllele);
    }
  }

  /**
   * 
   */
  @VisibleForTesting
  static final class MaskedRowGenerator extends BaseOperation<Void> implements Function<Void> {

    @VisibleForTesting
    MaskedRowGenerator() {
      super(ARGS);
    }

    @Override
    public void operate(
        @SuppressWarnings("rawtypes") FlowProcess flowProcess,
        FunctionCall<Void> functionCall) {

      val entry = functionCall.getArguments();

      functionCall
          .getOutputCollector()
          .add(entry.getTupleCopy());

      // Create masked counterpart if sensitive and mask is non trivial (see
      // https://wiki.oicr.on.ca/display/DCCSOFT/Data+Normalizer+Component?focusedCommentId=53182773#comment-53182773)
      if (getMaskingState(entry) == CONTROLLED) {
        val referenceGenomeAllele = entry.getString(REFERENCE_GENOME_ALLELE_FIELD);
        val mutatedToAllele = entry.getString(MUTATED_TO_ALLELE_FIELD);
        if (!isTrivialMaskedMutation(
            referenceGenomeAllele,
            mutatedToAllele)) {
          log.info("Creating mask for '{}'", entry); // Rare enough that we can log
          val mask = mask(
              TupleEntries.clone(entry),
              referenceGenomeAllele);

          log.info("Resulting mask for '{}': '{}'", entry, mask); // Rare enough that we can log
          functionCall
              .getOutputCollector()
              .add(mask);

          flowProcess.increment(MASKED, COUNT_INCREMENT);
        } else {
          log.info("Skipping trivial mask for '{}'", entry); // Rare enough that we can log
        }
      }
    }

    /**
     * 
     */
    private Tuple mask(TupleEntry copy, String referenceGenomeAllele) {
      copy.set(NORMALIZER_MASKING_FIELD, Masking.MASKED.getTupleValue());
      copy.set(CONTROL_GENOTYPE_FIELD, NO_VALUE);
      copy.set(TUMOUR_GENOTYPE_FIELD, NO_VALUE);
      copy.setString(MUTATED_FROM_ALLELE_FIELD, referenceGenomeAllele);
      return copy.getTuple();
    }

    /**
     * Returns the value for masking as set by the previous step.
     */
    private Masking getMaskingState(TupleEntry entry) {
      String maskingString = entry.getString(NORMALIZER_MASKING_FIELD);
      Optional<Masking> masking = Masking.getMasking(maskingString);
      checkState(masking.isPresent(), "TODO");
      return masking.get();
    }

    /**
     * We don't want to create a masked copy that would be result in a mutation like 'A>A' (useless).
     */
    private boolean isTrivialMaskedMutation(String referenceGenomeAllele, String mutatedToAllele) {
      return referenceGenomeAllele.equals(mutatedToAllele);
    }
  }
}
