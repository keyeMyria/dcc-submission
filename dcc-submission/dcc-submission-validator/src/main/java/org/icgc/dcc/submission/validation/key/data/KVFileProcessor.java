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
package org.icgc.dcc.submission.validation.key.data;

import static com.google.common.base.Preconditions.checkState;
import static lombok.AccessLevel.PUBLIC;
import static org.icgc.dcc.submission.validation.key.core.KVDictionary.getRow;
import static org.icgc.dcc.submission.validation.key.core.KVProcessor.ROW_CHECKS_ENABLED;
import static org.icgc.dcc.submission.validation.key.enumeration.KVFileType.CNSM_M;
import static org.icgc.dcc.submission.validation.key.enumeration.KVFileType.CNSM_P;
import static org.icgc.dcc.submission.validation.key.enumeration.KVFileType.CNSM_S;
import static org.icgc.dcc.submission.validation.key.enumeration.KVFileType.DONOR;
import static org.icgc.dcc.submission.validation.key.enumeration.KVFileType.EXP_G;
import static org.icgc.dcc.submission.validation.key.enumeration.KVFileType.EXP_M;
import static org.icgc.dcc.submission.validation.key.enumeration.KVFileType.JCN_M;
import static org.icgc.dcc.submission.validation.key.enumeration.KVFileType.JCN_P;
import static org.icgc.dcc.submission.validation.key.enumeration.KVFileType.METH_M;
import static org.icgc.dcc.submission.validation.key.enumeration.KVFileType.METH_P;
import static org.icgc.dcc.submission.validation.key.enumeration.KVFileType.METH_S;
import static org.icgc.dcc.submission.validation.key.enumeration.KVFileType.MIRNA_M;
import static org.icgc.dcc.submission.validation.key.enumeration.KVFileType.MIRNA_P;
import static org.icgc.dcc.submission.validation.key.enumeration.KVFileType.MIRNA_S;
import static org.icgc.dcc.submission.validation.key.enumeration.KVFileType.PEXP_M;
import static org.icgc.dcc.submission.validation.key.enumeration.KVFileType.PEXP_P;
import static org.icgc.dcc.submission.validation.key.enumeration.KVFileType.SAMPLE;
import static org.icgc.dcc.submission.validation.key.enumeration.KVFileType.SGV_M;
import static org.icgc.dcc.submission.validation.key.enumeration.KVFileType.SGV_P;
import static org.icgc.dcc.submission.validation.key.enumeration.KVFileType.SPECIMEN;
import static org.icgc.dcc.submission.validation.key.enumeration.KVFileType.SSM_M;
import static org.icgc.dcc.submission.validation.key.enumeration.KVFileType.SSM_P;
import static org.icgc.dcc.submission.validation.key.enumeration.KVFileType.STSM_M;
import static org.icgc.dcc.submission.validation.key.enumeration.KVFileType.STSM_P;
import static org.icgc.dcc.submission.validation.key.enumeration.KVFileType.STSM_S;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.hadoop.fs.Path;
import org.icgc.dcc.submission.core.parser.FileRecordProcessor;
import org.icgc.dcc.submission.validation.key.core.KVFileParser;
import org.icgc.dcc.submission.validation.key.enumeration.KVFileType;
import org.icgc.dcc.submission.validation.key.report.KVReporter;

import com.google.common.base.Optional;

/**
 * TODO: MUST split class on a per type basis
 */
@Slf4j
@RequiredArgsConstructor(access = PUBLIC)
public final class KVFileProcessor {

  private static final int DEFAULT_LOG_THRESHOLD = 1000000;

  private final KVFileType fileType;
  private final Path filePath;

