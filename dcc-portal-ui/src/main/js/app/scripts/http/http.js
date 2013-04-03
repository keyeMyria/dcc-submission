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

'use strict';

angular.module('app.http', ['app.http.http']);

angular.module('app.http.http', ['app.http.service']);

angular.module('app.http.http').factory('http', ['$http', 'httpService', function ($http, httpService) {
  var extractData = function (response) {
    return response.data;
  };

  return {
    get: function (url) {
      return $http.get(url).then(extractData);
    },
    query: function (url, so) {
      var filters = httpService.getCurrentFilters();

      url = url + "?filters=" + JSON.stringify(filters);
      url = so ? url + "&" + so : url;

      return $http.get(url).then(extractData);
    },
    embQuery: function (url, param) {
      var filters, query_url;
      filters = JSON.stringify(param);
      query_url = url + '?filters=' + filters;
      return $http.get(query_url).then(extractData);
    }
  };
}]);
