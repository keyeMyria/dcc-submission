import React, { Component, PropTypes } from 'react';
import {observer} from 'mobx-react';
import Modal from 'react-modal';

@observer
class ResetModal extends Component {
  static propTypes = {
    isOpen: PropTypes.bool.isRequired,
    onRequestClose: PropTypes.func.isRequired,
    onRequestSubmit: PropTypes.func.isRequired,
    projectName: PropTypes.string.isRequired,
  };

  render () {
    const { isOpen, onRequestClose, onRequestSubmit, projectName } = this.props;
    return (
      <Modal
          className={`Modal modal-dialog`}
          isOpen={isOpen}
          onRequestClose={onRequestClose}
          closeTimeoutMS={250}
          shouldCloseOnOverlayClick={true}
        >
        <div className="modal-container">
          <div className="modal-header">
            <button
              type="button"
              className="close"
              aria-label="Close"
              onClick={onRequestClose}
            >
              <span aria-hidden="true">&times;</span>
            </button>
            <h3>Reset Submission</h3>
          </div>
          <div className="modal-body">
            <div className="alert alert-info">
              This will reset the validation on the <strong>{projectName}</strong> submission!
            </div>
          </div>
          <div className="modal-footer">
            <button className="btn btn-default" onClick={onRequestClose}>Close</button>
            <button
              type="submit"
              className="btn btn-danger"
              onClick={onRequestSubmit}
            >Reset Submission</button>
          </div>
        </div>
      </Modal>
    );
  }
}

export default ResetModal;