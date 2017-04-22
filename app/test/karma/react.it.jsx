const React = require('react');
const TestUtils = require('react-dom/lib/ReactTestUtils');
const HelloMessage = require('../../views/index.jsx');

describe('React', function() {
  it('should render', async () => {
    const component = TestUtils.renderIntoDocument(
      <HelloMessage welcomeMessage="Welcome" />
    );
  
    const h1 = TestUtils.findRenderedDOMComponentWithTag(
      component, 'div'
    );

    expect(h1.textContent)
      .to.equal("Welcome!");
  });
})