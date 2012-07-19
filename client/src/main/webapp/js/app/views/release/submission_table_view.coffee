define (require) ->
  DataTableView = require 'views/base/data_table_view'
  template = require 'text!views/templates/release/submissions_table.handlebars'
  utils = require 'lib/utils'
  
  'use strict'

  class SubmissionTableView extends DataTableView
    template: template
    template = null
    autoRender: true
    
    container: '#submissions-table'
    containerMethod: 'html'
    tagName: 'table'
    className: "submissions table table-striped"
    id: "submissions"
    
    initialize: ->
      console.debug "SubmissionsTableView#initialize", @collection, @el
      super
      
      @subscribeEvent "completeRelease", @update
    
    createDataTable: (collection) ->
      console.debug "SubmissionsTableView#createDataTable"
      aoColumns = [
          {
            sTitle: "Name"
            mDataProp: "name"
            fnRender: (oObj, sVal) ->
              "<a href='/releases/#{sVal}'>#{sVal}</a>"
          }
          { sTitle: "State", mDataProp: "state" }
          {
            sTitle: "Release Date"
            mDataProp: "releaseDate"
            fnRender: (oObj, sVal) ->
              if sVal
                utils.date(sVal)
              else
                if utils.is_admin
                  """
                    <button
                      class="btn btn-mini btn-primary"
                      id="complete-release-popup-button"
                      data-toggle="modal"
                      href="#complete-release-popup">
                      Release Now
                    </button>
                  """
                else
                  "<em>Unreleased</em>"
          }
          { sTitle: "Projects", mDataProp: "submissions.length" }
        ]
      
      @.$('table').dataTable
        sDom:
          "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>"
        sPaginationType: "bootstrap"
        oLanguage:
          "sLengthMenu": "_MENU_ releases per page"
        aaSorting: [[ 2, "desc" ]]
        aoColumns: aoColumns
        sAjaxSource: ""
        sAjaxDataProp: ""
        fnServerData: (sSource, aoData, fnCallback) ->
          fnCallback collection.toJSON()
