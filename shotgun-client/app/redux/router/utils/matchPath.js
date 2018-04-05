

/**
 * Public API for matching a URL pathname to a path pattern.
 */

const normalise = function(path){
  if (!path.endsWith('/')){
    path = `${path}/`;
  }
  if (!path.startsWith('/')){
    path = `/${path}`;
  }
  return path;
};
const matchPath = function matchPath(pathname, options) {
  if (typeof options === 'string') options = { path: options };
  const normalisedPath = normalise(pathname);
  const normalisedOptionsPath = normalise(options.path);
  const isExact = normalisedPath === normalisedOptionsPath;
  const match = normalisedPath.startsWith(normalisedOptionsPath) || isExact;
  if (!match){
    return;
  }
  return {
    match,
    matchPath: pathname,
    isExact
  };
};

export default matchPath;
