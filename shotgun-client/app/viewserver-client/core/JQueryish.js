export const $ = {
  extend: (assign, from, to) => {
    return Object.assign({}, from, to);
  },
  each: (collection, fn) => {
    if (typeof collection === 'object'){
      return Object.keys(collection).map((key) => {
        return fn(key, collection[key]);
      });
    } else if (Array.isArray(collection)){
      return collection.map((val, index) => fn(index, val));
    }
    throw new Error(`Unable to iterate collection "${collection}"`);
  }
};
