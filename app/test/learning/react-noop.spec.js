const React = require('React');
const expect = require('chai').expect;

describe('React', function() {
  it('handle click with dummy renderer', () => {
    let context = DummyRenderer.render(ComponentUnderTest);

    context.click();

    expect(context.getState().isToggleOn).to.be.true;
  });
});

class ComponentUnderTest extends React.Component {
  constructor(props) {
    super(props);

    this.state = {isToggleOn: false};
    this.handleClick = this.handleClick.bind(this);
  }

  componentDidMount() {
  }

  render() {
    return <div>
      <span prop="1">Hello 1</span>
      <span prop="2">Hello 2</span>
      <span onClick={this.handleClick}>Hello 2</span>
    </div>
  }

  handleClick() {
    this.setState(prevState => ({
      isToggleOn: !prevState.isToggleOn
    }));
  }
};

const DummyRenderer = new function() {
  this.render = (Component) => {
    let props = {};
    let context = {};
    let updater = this;
    let element = new Component(props);

    if (element.componentWillMount) element.componentWillMount();

    element.context = context;
    element.updater = this;

    let rendered = element.render();

    if (element.componentDidMount) element.componentDidMount();

    let onClick = rendered.props.children[2];

    return {
      click: rendered.props.children[2].props.onClick,
      getState: () => element.state
    };
  };

  this.unmountComponentAtNode = function(container) {
  }

  this.enqueueSetState = function(element, newStateFn) {
    let newState = newStateFn(element.state);

    console.log('setState invoked');

    element.state = newState;
  }
}