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
package org.icgc.dcc.core.model;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static lombok.AccessLevel.PRIVATE;
import static org.icgc.dcc.core.model.FileSchemaNames.SubmissionFileSubType.META;
import static org.icgc.dcc.core.model.FileSchemaNames.SubmissionFileSubType.PRIMARY;
import static org.icgc.dcc.core.model.FileSchemaNames.SubmissionFileSubType.SECONDARY;

import java.util.Iterator;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;

import org.icgc.dcc.core.model.FeatureTypes.FeatureType;
import org.icgc.dcc.core.model.FileTypes.FileType;
import org.icgc.dcc.core.model.SubmissionDataType.SubmissionDataTypes;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;

/**
 * Contains names for file schemata (eg. "ssm_p", "cnsm_s", "exp_g", "N/A", ...)
 */
@NoArgsConstructor(access = PRIVATE)
public final class FileSchemaNames {

  /**
   * Used as placeholder in the loader for imported fields.
   */
  public static final String NOT_APPLICABLE = "N/A";

  /**
   * TODO: Make this association explicit rather than by convention (we shouldn't rely on prefices/suffices).
   */
  private static final String SUFFIX_SEPARATOR = "_";
  private static final Joiner JOINER = Joiner.on(SUFFIX_SEPARATOR);
  private static final Splitter SPLITTER = Splitter.on(SUFFIX_SEPARATOR);
  private static final Optional<SubmissionFileSubType> ABSENT = Optional.absent();

  /**
   * TODO: migrate all constants below to this enum (DCC-1452).
   * <p>
   * According to https://wiki.oicr.on.ca/display/DCCINT/Submission+File+Format, this would have to be called "FileType"
   * as well, like "donor", "specimen", ... This seems quite confusing however.
   */
  public enum SubmissionFileSubType {
    META,
    PRIMARY,
    SECONDARY,
    GENE,

    DONOR,
    SPECIMEN,
    SAMPLE,

    OPTIONAL;

    /**
     * See {@link #usedAsAbbrevatiation()}.
     */
    private static final List<SubmissionFileSubType> TYPES_USED_AS_ABBREVIATION =
        newArrayList(META, PRIMARY, SECONDARY, GENE);

    public String getAbbreviation() {
      checkState(usedAsAbbrevatiation(),
          "Clinical sub types do not use abbreviations, attempt was made on %s", this);
      return getFirstCharacter().toLowerCase();
    }

    /**
     * Determines whether the sub-type is used as abbreviation for further qualification (for instance "meta" is used as
     * the "_m" suffix) or not (for instance "donor").
     */
    private boolean usedAsAbbrevatiation() {
      return TYPES_USED_AS_ABBREVIATION.contains(this);
    }

    public static Optional<SubmissionFileSubType> fromAbbreviation(String abbreviation) {
      for (val item : values()) {
        if (item.getAbbreviation().equals(abbreviation)) {
          return Optional.<SubmissionFileSubType> of(item);
        }
      }
      return ABSENT;
    }

    public static SubmissionFileSubType fromFileSchemaName(String fileSchemaName) {
      return fromFileSchemaType(FileSchemaType.from(fileSchemaName));
    }

    public static SubmissionFileSubType fromFileSchemaType(FileSchemaType type) {
      Iterator<String> iterator = SPLITTER.split(type.getTypeName()).iterator();
      checkState(iterator.hasNext(), "Expecting at least one element from %s", type);
      String first = iterator.next();
      Optional<SubmissionFileSubType> subType = iterator.hasNext() ?
          fromAbbreviation(iterator.next()) :
          Optional.of(valueOf(first.toUpperCase()));
      checkState(subType.isPresent(),
          "Could not find any match for: %s", type); // TODO? we may need to address that case in the future
      return subType.get();
    }

    private String getFirstCharacter() {
      return name().substring(0, 1);
    }
  }

  /**
   * TODO: migrate all constants below to this enum (DCC-1452).
   */
  public enum FileSchemaType implements SubmissionFileType {

    SSM_M(FeatureType.SSM_TYPE, SubmissionFileSubType.META),
    SSM_P(FeatureType.SSM_TYPE, SubmissionFileSubType.PRIMARY),
    SSM_S(FeatureType.SSM_TYPE, SubmissionFileSubType.SECONDARY),

    CNSM_M(FeatureType.CNSM_TYPE, SubmissionFileSubType.META),
    CNSM_P(FeatureType.CNSM_TYPE, SubmissionFileSubType.PRIMARY),
    CNSM_S(FeatureType.CNSM_TYPE, SubmissionFileSubType.SECONDARY),

