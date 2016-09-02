import React, { Component } from 'react';
import {observable} from 'mobx';
import {observer} from 'mobx-react';

import { fetchHeaders } from '~/utils';

const ErrorsDict = {
  _default: 'An error occurred. Please contact Support for assistance.'
};

async function validateSubmission ({projectKey, dataTypes, emails}) {
  const response = await fetch('/ws/nextRelease/queue', {
    method: 'POST',
    headers: {
      ...fetchHeaders.get(),
    },
    body: JSON.stringify([
      {
        dataTypes: dataTypes,
        emails: emails,
        key: projectKey
      }
    ]),
  });

  const responseData = await response.json();
  if (!response.ok) {
    console.error('response not ok', responseData);
    throw new Error(ErrorsDict[responseData.code] || ErrorsDict._default);
  }
}

@observer
class ValidateModal extends Component {

  @observable errorMessage = null;

  handleClickSubmit = async () => {
    console.log('submit');
    try {
      await validateSubmission();
      this.props.onSuccess();
    } catch (e) {
      this.errorMessage = e.message; 
    }
  }

  render () {
    const { onClickClose } = this.props;
    const email = 'asdf@asdf';
    const queueLength = 5;
    return (
      <div className="modal-container">
        <div className="modal-header">
          <button
            type="button"
            className="close"
            aria-label="Close"
            onClick={onClickClose}
          >
            <span aria-hidden="true">&times;</span>
          </button>
          <h3>Validate Submission</h3>
        </div>
        <div className="modal-body">
          <table id="validate-file-types" className="table table-condensed">
            <tfoot>
              <tr>
                <td colspan="2">
                  <button type="button" className="m-btn mini blue-stripe"> Select All</button>
                  <button type="button" className="m-btn mini blue-stripe"> Clear</button>
                </td>
              </tr>
            </tfoot>
          </table>
        </div>
        <div>
          <div className="alert alert-danger">Validation may take several hours to complete!</div>
          <div className="alert alert-info">
            There are currently <strong>{{queueLength}}</strong> submission(s) in the Validation Queue.
            <br/>
            Enter a comma(,) seperated list of the email addresses that should be notified when validation is finished:
            <br/><br/>
            <textarea autofocus="autofocus" style="width:100%" className="m-wrap" id="emails">{email}</textarea>
          </div>
        </div>
        <div className="modal-footer">
          <button className="m-btn grey-stripe" onClick={onClickClose}>Close</button>
          <button
            type="submit"
            className="m-btn blue"
            onClick={this.handleClickSubmit}
          >Validate Submission</button>
        </div>
      </div>
    );
  }
}

export default ValidateModal;