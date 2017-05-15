const React = require('React');
//const ReactReconciler = require('react-dom/lib/ReactReconciler');
const ReactFiberReconciler = require('react-dom/lib/ReactFiberReconciler');
const expect = require('chai').expect;
const emptyObject = require('fbjs/lib/emptyObject');

describe('React', function() {
  it('handle click with dummy renderer', () => {
    let context = DummyRenderer.render(ComponentUnderTest);

    context.click();

    expect(context.getState().isToggleOn).to.be.true;
  });

  it('render with reconciler', async() => {
    let rendered = await DummyReconcilerRenderer.render(ComponentUnderTest);
    
    expect(rendered.type).to.equal('a');
  });
});

class ComponentUnderTest extends React.Component {
  constructor(props) {
    super(props);

    this.state = {isToggleOn: false};
    this.handleClick = this.handleClick.bind(this);
  }

  componentDidMount() {
    console.log('Mount!');
  }

  render() {
    console.log('Render!');

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

var incompleteCallbacks = 0;

function waitDone() {
  return new Promise(completer);
}

function completer(fulfill) {
  if (incompleteCallbacks == 0) {
    fulfill();
  } else {
    process.nextTick(() => completer(fulfill));
  }
}

const DummyReconcilerRenderer = new function() {
  var rootContainer = null;
  
  const container = {
    id: 'ze-container',
    result: []
  };

  this.render = async (element) => {
    //let element = new Component({});

    rootContainer = DummyFiberReconciler.createContainer(container);
    
    DummyFiberReconciler.updateContainer(<a>Hi</a>, rootContainer);

    await waitDone();
    
    return container.result[0];
  }

  this.unmountComponentAtNode = (container) => {
    var root = rootContainer;
    if (root) {
      // TODO: Is it safe to reset this now or should I wait since this
      // unmount could be deferred?
      rootContainer = null;
      DummyFiberReconciler.unmountContainer(root);
    }
  }
}

// const DummyReconcilerRenderer = {
//   create: (element) => {
//     return ReactTestMount.render(element, options);
//   },
//   /* eslint-disable camelcase */
//   unstable_batchedUpdates: ReactUpdates.batchedUpdates,
//   /* eslint-enable camelcase */
// };

const DummyFiberReconciler = ReactFiberReconciler({
  getRootHostContext() {
    console.log('getRootHostContext');
    
    return emptyObject;
  },
  updateContainer(container, children) {    
    console.log('Update container', container, children);
  },
  // commitUpdate(instance, oldProps, newProps, children) {
  // },
  appendChild(parentInstance, child) {
    console.log('appendChild', parentInstance, child);

    parentInstance.result.push(child);
  },
  beginUpdate() {
    console.log('beginUpdate');
  },
  appendInitialChild(parentInstance, child) {
    console.log('appendInitialChild', parentInstance, child);

    if (typeof child === 'string') {
      // Noop for string children of Text (eg <Text>{'foo'}{'bar'}</Text>)
      return;
    }
  },

  commitTextUpdate(textInstance, oldText, newText) {
    console.log('commitTextUpdate', textInstance, oldText, newText);
    // Noop
  },

  commitUpdate(instance, type, oldProps, newProps) {
    console.log('Commit update', instance, oldProps, newProps, children);

    instance._applyProps(instance, newProps, oldProps);
  },

  createInstance(type, props, internalInstanceHandle) {
    console.log('createInstance', type, props, internalInstanceHandle);

    return {
      type, props
    };
  },

  createTextInstance(text, rootContainerInstance, internalInstanceHandle) {
    console.log('createTextInstance', text, rootContainerInstance, internalInstanceHandle);
    return text;
  },

  finalizeInitialChildren(domElement, type, props) {
    console.log('finalizeInitialChildren', domElement, type, props);
    // Noop
  },

  insertBefore(parentInstance, child, beforeChild) {
    console.log('insertBefore', parentInstance, child, beforeChild);
    invariant(
      child !== beforeChild,
      'ReactART: Can not insert node before itself'
    );

    child.injectBefore(beforeChild);
  },

  prepareForCommit() {
    console.log('prepareForCommit');
    // Noop
  },

  prepareUpdate(domElement, type, oldProps, newProps) {
    console.log('prepareUpdate', domElement, type, oldProps, newProps);
    return true;
  },

  removeChild(parentInstance, child) {
    console.log('removeChild', parentInstance, child);

    destroyEventListeners(child);

    child.eject();
  },

  resetAfterCommit() {
    console.log('resetAfterCommit');
    // Noop
  },

  resetTextContent(domElement) {
    console.log('resetTextContent', domElement);
    // Noop
  },

  getChildHostContext() {
    console.log('getChildHostContext');
    return null;
  },

  scheduleDeferredCallback(callback) {
    incompleteCallbacks++;

    process.nextTick(() => {
      try {
        callback({
          didTimeout: true,
          timeRemaining() {
            return 100;
          }
        });
      } finally {
        incompleteCallbacks--;
        console.log('Work done, remaining', incompleteCallbacks);
      }
    });
  },

  shouldSetTextContent(props) {
    console.log('shouldSetTextContent', props);

    return (
      typeof props.children === 'string' ||
      typeof props.children === 'number'
    );
  },

  useSyncScheduling: true,
});

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