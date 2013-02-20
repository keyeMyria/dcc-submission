/*
 * Copyright 2013(c) The Ontario Institute for Cancer Research. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public
 * License v3.0. You should have received a copy of the GNU General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.icgc.dcc.portal.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.icgc.dcc.portal.search.SearchQuery;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static java.lang.Math.ceil;
import static java.lang.Math.floor;

@EqualsAndHashCode(callSuper = false)
@Data
public final class GetManyResponse extends BaseResponse {

  private final static ObjectMapper MAPPER = new ObjectMapper();
  private final JsonNode hits;
  private final JsonNode facets;
  private final Pagination pagination;

  public GetManyResponse(final SearchResponse response, final HttpServletRequest hsr, SearchQuery searchQuery) {
    super(hsr);
    try {
      // TODO
      System.out.println(MAPPER.readValue(response.toString(), JsonNode.class));
      System.out.println(MAPPER.readValue(response.toString(), JsonNode.class).path("hits"));
      System.out.println(MAPPER.readValue(response.toString(), JsonNode.class).path("hits").path("hits"));
      System.out
          .println(MAPPER.readValue(response.toString(), JsonNode.class).path("hits").path("hits").path("fields"));
    } catch (IOException e) {
      e.printStackTrace(); // To change body of catch statement use File | Settings | File
                           // Templates.
    }
    this.hits = extractData(response.getHits().getHits());
    this.facets = null;
    this.pagination = new Pagination(response.getHits(), searchQuery);
  }

  private JsonNode extractData(final SearchHit[] hits) {
    ArrayNode arrayNode = MAPPER.createArrayNode();
    for (SearchHit hit : hits) {
      JsonNode node;
      try {
        node = MAPPER.readValue(hit.getSourceAsString(), JsonNode.class);
      } catch (IOException e) {
        throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
            .entity(new ErrorResponse(Response.Status.BAD_REQUEST, e)).type(MediaType.APPLICATION_JSON_TYPE).build());
      }
      arrayNode.add(node);
    }
    return arrayNode;
  }

  @Data
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private static final class Pagination {
    private final int count;
    private final long total;
    private final int size;
    private final int from;
    private final double page;
    private final double pages;
    private final String sort;
    private final String order;

    public Pagination(final SearchHits hits, SearchQuery searchQuery) {
      this.count = hits.getHits().length;
      this.total = hits.getTotalHits();
      this.size = searchQuery.getSize();
      this.from = searchQuery.getFrom() + 1;
      this.sort = searchQuery.getSort();
      this.order = searchQuery.getOrder().toString().toLowerCase();
      this.page = floor(from / size) + 1;
      this.pages = ceil(total / size);
    }
  }
}
