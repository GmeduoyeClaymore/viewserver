import { connectAdvanced } from './connectAdvanced';
import isEqual from './is-equal';
import Logger from 'common/Logger';
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

const selectorFactory = (ignorefuncs, selector) => (dispatch, initializationProps) => {
  let previousPropsForComparison = {};
  let previousProps = {};
  return (nextState, nextOwnProps) => {
    ownProps = nextOwnProps;
    const propsFromSelector = selector(nextState, nextOwnProps);
    let propsForComparison = propsFromSelector;
    if (ignorefuncs){
      propsForComparison = filterFunctions(propsForComparison);
    }
    if (!isEqual(previousPropsForComparison, propsForComparison)) {
      previousPropsForComparison = propsForComparison;
      previousProps = propsFromSelector;
    }
    return previousProps;
  };
};

export const connect = (selector, ignorefuncs = true) => (component) => connectAdvanced(
    selectorFactory(ignorefuncs, selector)
  )(component);

