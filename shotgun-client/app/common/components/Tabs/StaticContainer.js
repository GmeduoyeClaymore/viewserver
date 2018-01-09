const React = require('react');

class StaticContainer extends React.Component {
  // noinspection JSAnnotator
  shouldComponentUpdate(nextProps: Object): boolean {
    return !!nextProps.shouldUpdate;
  }

  // noinspection JSAnnotator
  render(): ?ReactElement {
    const child = this.props.children;
    if (child === null || child === false) {
      return null;
    }
    return React.Children.only(child);
  }
}

module.exports = StaticContainer;
//export default StaticContainer;
