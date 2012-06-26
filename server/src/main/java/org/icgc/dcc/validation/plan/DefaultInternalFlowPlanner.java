/**
 * Copyright 2012(c) The Ontario Institute for Cancer Research. All rights reserved.
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
package org.icgc.dcc.validation.plan;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Map;

import org.icgc.dcc.model.dictionary.FileSchema;
import org.icgc.dcc.validation.cascading.AddValidationFieldsFunction;
import org.icgc.dcc.validation.cascading.TupleStates;
import org.icgc.dcc.validation.cascading.ValidationFields;

import cascading.flow.FlowDef;
import cascading.pipe.Each;
import cascading.pipe.Pipe;
import cascading.pipe.assembly.Retain;
import cascading.tap.Tap;
import cascading.tuple.Fields;

import com.google.common.collect.Maps;

class DefaultInternalFlowPlanner implements InternalFlowPlanner {

  private final Planner planner;

  private final FileSchema fileSchema;

  private final Pipe head;

  private final Map<Trim, Pipe> trimmedTails = Maps.newHashMap();

  private Pipe validTail;

  DefaultInternalFlowPlanner(Planner plan, FileSchema fileSchema) {
    checkArgument(plan != null);
    checkArgument(fileSchema != null);
    this.planner = plan;
    this.fileSchema = fileSchema;
    this.validTail = this.head = new Pipe(fileSchema.getName());

    this.validTail = applySystemPipes(this.validTail);
  }

  @Override
  public FileSchema getSchema() {
    return fileSchema;
  }

  @Override
  public void apply(PlanElement element) {
    checkArgument(element != null);
    validTail = ((InternalPlanElement) element).extend(validTail);
  }

  @Override
  public PlanPhase getPhase() {
    return PlanPhase.INTERNAL;
  };

  @Override
  public Trim addTrimmedOutput(String... fields) {
    checkArgument(fields != null);
    checkArgument(fields.length > 0);
    Trim trim = new Trim(fileSchema.getName(), fields);
    if(trimmedTails.containsKey(trim) == false) {
      Pipe newHead = new Pipe(trim.getName(), validTail);
      Pipe tail = new Retain(newHead, new Fields(fields));
      trimmedTails.put(trim, tail);
    }
    return trim;
  }

  @Override
  public FlowDef plan() {
    CascadingStrategy strategy = planner.getCascadingStrategy();
    Pipe tail = applyFilter(validTail);
    Tap source = strategy.getSourceTap(fileSchema);
    Tap sink = strategy.getInternalSinkTap(fileSchema.getName());

    FlowDef def = new FlowDef().setName(getSchema().getName() + ".int").addSource(head, source).addTailSink(tail, sink);
    for(Map.Entry<Trim, Pipe> e : trimmedTails.entrySet()) {
      def.addTailSink(e.getValue(), strategy.getTrimmedTap(e.getKey()));
    }
    return def;
  }

  private Pipe applySystemPipes(Pipe pipe) {
    return new Each(pipe, new AddValidationFieldsFunction(), Fields.ALL);
  }

  private Pipe applyFilter(Pipe pipe) {
    return new Retain(new Each(pipe, TupleStates.keepInvalidTuplesFilter()), ValidationFields.STATE_FIELD);
  }

}