    STSM_M(FeatureType.STSM_TYPE, META),
    STSM_P(FeatureType.STSM_TYPE, PRIMARY),
    STSM_S(FeatureType.STSM_TYPE, SECONDARY),

    SGV_M(FeatureType.SGV_TYPE, SubmissionFileSubType.META),
    SGV_P(FeatureType.SGV_TYPE, SubmissionFileSubType.PRIMARY),

    CNGV_M(FeatureType.CNGV_TYPE, SubmissionFileSubType.META),
    CNGV_P(FeatureType.CNGV_TYPE, SubmissionFileSubType.PRIMARY),
    CNGV_S(FeatureType.CNGV_TYPE, SubmissionFileSubType.SECONDARY),

    STGV_M(FeatureType.STGV_TYPE, SubmissionFileSubType.META),
    STGV_P(FeatureType.STGV_TYPE, SubmissionFileSubType.PRIMARY),
    STGV_S(FeatureType.STGV_TYPE, SubmissionFileSubType.SECONDARY),

    PEXP_M(FeatureType.PEXP_TYPE, SubmissionFileSubType.META),
    PEXP_P(FeatureType.PEXP_TYPE, SubmissionFileSubType.PRIMARY),

    METH_M(FeatureType.METH_TYPE, SubmissionFileSubType.META),
    METH_P(FeatureType.METH_TYPE, SubmissionFileSubType.PRIMARY),
    METH_S(FeatureType.METH_TYPE, SubmissionFileSubType.SECONDARY),

    MIRNA_M(FeatureType.MIRNA_TYPE, SubmissionFileSubType.META),
    MIRNA_P(FeatureType.MIRNA_TYPE, SubmissionFileSubType.PRIMARY),
    MIRNA_S(FeatureType.MIRNA_TYPE, SubmissionFileSubType.SECONDARY),

    JCN_M(FeatureType.JCN_TYPE, SubmissionFileSubType.META),
    JCN_P(FeatureType.JCN_TYPE, SubmissionFileSubType.PRIMARY),

    EXP_M(FeatureType.EXP_TYPE, SubmissionFileSubType.META),
    EXP_G(FeatureType.EXP_TYPE, SubmissionFileSubType.GENE),

    DONOR(FileType.DONOR_TYPE, SubmissionFileSubType.DONOR),
    SPECIMEN(FileType.SPECIMEN_TYPE, SubmissionFileSubType.SPECIMEN),
    SAMPLE(FileType.SAMPLE_TYPE, SubmissionFileSubType.SAMPLE),

    BIOMARKER(FileType.BIOMARKER_TYPE, SubmissionFileSubType.OPTIONAL),
    FAMILY(FileType.FAMILY_TYPE, SubmissionFileSubType.OPTIONAL),
    EXPOSURE(FileType.EXPOSURE_TYPE, SubmissionFileSubType.OPTIONAL),
    SURGERY(FileType.SURGERY_TYPE, SubmissionFileSubType.OPTIONAL),
    THERAPY(FileType.THERAPY_TYPE, SubmissionFileSubType.OPTIONAL);

    private FileSchemaType(SubmissionDataType type) {
      this(type, null);
    }

    private FileSchemaType(SubmissionDataType type, SubmissionFileSubType subType) {
      this.type = checkNotNull(type);
      this.subType = subType;
    }

    @Override
    public String getTypeName() {
      return subType.usedAsAbbrevatiation() ?
          JOINER.join(type.getTypeName(), subType.getAbbreviation()) :
          type.getTypeName();
    }

    @Getter
    private final SubmissionDataType type;

    @Getter
    private final SubmissionFileSubType subType;

    /**
     * Returns an enum matching the type like "ssm_p", "meth_s", ...
     */
    public static FileSchemaType from(FeatureType type, SubmissionFileSubType subType) {
      return FileSchemaType.from(JOINER.join(type.getTypeName(), subType.getAbbreviation()));
    }

    /**
     * Returns an enum matching the type like "ssm_p", "meth_s", ...
     * <p>
     * TODO: favor the one below
     */
    public static FileSchemaType from(String typeName) {
      return valueOf(typeName.toUpperCase());
    }

    /**
     * Returns the corresponding {@link SubmissionDataType} (like SSM or DONOR) for the given {@link FileSchemaType}
     * (like SSM_M or DONOR).
     */
    public SubmissionDataType getSubmissionDataType() {
      val iterator = SPLITTER.split(name()).iterator();
      checkState(iterator.hasNext(), "Invalid %s: '%s'", FileSchemaType.class.getSimpleName(), name());
      String submissionDataTypeName = iterator.next();
      return SubmissionDataTypes.fromTypeName(submissionDataTypeName);
    }

  }
}
