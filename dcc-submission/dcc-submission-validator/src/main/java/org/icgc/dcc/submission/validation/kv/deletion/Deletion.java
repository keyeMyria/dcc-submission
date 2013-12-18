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
package org.icgc.dcc.submission.validation.kv.deletion;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newTreeMap;
import static org.icgc.dcc.core.model.FeatureTypes.FeatureType.from;
import static org.icgc.dcc.submission.validation.kv.Helper.getToBeRemovedFile;
import static org.icgc.dcc.submission.validation.kv.KeyValidatorConstants.TAB_SPLITTER;
import static org.icgc.dcc.submission.validation.kv.deletion.Deletion.KeyValidationAdditionalType.ALL;
import static org.icgc.dcc.submission.validation.kv.deletion.Deletion.KeyValidationAdditionalType.ERROR;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Map;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.icgc.dcc.core.model.DeletionType;
import org.icgc.dcc.core.model.FeatureTypes.FeatureType;

import com.google.common.base.Splitter;

/**
 * TODO: consider having the validation be separated from the key validator?
 */
@Slf4j
public class Deletion {

  private static final Splitter FEATURE_TYPE_SPLITTER = Splitter.on(',');

  public enum KeyValidationAdditionalType implements DeletionType {
    ALL, ERROR;

    @Override
    public boolean isAllDeletionType() {
      return this == ALL;
    }

    @Override
    public boolean isErroneousDeletionType() {
      return this == ERROR;
    }

    // TODO: move to FeatureTypeDeletion?
    public static boolean matchesAllDeletionType(String value) {
      return ALL.name().equalsIgnoreCase(value);
    }
  }

  /**
   * Key-value pair format is expected to have been checked for already in the first-pass validation.
   * <p>
   * Does not perform any validation per se, simply parsing.
   */
  @SneakyThrows
  public DeletionData parseToBeDeletedFile() { // TODO: move to deletion file
    val toBeDetetedFile = getToBeRemovedFile();
    log.info("{}", toBeDetetedFile);

    // TODO: use builder
    Map<String, List<DeletionType>> deletionMap = newTreeMap();

    // TODO: "with" construct
    val reader = new BufferedReader(new FileReader(new File(toBeDetetedFile)));
    int lineCount = 0;
    for (String line; (line = reader.readLine()) != null;) {
      if (lineCount != 0 && !line.trim().isEmpty()) {
        val row = newArrayList(TAB_SPLITTER.split(line));
        log.debug("\t" + row);

        checkState(row.size() == 2, "TODO");
        String donorId = row.get(0);
        String featureTypesString = row.get(1);

        deletionMap.put(
            donorId,
            getDeletionType(
            getFeatureTypeStringList(featureTypesString)));
      }
      lineCount++;
    }
    return new DeletionData(deletionMap);
  }

  private List<String> getFeatureTypeStringList(String featureTypesString) {
    return newArrayList(FEATURE_TYPE_SPLITTER.split(
        featureTypesString
            .toLowerCase()
            .replace(" ", "")));
  }

  private List<DeletionType> getDeletionType(List<String> featureTypeStringList) {
    List<DeletionType> deletionTypes = newArrayList();
    for (val featureTypeString : featureTypeStringList) {
      boolean isAll = KeyValidationAdditionalType.matchesAllDeletionType(featureTypeString);
      if (!isAll) {
        if (FeatureType.hasMatch(featureTypeString)) {
          deletionTypes.add(from(featureTypeString));
        } else {
          deletionTypes.add(ERROR);
        }
      } else {
        deletionTypes.add(ALL);
      }
    }
    return deletionTypes;
  }
}
