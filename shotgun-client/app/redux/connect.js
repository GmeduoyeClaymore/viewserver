import { connectAdvanced } from './connectAdvanced';
import isEqual from './is-equal';
const getType = {};

const filterFunctions = (args) => {
  if (!args){
    return args;
  }
  const result = {};
  Object.keys(args).forEach( key => {
    const val = args[key];
    if (!(val && getType.toString.call(val) === '[object Function]'))   {
      result[key] = val;
    }
  });
  return result;
};

const selectorFactory = (ignorefuncs, selector) => (dispatch) => {
  let previousPropsForComparison = {};
  let previousProps = {};
  if (!dispatch){
    throw new Error('Dispatch must not be null');
  }
  return (nextState, nextOwnProps) => {
    ownProps = nextOwnProps;
    const propsFromSelector = selector(nextState, nextOwnProps);
    let propsForComparison = propsFromSelector;
    if (ignorefuncs){
      propsForComparison = filterFunctions(propsForComparison);
    }
    if (!isEqual(previousPropsForComparison, propsForComparison)) {
      previousPropsForComparison = propsForComparison;
      previousProps = {...propsFromSelector, dispatch};
    }
    return previousProps;
  };
};

export const connect = (mapStateToProps = (_, props) => props, ignorefuncs = true) => (component) => connectAdvanced(
  selectorFactory(ignorefuncs, mapStateToProps)
)(component);

