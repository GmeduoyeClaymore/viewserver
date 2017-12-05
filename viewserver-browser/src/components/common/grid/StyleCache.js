import Style from './Style';

let global = null;

/* we now want to get 
   styles based on
   an id chain */


const StyleCache = {
	create() {
		const kMap = new Map();
		const iMap = new Map();
		
		return {
			get(style) {
				const key = Style.key(style);
				
				let result = kMap.get(key);
				if (!result) {
					result = Style.create(style);
					kMap.set(key, result);
					iMap.set(result.id, result);			
				}
				return result;				
			},

			getOverrideById(baseStyleId, overrideStyleId) {
				// create a key representing the two merged styles
				const overrideChainId = baseStyleId + ':' + overrideStyleId;
				
				// lookup the style
				let style = iMap.get(overrideChainId);
				if (style) {
					return style;
				}

				// style is not known by the chain id (the merges style may still be in the cache though so use get)
				style = this.get(this.getById(baseStyleId).override(this.getById(overrideStyleId)));
				
				// cache by chain key
				iMap.set(overrideChainId, style)				
				
				// done!!!
				return style;
			},
			
			getById(id) {
				const style = iMap.get(id);
				if (!style) {
					throw new Error(`Style with id ${id} is not in the cache.`);
				}
				return style;
			},

			hasId(id) {
				return iMap.has(id);
			}
		};
	},

	get global() {
		return global || (global = StyleCache.create());
	}
};

export default StyleCache;