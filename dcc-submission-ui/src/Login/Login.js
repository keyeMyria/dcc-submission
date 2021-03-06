import React, { Component } from 'react';

import {observable} from 'mobx';
import {observer} from 'mobx-react';

import user from '~/user.js';
import './Login.css';

function Warning() {
  return (
    <div className="Warning">
      <a className="oicr-logo" href="http://oicr.on.ca" target="_blank">
        <img
          alt="OICR"
          width={112}
          src={require('~/assets/images/logo-oicr.png')}
        />
      </a>
      <div className="warning-content text-center">
        <strong>WARNING</strong>: This is an Ontario Institute for Cancer Research (OICR) computer system. This resource and all subsequent resources accessed from this point, including all related equipment, networks, websites, applications, databases and network devices is provided for authorized OICR use only.
        <br /><br />
        Use of this system is subject to monitoring.
        By using this system, you acknowledge that you are an authorized user and agree to protect and maintain the security, integrity and confidentiality of the system and data stored on it consistent with the OICR policies and legal requirements. Any unauthorized use of Ontario Institute for Cancer Research systems may result in disciplinary action, civil or criminal penalties.
        <br /><br />
        <a className="secondary-link" href="http://oicr.on.ca/terms-and-conditions" target="_blank">Terms and Conditions</a>
      </div>
    </div>
  );
}

@observer
class Login extends Component {

  @observable username = '';
  @observable password = '';
  @observable errorMessage = '';

  handleSubmit = async (e) => {
    e.preventDefault();
    this.errorMessage = '';
    try {
      await user.login(this.username, this.password);
    } catch (e) {
      this.errorMessage = e.message;
    }
  }

  render() {
    return (
      <div className="Login">
        <div className="login-inner">
          <h1 className="main-logo">
            <img
              alt="ICGC"
              className="icgc-logo"
              src={require('~/assets/images/logo-icgc.png')}
            />
            <span className="icgc-text">ICGC</span>
          </h1>
          <h2>Data Submission</h2>
          
          {this.errorMessage && (
            <div className="alert alert-danger">
              {this.errorMessage}
            </div>
          )}

          <form
            id="login-form"
            className="form-horizontal col-sm-3"
            onSubmit={this.handleSubmit}
          >
            <div className="form-group">
              <input
                id="username"
                autoFocus={true}
                className="form-control"
                name="username"
                placeholder="Username"
                onChange={e => {this.username = e.target.value}}
              />
            </div>
            <div className="form-group">
              <input
                id="password"
                type="password"
                className="form-control"
                name="password"
                placeholder="Password"
                onChange={e => {this.password = e.target.value}}
              />
            </div>
            <div className="form-group">
                <button type="submit" className="dcc form-control btn btn-primary">
                  Login
                </button>
            </div>
            {
              // <div className="text-right">
              //   <a
              //     href="https://calendar.google.com/calendar/embed?src=icgcportal@gmail.com&ctz=America/Toronto"
              //     target="_blank"
              //   >Calendar</a>
              // </div>
            }
          </form>
        </div>
        <Warning/>
      </div>
    );
  }
}

export default Login;