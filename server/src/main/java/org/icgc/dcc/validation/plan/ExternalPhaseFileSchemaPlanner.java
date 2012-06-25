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
import static com.google.common.base.Preconditions.checkState;

import java.util.List;
import java.util.Map;

import org.icgc.dcc.model.dictionary.FileSchema;

import cascading.flow.FlowDef;
import cascading.pipe.Merge;
import cascading.pipe.Pipe;
import cascading.tap.Tap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

class ExternalPhaseFileSchemaPlanner implements FileSchemaPlanner {

  private final Planner planner;

  private final FileSchema fileSchema;

  private final Map<String, String[]> trimmedHeads = Maps.newHashMap();

  private final List<Pipe> joinedTails = Lists.newLinkedList();

  ExternalPhaseFileSchemaPlanner(Planner plan, FileSchema fileSchema) {
    checkArgument(plan != null);
    checkArgument(fileSchema != null);
    this.planner = plan;
    this.fileSchema = fileSchema;
  }

  @Override
  public FileSchema getSchema() {
    return fileSchema;
  }

  @Override
  public PlanPhase getPhase() {
    return PlanPhase.EXTERNAL;
  }

  @Override
  public void apply(PlanElement e) {
    ExternalIntegrityPlanElement element = (ExternalIntegrityPlanElement) e;
    Pipe lhs = planner.getSchemaPlan(PlanPhase.INTERNAL, getSchema().getName()).trim(element.lhsFields());
    Pipe rhs = planner.getSchemaPlan(PlanPhase.INTERNAL, element.rhs()).trim(element.rhsFields());
    checkState(lhs != null);
    checkState(rhs != null);
    trimmedHeads.put(fileSchema.getName(), element.lhsFields());
    trimmedHeads.put(element.rhs(), element.rhsFields());
    joinedTails.add(element.join(lhs, rhs));
  }

  @Override
  public Pipe trim(String... fields) {
    return null;
  }

  @Override
  public FlowDef plan() {
    CascadingStrategy strategy = planner.getCascadingStrategy();
    if(joinedTails.size() > 0) {
      Tap sink = strategy.getExternalSinkTap(fileSchema.getName());
      FlowDef def = new FlowDef().setName(getSchema().getName() + ".ext").addTailSink(mergeJoinedTails(), sink);

      for(Map.Entry<String, String[]> e : trimmedHeads.entrySet()) {
        def.addSource(e.getKey(), strategy.getTrimmedTap(e.getKey(), e.getValue()));
      }
      return def;
    }
    return null;
  }

  private Pipe mergeJoinedTails() {
    return new Merge(joinedTails.toArray(new Pipe[] {}));
  }

}
