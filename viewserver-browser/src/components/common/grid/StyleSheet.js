import Style from './Style';

export default {
	create : function(styles, cache) {	
		const result = {};
		for (let key in styles) {
			result[key] = cache ? 
				cache.get(styles[key]) : 
				Style.create(styles[key]);
		}
		return result;
	}
}