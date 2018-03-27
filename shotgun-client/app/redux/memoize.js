const NEVER_SET = {};
const defaultComparer = (arg1, arg2) => arg1 === arg2;
/**
 * Wraps the specified function such that the "last" result is cached and returned if subsequent calls
 * are made with the same parameters.
 * @example
 * function expensiveOperation(arg1, arg2) {
 *   // expensive code...
 * }
 * const operation = memoize(expensiveOperation);
 * // first call with parameters (1, 2), expensiveOperation is invoked and
 * // the result is cached.
 * let result = operation(1, 2);
 * // second call with parameters (1, 2), expensiveOperation is NOT invoked and
 * // the cached result is returned.
 * result = operation(1, 2);
 * // parameters have changed to (1, 3), expensiveOperation is invoked and
 * // the result is cached.
 * result = operation(1, 3);
 * // arguments have changed back to (1, 2), only the previous result is cached,
 * // therefore expensiveOperation is invoked again and the result is cached.
 * result = operation(1, 2);
 * // default comparer compares positional args by reference the comparer can be
 * overloaded to provide custom comparison logic for positional args
 * @param  {Function} fn   The function to memoize.
 * @param  {Function} comparer   in the format (arg,arg,index) => bool comparing positional args.
 * @return {Function}      The memoized function
 */
export const memoize = (fn, comparer) => {
  comparer = comparer || defaultComparer;
  const cachedArgs = [];
  let cachedResult = NEVER_SET;
  return (...args) => {
    // update cached args and check for state data
    let stale = false || args.length !== cachedArgs.length;
    for (let i = 0, n = args.length; i < n; i++) {
      const arg = args[i];
      let cachedArg;
      if (i < cachedArgs.length) {
        cachedArg = cachedArgs[i];
        cachedArgs[i] = arg;
      } else {
        cachedArgs.push(arg);
      }
      stale |= !comparer(cachedArg, arg, i);
    }
    cachedArgs.length = args.length;
    if (stale || cachedResult === NEVER_SET) {
      cachedResult = fn(...args);
    }
    return cachedResult;
  };
};

export default memoize;
