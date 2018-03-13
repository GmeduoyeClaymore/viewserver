import { connectAdvanced } from './connectAdvanced';
import isEqual from './is-equal';


const selectorFactory = (ignorefuncs, selector) => (dispatch) => {
  let previousPropsForComparison = {};
  let previousProps = {};
  if (!dispatch){
    throw new Error('Dispatch must not be null');
  }
  return (nextState, nextOwnProps) => {
    ownProps = nextOwnProps;
    const propsFromSelector = selector(nextState, {...nextOwnProps, dispatch});
    const propsForComparison = propsFromSelector;
    
    if (!isEqual(previousPropsForComparison, propsForComparison, false, ignorefuncs)) {
      previousPropsForComparison = propsForComparison;
      previousProps = {...propsForComparison, dispatch};
    }
    return previousProps;
  };
};

export const connect = (mapStateToProps = (_, props) => props, ignorefuncs = true) => (component) => connectAdvanced(
  selectorFactory(ignorefuncs, mapStateToProps)
)(component);

