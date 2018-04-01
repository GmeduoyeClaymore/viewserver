

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
