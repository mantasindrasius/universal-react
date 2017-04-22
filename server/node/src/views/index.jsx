var React = require('react');

class HelloMessage extends React.Component {
  render() {
    return <div id="welcome-message">{this.props.welcomeMessage}!</div>;
  }
}

module.exports = HelloMessage;