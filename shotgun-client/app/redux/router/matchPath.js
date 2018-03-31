
import  pathToRegexp  from 'path-to-regexp';

const patternCache = {};
const cacheLimit = 10000;
let cacheCount = 0;

const compilePath = (pattern, options) => {
  const cacheKey = '' + options.end + options.strict + options.sensitive;
  const cache = patternCache[cacheKey] || (patternCache[cacheKey] = {});

  if (cache[pattern]) return cache[pattern];

  const keys = [];
  const re = pathToRegexp(pattern, keys, options);
  const compiledPattern = { re, keys };

  if (cacheCount < cacheLimit) {
    cache[pattern] = compiledPattern;
    cacheCount++;
  }

  return compiledPattern;
};

/**
 * Public API for matching a URL pathname to a path pattern.
 */
const matchPath = function matchPath(pathname, options) {
  if (typeof options === 'string') options = { path: options };
  const isExact = pathname === options.path;
  const match = pathname.startsWith(options.path) || isExact;
  if (!match){
    return;
  }
  return {
    match,
    isExact
  };
};

export default matchPath;
