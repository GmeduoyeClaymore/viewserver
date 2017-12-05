function binarySearch(array, item, compare) {
	var m = 0;
	var n = (array.length / 2) - 1;
	while (m <= n) {
    	var k = (n + m) >> 1;
    	var cmp = compare(item, array[k<<1], array[(k << 1) + 1]);
    	if (cmp > 0) {
        	m = k + 1;
    	} else if(cmp < 0) {
        	n = k - 1;
    	} else {
        	return k;
    	}
	}
	return - 1;
}

export default class ColumnMetrics {
	constructor(columns) {
		const count = columns ? columns.length : 0;
		const metrics = new Int32Array(count << 1);
		
		let offset = 0
		let index = 0;
		for (var colIndex=0; colIndex<count; colIndex++) {
			metrics[index++] = offset;
			offset += (metrics[index++] = columns[colIndex].width);			 
		}

		this._metrics = metrics;
		this._totalWidth = offset;
	}

	getStart(index) {
		return index >= 0 && index < this.count ? this._metrics[(index << 1)] : -1;
	}

	getEnd(index) {
		if (index >= 0 && index < this.count) {
			index = index << 1;
			return this._metrics[index] + this._metrics[index + 1];
		}
		return -1;
	}

	getWidth(index) {
		return index >= 0 && index < this.count ? this._metrics[(index << 1) + 1] : 0;
	}

	setWidth(index, width) {
		const metrics = this._metrics;
		if (index >= 0 && index < this.count && this.getWidth(index) !== width) {
			index = index << 1;
			// calculate reflow offset
			let offset = metrics[index++] + width;
			// update width
			metrics[index++] = width;
			// reflow
			while (index<metrics.length) {
				metrics[index++] = offset;
				offset += metrics[index++]; 
			}	
			this._totalWidth = offset;
			return true;
		}
		return false;
	}

	get totalWidth() {
		return this._totalWidth;
	}

	get count() {
		return this._metrics.length >> 1;
	}
	
	toArray() {
		const result = [];
		const metrics = this._metrics;
		for (var i=0, n=this.count * 2; i<n; i+=2) {
			result.push({
				offset: metrics[i],
				width: metrics[i + 1]
			});
		}
		return result;
	}

	colAt(x) {
		if (x >= this.totalWidth) {
			return -2;
		}
		if (x < 0) {
			return -1;
		}
		return binarySearch(this._metrics, x, (x, offset, width) => {
			if (x < offset) {
				return -1;
			}
			return x >= offset + width ? 1 : 0;			
		});		
	}

	lastColAt(x) {
		var result = this.colAt(x);
		return result === -2 ? this.count - 1 : result;		
	}
}
