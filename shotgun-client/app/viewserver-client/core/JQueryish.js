export const $ = {
    extend : (assign, from, to) => {
        return Object.assign({},from,to);
    },
    each : (collection, fn) => {
        if(typeof collection === 'object' && !collection.map){
            return Object.entries(collection).map((val,index) => fn(index,val[1]))
        }
        else if(Array.isArray(collection)){
            return collection.map((val,index) => fn(index,val))
        }
        throw new Error(`Unable to iterate collection "${collection}"`)
    }
}