  @SneakyThrows
  public void processFile(
      final KVFileParser fileParser,
      final KVReporter reporter, // To report all but surjection errors at this point
      final KVPrimaryKeys primaryKeys,
      final Optional<KVPrimaryKeys> optionalReferencedPrimaryKeys, // N/A for DONOR for instance
      final Optional<KVEncounteredForeignKeys> optionalEncounteredKeys // N/A for SSM_P for instance
  ) {
    log.info("{}", fileType, filePath);

    fileParser.parse(filePath, new FileRecordProcessor<List<String>>() {

      @Override
      public void process(long lineNumber, List<String> record) {
        val row = getRow(fileType, record);
        log.debug("Row: '{}'", row);

        processRow(row, lineNumber, reporter, primaryKeys, optionalReferencedPrimaryKeys, optionalEncounteredKeys);

        if ((lineNumber % DEFAULT_LOG_THRESHOLD) == 0) {
          logProcessedLine(lineNumber, false);
        }
      }

    });
  }

  /**
   * Processes a row (performs some validation).
   */
  private void processRow(
      KVRow row,
      long lineCount,
      KVReporter reporter,
      KVPrimaryKeys primaryKeys,
      Optional<KVPrimaryKeys> optionalReferencedPrimaryKeys,
      Optional<KVEncounteredForeignKeys> optionalEncounteredKeys) {

    val fileName = filePath.getName();

    // Clinical
    // DONOR
    if (fileType == DONOR) { // TODO: split per file type (subclass or compose)

      // Uniqueness check
      if (primaryKeys.containsPk(row.getPk())) {
        reporter.reportUniquenessError(fileType, fileName, lineCount, row.getPk());
      }

      // No foreign key check for DONOR
      primaryKeys.updatePks(fileName, row);
      if (ROW_CHECKS_ENABLED) checkState(!row.hasFk()); // Hence no surjection
      if (ROW_CHECKS_ENABLED) checkState(!row.hasSecondaryFk());
    }

    // SPECIMEN
    else if (fileType == SPECIMEN) {

      // Uniqueness check
      if (primaryKeys.containsPk(row.getPk())) {
        reporter.reportUniquenessError(fileType, fileName, lineCount, row.getPk());
      }

      // Foreign key check
      if (!hasMatchingReference(optionalReferencedPrimaryKeys, row.getFk())) {
        reporter.reportRelationError(fileType, fileName, lineCount, row.getFk());
      }

      primaryKeys.updatePks(fileName, row);
      ensurePresent(optionalEncounteredKeys);
      optionalEncounteredKeys.get().addEncounteredForeignKey(row.getFk());
      if (ROW_CHECKS_ENABLED) checkState(!row.hasSecondaryFk());
    }

    // SAMPLE
    else if (fileType == SAMPLE) {

      // Uniqueness check
      if (primaryKeys.containsPk(row.getPk())) {
        reporter.reportUniquenessError(fileType, fileName, lineCount, row.getPk());
      }

      // Foreign key check
      if (!hasMatchingReference(optionalReferencedPrimaryKeys, row.getFk())) {
        reporter.reportRelationError(fileType, fileName, lineCount, row.getFk());
      }

      primaryKeys.updatePks(fileName, row);
      ensurePresent(optionalEncounteredKeys);
      optionalEncounteredKeys.get().addEncounteredForeignKey(row.getFk());
      if (ROW_CHECKS_ENABLED) checkState(!row.hasSecondaryFk());
    }

    // SSM

    // SSM_M
    else if (fileType == SSM_M) {

      // Uniqueness check
      if (primaryKeys.containsPk(row.getPk())) {
        reporter.reportUniquenessError(fileType, fileName, lineCount, row.getPk());

      }

      // Foreign key check
      if (!hasMatchingReference(optionalReferencedPrimaryKeys, row.getFk())) {
        reporter.reportRelationError(fileType, fileName, lineCount, row.getFk());
      }

      // Secondary foreign key check
      if (row.hasSecondaryFk() // May not have a secondary FK (optional)
          && !hasMatchingReference(
              optionalReferencedPrimaryKeys,
              row.getSecondaryFk())) {
        reporter.reportSecondaryRelationError(fileType, fileName, lineCount, row.getSecondaryFk());
      }

      primaryKeys.updatePks(fileName, row);
      // Not checking for surjection with meta files
    }

    // SSM_P
    else if (fileType == SSM_P) {
      ; // No uniqueness check for SSM_P

      // Foreign key check
      if (!hasMatchingReference(optionalReferencedPrimaryKeys, row.getFk())) {
        reporter.reportRelationError(fileType, fileName, lineCount, row.getFk());
      }

      if (ROW_CHECKS_ENABLED) ensureNoPK(row);
      ensurePresent(optionalEncounteredKeys);
      optionalEncounteredKeys.get().addEncounteredForeignKey(row.getFk());
      if (ROW_CHECKS_ENABLED) checkState(!row.hasSecondaryFk());
    }

    // CNSM

    // CNSM_M
    else if (fileType == CNSM_M) {

      // Uniqueness check
      if (primaryKeys.containsPk(row.getPk())) {
        reporter.reportUniquenessError(fileType, fileName, lineCount, row.getPk());
      }

      // Foreign key check
      if (!hasMatchingReference(optionalReferencedPrimaryKeys, row.getFk())) {
        reporter.reportRelationError(fileType, fileName, lineCount, row.getFk());
      }

      // Secondary foreign key check
      if (row.hasSecondaryFk()
          && !hasMatchingReference(
              optionalReferencedPrimaryKeys,
              row.getSecondaryFk())) {
        reporter.reportSecondaryRelationError(fileType, fileName, lineCount, row.getSecondaryFk());
      }

      primaryKeys.updatePks(fileName, row);
      // Not checking for surjection with meta files
    }

    // CNSM_P
    else if (fileType == CNSM_P) {

      // Uniqueness check
      if (primaryKeys.containsPk(row.getPk())) {
        reporter.reportUniquenessError(fileType, fileName, lineCount, row.getPk());

      }

      // Foreign key check
      if (!hasMatchingReference(optionalReferencedPrimaryKeys, row.getFk())) {
        reporter.reportRelationError(fileType, fileName, lineCount, row.getFk());

      }

      primaryKeys.updatePks(fileName, row);
      ensurePresent(optionalEncounteredKeys);
      optionalEncounteredKeys.get().addEncounteredForeignKey(row.getFk());
      if (ROW_CHECKS_ENABLED) checkState(!row.hasSecondaryFk());
    }

    // CNSM_S
    else if (fileType == CNSM_S) {
      ; // No uniqueness check for CNSM

      // Foreign key check
      if (!hasMatchingReference(optionalReferencedPrimaryKeys, row.getFk())) {
        reporter.reportRelationError(fileType, fileName, lineCount, row.getFk());

      }

      if (ROW_CHECKS_ENABLED) ensureNoPK(row);
      ; // No surjection between secondary and primary
      if (ROW_CHECKS_ENABLED) checkState(!row.hasSecondaryFk());
    }

    // STSM

    // STSM_M
    else if (fileType == STSM_M) {

      // Uniqueness check
      if (primaryKeys.containsPk(row.getPk())) {
        reporter.reportUniquenessError(fileType, fileName, lineCount, row.getPk());
      }

      // Foreign key check
      if (!hasMatchingReference(optionalReferencedPrimaryKeys, row.getFk())) {
        reporter.reportRelationError(fileType, fileName, lineCount, row.getFk());
      }

      // Secondary foreign key check
      if (row.hasSecondaryFk()
          && !hasMatchingReference(
              optionalReferencedPrimaryKeys,
              row.getSecondaryFk())) {
        reporter.reportSecondaryRelationError(fileType, fileName, lineCount, row.getSecondaryFk());
      }

      primaryKeys.updatePks(fileName, row);
      // Not checking for surjection with meta files
    }

    // STSM_P
    else if (fileType == STSM_P) {

      // Uniqueness check
      if (primaryKeys.containsPk(row.getPk())) {
        reporter.reportUniquenessError(fileType, fileName, lineCount, row.getPk());

      }

      // Foreign key check
      if (!hasMatchingReference(optionalReferencedPrimaryKeys, row.getFk())) {
        reporter.reportRelationError(fileType, fileName, lineCount, row.getFk());

      }

      primaryKeys.updatePks(fileName, row);
      ensurePresent(optionalEncounteredKeys);
      optionalEncounteredKeys.get().addEncounteredForeignKey(row.getFk());
      if (ROW_CHECKS_ENABLED) checkState(!row.hasSecondaryFk());
    }

    // STSM_S
    else if (fileType == STSM_S) {
      ; // No uniqueness check for STSM_s

      // Foreign key check
      if (!hasMatchingReference(optionalReferencedPrimaryKeys, row.getFk())) {
        reporter.reportRelationError(fileType, fileName, lineCount, row.getFk());

      }

      if (ROW_CHECKS_ENABLED) ensureNoPK(row);
      ; // No surjection between secondary and primary
      if (ROW_CHECKS_ENABLED) checkState(!row.hasSecondaryFk());
    }

    // MIRNA

    // MIRNA_M
    else if (fileType == MIRNA_M) {

      // Uniqueness check
      if (primaryKeys.containsPk(row.getPk())) {
        reporter.reportUniquenessError(fileType, fileName, lineCount, row.getPk());
      }

      // Foreign key check
      if (!hasMatchingReference(optionalReferencedPrimaryKeys, row.getFk())) {
        reporter.reportRelationError(fileType, fileName, lineCount, row.getFk());
      }

      // Secondary foreign key check
      if (row.hasSecondaryFk()
          && !hasMatchingReference(
              optionalReferencedPrimaryKeys,
              row.getSecondaryFk())) {
        reporter.reportSecondaryRelationError(fileType, fileName, lineCount, row.getSecondaryFk());

      }

      primaryKeys.updatePks(fileName, row);
      // Not checking for surjection with meta files
    }

    // MIRNA_P
    else if (fileType == MIRNA_P) {
      ; // No uniqueness check for MIRNA_P (unlike for other types, the PK is on the secondary file for MIRNA)

      // Foreign key check
      if (!hasMatchingReference(optionalReferencedPrimaryKeys, row.getFk())) {
        reporter.reportRelationError(fileType, fileName, lineCount, row.getFk());

      }

      if (ROW_CHECKS_ENABLED) ensureNoPK(row);

      // FIXME: Anthony is working on the new data model that will address this issue
      // primaryKeys.updatePks(fileName, row);
      ensurePresent(optionalEncounteredKeys);
      optionalEncounteredKeys.get().addEncounteredForeignKey(row.getFk());
      if (ROW_CHECKS_ENABLED) checkState(!row.hasSecondaryFk());
    }

    // MIRNA_S
    else if (fileType == MIRNA_S) {

      // Uniqueness check (unlike for other types, the PK is on the secondary file for MIRNA)
      if (primaryKeys.containsPk(row.getPk())) {
        reporter.reportUniquenessError(fileType, fileName, lineCount, row.getPk());

      }

      // Foreign key check
      if (!hasMatchingReference(optionalReferencedPrimaryKeys, row.getFk())) {
        reporter.reportRelationError(fileType, fileName, lineCount, row.getFk());

      }

      // if (ROW_CHECKS_ENABLED) ensureNoPK(row);
      ; // No surjection between secondary and primary
      if (ROW_CHECKS_ENABLED) checkState(!row.hasSecondaryFk());
    }

    // METH

    // METH_M
    else if (fileType == METH_M) {

      // Uniqueness check
      if (primaryKeys.containsPk(row.getPk())) {
        reporter.reportUniquenessError(fileType, fileName, lineCount, row.getPk());
      }

      // Foreign key check
      if (!hasMatchingReference(optionalReferencedPrimaryKeys, row.getFk())) {
        reporter.reportRelationError(fileType, fileName, lineCount, row.getFk());
      }

      // Secondary foreign key check
      if (row.hasSecondaryFk()
          && !hasMatchingReference(
              optionalReferencedPrimaryKeys,
              row.getSecondaryFk())) {
        reporter.reportSecondaryRelationError(fileType, fileName, lineCount, row.getSecondaryFk());
      }

      primaryKeys.updatePks(fileName, row);
      // Not checking for surjection with meta files
    }

    // METH_P
    else if (fileType == METH_P) {

      // Uniqueness check
      if (primaryKeys.containsPk(row.getPk())) {
        reporter.reportUniquenessError(fileType, fileName, lineCount, row.getPk());

      }

      // Foreign key check
      if (!hasMatchingReference(optionalReferencedPrimaryKeys, row.getFk())) {
        reporter.reportRelationError(fileType, fileName, lineCount, row.getFk());

      }

      primaryKeys.updatePks(fileName, row);
      ensurePresent(optionalEncounteredKeys);
      optionalEncounteredKeys.get().addEncounteredForeignKey(row.getFk());
      if (ROW_CHECKS_ENABLED) checkState(!row.hasSecondaryFk());
    }

    // METH_S
    else if (fileType == METH_S) {
      ; // No uniqueness check for METH_s

      // Foreign key check
      if (!hasMatchingReference(optionalReferencedPrimaryKeys, row.getFk())) {
        reporter.reportRelationError(fileType, fileName, lineCount, row.getFk());

      }

      if (ROW_CHECKS_ENABLED) ensureNoPK(row);
      ; // No surjection between secondary and primary
      if (ROW_CHECKS_ENABLED) checkState(!row.hasSecondaryFk());
    }

    // EXP

    // EXP_M
    else if (fileType == EXP_M) {
      // TODO: later on, report on diff using: oldData.pksContains(row.getPk())

      // Uniqueness check
      if (primaryKeys.containsPk(row.getPk())) {
        reporter.reportUniquenessError(fileType, fileName, lineCount, row.getPk());

      }

      // Foreign key check
      if (!hasMatchingReference(optionalReferencedPrimaryKeys, row.getFk())) {
        reporter.reportRelationError(fileType, fileName, lineCount, row.getFk());

      }

      // Secondary foreign key check
      if (row.hasSecondaryFk()
          && !hasMatchingReference(
              optionalReferencedPrimaryKeys,
              row.getSecondaryFk())) {

        reporter.reportSecondaryRelationError(fileType, fileName, lineCount, row.getSecondaryFk());

      }

      primaryKeys.updatePks(fileName, row);
      // Not checking for surjection with meta files
    }

    // EXP_G
    else if (fileType == EXP_G) {
      ; // No uniqueness check for EXP_P

      // Foreign key check
      if (!hasMatchingReference(optionalReferencedPrimaryKeys, row.getFk())) {
        reporter.reportRelationError(fileType, fileName, lineCount, row.getFk());

      }

      if (ROW_CHECKS_ENABLED) ensureNoPK(row);
      ensurePresent(optionalEncounteredKeys);
      optionalEncounteredKeys.get().addEncounteredForeignKey(row.getFk());
      if (ROW_CHECKS_ENABLED) checkState(!row.hasSecondaryFk());
    }

    // PEXP

    // PEXP_M
    else if (fileType == PEXP_M) {
      // TODO: later on, report on diff using: oldData.pksContains(row.getPk())

      // Uniqueness check
      if (primaryKeys.containsPk(row.getPk())) {
        reporter.reportUniquenessError(fileType, fileName, lineCount, row.getPk());

      }

      // Foreign key check
      if (!hasMatchingReference(optionalReferencedPrimaryKeys, row.getFk())) {
        reporter.reportRelationError(fileType, fileName, lineCount, row.getFk());
      }

      // Secondary foreign key check
      if (row.hasSecondaryFk()
          && !hasMatchingReference(
              optionalReferencedPrimaryKeys,
              row.getSecondaryFk())) {
        reporter.reportSecondaryRelationError(fileType, fileName, lineCount, row.getSecondaryFk());
      }

      primaryKeys.updatePks(fileName, row);
      // Not checking for surjection with meta files
    }

    // PEXP_P
    else if (fileType == PEXP_P) {
      ; // No uniqueness check for PEXP_P

      // Foreign key check
      if (!hasMatchingReference(optionalReferencedPrimaryKeys, row.getFk())) {
        reporter.reportRelationError(fileType, fileName, lineCount, row.getFk());

      }

      if (ROW_CHECKS_ENABLED) ensureNoPK(row);
      ensurePresent(optionalEncounteredKeys);
      optionalEncounteredKeys.get().addEncounteredForeignKey(row.getFk());
      if (ROW_CHECKS_ENABLED) checkState(!row.hasSecondaryFk());
    }

    // JCN

    // JCN_M
    else if (fileType == JCN_M) {
      // TODO: later on, report on diff using: oldData.pksContains(row.getPk())

      // Uniqueness check
      if (primaryKeys.containsPk(row.getPk())) {
        reporter.reportUniquenessError(fileType, fileName, lineCount, row.getPk());
      }

      // Foreign key check
      if (!hasMatchingReference(optionalReferencedPrimaryKeys, row.getFk())) {
        reporter.reportRelationError(fileType, fileName, lineCount, row.getFk());
      }

      // Secondary foreign key check
      if (row.hasSecondaryFk()
          && !hasMatchingReference(
              optionalReferencedPrimaryKeys,
              row.getSecondaryFk())) {
        reporter.reportSecondaryRelationError(fileType, fileName, lineCount, row.getSecondaryFk());

      }

      primaryKeys.updatePks(fileName, row);
      // Not checking for surjection with meta files
    }

    // JCN_P
    else if (fileType == JCN_P) {
      ; // No uniqueness check for JCN_P

      // Foreign key check
      if (!hasMatchingReference(optionalReferencedPrimaryKeys, row.getFk())) {
        reporter.reportRelationError(fileType, fileName, lineCount, row.getFk());

      }

      if (ROW_CHECKS_ENABLED) ensureNoPK(row);
      ensurePresent(optionalEncounteredKeys);
      optionalEncounteredKeys.get().addEncounteredForeignKey(row.getFk());
      if (ROW_CHECKS_ENABLED) checkState(!row.hasSecondaryFk());
    }

    // SGV

    // SGV_M
    else if (fileType == SGV_M) {
      // TODO: later on, report on diff using: oldData.pksContains(row.getPk())

      // Uniqueness check
      if (primaryKeys.containsPk(row.getPk())) {
        reporter.reportUniquenessError(fileType, fileName, lineCount, row.getPk());
      }

      // Foreign key check
      if (!hasMatchingReference(optionalReferencedPrimaryKeys, row.getFk())) {
        reporter.reportRelationError(fileType, fileName, lineCount, row.getFk());
      }

      // Secondary foreign key check
      if (row.hasSecondaryFk()
          && !hasMatchingReference(
              optionalReferencedPrimaryKeys,
              row.getSecondaryFk())) {

        reporter.reportSecondaryRelationError(fileType, fileName, lineCount, row.getSecondaryFk());
      }

      primaryKeys.updatePks(fileName, row);
      // Not checking for surjection with meta files
    }

    // SGV_P
    else if (fileType == SGV_P) {
      ; // No uniqueness check for SGV_P

      // Foreign key check
      if (!hasMatchingReference(optionalReferencedPrimaryKeys, row.getFk())) {
        reporter.reportRelationError(fileType, fileName, lineCount, row.getFk());
      }

      if (ROW_CHECKS_ENABLED) ensureNoPK(row);
      ensurePresent(optionalEncounteredKeys);
      optionalEncounteredKeys.get().addEncounteredForeignKey(row.getFk());
      if (ROW_CHECKS_ENABLED) checkState(!row.hasSecondaryFk());
    }
  }

  /**
   * @param fk May be primary or secondary FK.
   */
  private boolean hasMatchingReference(Optional<KVPrimaryKeys> optionalReferencedPrimaryKeys, KVKey fk) {
    if (ROW_CHECKS_ENABLED) {
      checkState(
          optionalReferencedPrimaryKeys.isPresent(),
          "Referenced PKs are expected to be present for type '{}'", fileType);
    }
    return optionalReferencedPrimaryKeys.get().containsPk(fk);
  }

  private void ensureNoPK(KVRow row) {
    checkState(!row.hasPk(),
        "Row is expected to contain a PK for type '{}': '{}'", fileType, row);
  }

  private void ensurePresent(Optional<KVEncounteredForeignKeys> optionalEncounteredKeys) {
    checkState(optionalEncounteredKeys.isPresent(),
        "Encountered keys are expected to be present for type '{}'", fileType);
  }

  private void logProcessedLine(long lineCount, boolean finished) {
    log.info("'{}' lines processed" + (finished ? " (finished)" : ""), lineCount);
  }
}