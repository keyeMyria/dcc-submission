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
package org.icgc.dcc.submission.validation.visitor;

import static com.google.common.collect.Maps.newHashMap;
import static org.icgc.dcc.submission.validation.cascading.TupleStates.keepInvalidTuplesFilter;
import static org.icgc.dcc.submission.validation.cascading.ValidationFields.STATE_FIELD;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import lombok.Cleanup;

import org.codehaus.jackson.map.MappingIterator;
import org.codehaus.jackson.map.ObjectMapper;
import org.icgc.dcc.submission.dictionary.model.FileSchema;
import org.icgc.dcc.submission.validation.PlanExecutionException;
import org.icgc.dcc.submission.validation.cascading.TupleState;
import org.icgc.dcc.submission.validation.core.FlowType;
import org.icgc.dcc.submission.validation.core.ReportingPlanElement;
import org.icgc.dcc.submission.validation.core.ErrorCode;
import org.icgc.dcc.submission.validation.platform.PlatformStrategy;
import org.icgc.dcc.submission.validation.report.Outcome;
import org.icgc.dcc.submission.validation.report.ReportCollector;
import org.icgc.dcc.submission.validation.report.SchemaReport;
import org.icgc.dcc.submission.validation.report.ErrorReport;

import cascading.pipe.Each;
import cascading.pipe.Pipe;
import cascading.pipe.assembly.Retain;

public class ErrorPlanningVisitor extends ReportingFlowPlanningVisitor {

  public static final int MAX_ERROR_COUNT = 50;

  public ErrorPlanningVisitor(FlowType type) {
    super(type);
  }

  @Override
  public void visit(FileSchema fileSchema) {
    super.visit(fileSchema);
    collect(new ErrorsPlanElement(fileSchema, this.getFlowType()));
  }

  static class ErrorsPlanElement implements ReportingPlanElement {

    private final FileSchema fileSchema;

    private final FlowType flowType;

    public ErrorsPlanElement(FileSchema fileSchema, FlowType flowType) {
      this.fileSchema = fileSchema;
      this.flowType = flowType;
    }

    @Override
    public String getName() {
      return "errors";
    }

    @Override
    public String describe() {
      return String.format("errors");
    }

    @Override
    public Pipe report(Pipe pipe) {
      return new Retain(new Each(pipe, keepInvalidTuplesFilter()), STATE_FIELD);
    }

    public FileSchema getFileSchema() {
      return this.fileSchema;
    }

    public FlowType getFlowType() {
      return this.flowType;
    }

    @Override
    public ReportCollector getCollector() {
      return new ErrorReportCollector();
    }

    class ErrorReportCollector implements ReportCollector {

      private final Map<ErrorCode, ErrorReport> errorMap = newHashMap();

      public ErrorReportCollector() {
      }

      @Override
      public Outcome collect(PlatformStrategy strategy, SchemaReport report) {
        try {
          @Cleanup
          InputStream src = strategy.readReportTap(getFileSchema(), getFlowType(), getName());

          report.setName(strategy.path(getFileSchema()).getName());

          ObjectMapper mapper = new ObjectMapper();

          Outcome outcome = Outcome.PASSED;
          MappingIterator<TupleState> tupleStates = mapper.reader().withType(TupleState.class).readValues(src);
          while (tupleStates.hasNext()) {
            TupleState tupleState = tupleStates.next();
            if (tupleState.isInvalid()) {
              outcome = Outcome.FAILED;
              for (TupleState.TupleError error : tupleState.getErrors()) {
                if (errorMap.containsKey(error.getCode()) == true) {
                  ErrorReport errorReport = errorMap.get(error.getCode());
                  errorReport.updateReport(error);
                } else {
                  errorMap.put(error.getCode(), new ErrorReport(error));
                }
              }
            }
          }

          for (ErrorReport e : errorMap.values()) {
            e.updateLineNumbers(strategy.path(getFileSchema()));
            report.addError(e);
          }
          return outcome;
        } catch (FileNotFoundException fnfe) {
          return Outcome.PASSED;
        } catch (IOException e) {
          throw new PlanExecutionException(e);
        }
      }
    }
  }

}
