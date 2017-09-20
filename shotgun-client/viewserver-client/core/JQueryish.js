export const $ = {
    extend : (assign, from, to) => {
        return Object.assign({},from,to);
    },
    each : (collection, fn) => {
        return collection.map(fn)
    }
}