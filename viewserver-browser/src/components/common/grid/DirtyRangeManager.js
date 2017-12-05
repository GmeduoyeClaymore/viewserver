import Range from './Range';

function setRange(items, index, rowStart, colStart, rowEnd, colEnd) {
	items[index++] = rowStart;
	items[index++] = colStart
	items[index++] = rowEnd;
	items[index] = colEnd;		
}

function min(a, b) {
	return a < b ? a : b;
}

function max(a, b) {
	return a > b ? a : b;
}

const setRangeFromEnd = Int32Array.prototype.copyWithin &&
	function(array, targetIndex, sourceIndex) {
		array.copyWithin(targetIndex, sourceIndex, sourceIndex + 4);
	} 
	|| 
	function(array, targetIndex, sourceIndex) {
		array[targetIndex++] = array[sourceIndex++];
		array[targetIndex++] = array[sourceIndex++];
		array[targetIndex++] = array[sourceIndex++];
		array[targetIndex] = array[sourceIndex];
	};

class RangeList {
	constructor() {
		this._items = new Int32Array(128);
		this._length = 0;
	}

	get length() {
		return this._length;
	}

	set length(value) {
		if (this._length !== value) {
			this._length = value;
			this.ensureCapacity(value);			
		}
	}

	get capacity() {
		return this._items.length >> 2;
	}

	ensureCapacity(capacity) {
		if (this.capacity < capacity) {
			const newItems = new Int32Array(Math.max(capacity << 2, this.capacity * 8));
			newItems.set(this._items, 0);
			this._items = newItems;	
		}
	}

	push(rowStart, colStart, rowEnd, colEnd) {
		let index = this._length;
		this.length++;
		setRange(this._items, index << 2, rowStart, colStart, rowEnd, colEnd);
	}

	set(index, rowStart, colStart, rowEnd, colEnd) {
		setRange(this._items, index << 2, rowStart, colStart, rowEnd, colEnd);
	}

	copyWithin(targetIndex, sourceIndex) {
		setRangeFromEnd(this._items, targetIndex << 2, sourceIndex << 2);		
	}
	
	getRowStart(index) {
		return this._items[(index << 2)];
	}

	getColStart(index) {
		return this._items[(index << 2) + 1];
	}

	getRowEnd(index) {
		return this._items[(index << 2) + 2];
	}

	getColEnd(index) {
		return this._items[(index << 2) + 3];
	}

	forEach(fn) {
		const items = this._items;
		for (let i=0, n=this._length; i<n; i++) {
			const index = i << 2;
			fn(items[index], items[index + 1], items[index + 2], items[index + 3]);
		}
	}

	clear() {
		this._length = 0;		
	}
} 

function invalidate(items, rowStart, colStart, rowEnd, colEnd, searchIndex) {
	let mergeIndex = -1;
	let currRowStart = 0;
	let currRowEnd = 0;
	let currColEnd = 0;
	let currColStart = 0;
	let colIntStart = 0;
	let colIntEnd = 0;
	let rowIntStart = 0;
	let rowIntEnd = 0;
			
	while (searchIndex < items.length) {
		// get current range
		currRowStart = items.getRowStart(searchIndex);
		currColStart = items.getColStart(searchIndex);
		currRowEnd = items.getRowEnd(searchIndex);
		currColEnd = items.getColEnd(searchIndex);
		
		// get intersection points
		colIntStart = max(currColStart, colStart);
		colIntEnd = min(currColEnd, colEnd);
		rowIntStart = max(currRowStart, rowStart);
		rowIntEnd = min(currRowEnd, rowEnd);
		
		if ((currRowEnd === rowEnd && currRowStart === rowStart && colIntStart <= colIntEnd + 1) || 
			(currColEnd === colEnd && currColStart === colStart && rowIntStart <= rowIntEnd + 1)) {
			if (mergeIndex === -1) {
				mergeIndex = searchIndex;
			}
			
			// calculate merged range		
			rowStart = min(rowStart, currRowStart);
			colStart = min(colStart, currColStart);
			rowEnd = max(rowEnd, currRowEnd);
			colEnd = max(colEnd, currColEnd);

			// repace at merge index
			items.set(mergeIndex, rowStart, colStart, rowEnd, colEnd); 			
			
			// If not the first merge operation then we need to remove current,
			// the fastest way is to replace with the last item and decrement 
			// count (rather than shunt the list about).
			if (mergeIndex !== searchIndex) {
				if (searchIndex < items.length - 1) {
					items.copyWithin(searchIndex, items.length - 1);
					searchIndex--;
				}
				items.length--;				
			}
		} else if (rowIntStart <= rowIntEnd && colIntStart <= colIntEnd) {			
			if (rowStart < rowIntStart) {
				invalidate(items, rowStart, colStart, rowIntStart - 1, colEnd, searchIndex + 1);
				rowStart = rowIntStart;
			} 
			if (rowEnd > rowIntEnd) {
				invalidate(items, rowIntEnd + 1, colStart, rowEnd, colEnd, searchIndex + 1);
				rowEnd = rowIntEnd;
			}
			if (colStart < colIntStart) {
				invalidate(items, rowStart, colStart, rowEnd, colIntStart - 1, searchIndex + 1);
			}			
			if (colEnd > colIntEnd) {
				invalidate(items, rowStart, colIntEnd + 1, rowEnd, colEnd, searchIndex + 1);
			}
			return;
		}  		
		 
		searchIndex++;
	}

	if (mergeIndex === -1) {
		items.push(rowStart, colStart, rowEnd, colEnd);	
	}
}

export default class DirtyRangeManager {
	constructor() {
		this._items = new RangeList();
		this._clip = null;
	}

	/*  Applies a clipping range, any ranges invalidated 
		will be clipped to this range. 

		WARNING: if the previous clipping range is a subset of the 
		new range (e.g. the new range is larger) then previous 
		invalidate calls may have been clipped too much. This will only 
		happen when changing layout, which would generally result in a 
		full invalidate. */
	setClipping(rowStart, colStart, rowEnd, colEnd) {
		const newValue = new Range(rowStart, colStart, rowEnd, colEnd);
		const oldValue = this._clip;

		let reclip = null;
		if (oldValue) {
			if (!newValue.contains(oldValue.rowStart, oldValue.colStart, oldValue.rowEnd, oldValue.colEnd)) {
				reclip = [];
				this.flush((rowStart, colStart, rowEnd, colEnd) => {
					reclip.push([rowStart, colStart, rowEnd, colEnd]);					
				});				
			}
		}
		this._clip = newValue;

		if (reclip) {
			reclip.forEach(r => {
				this.invalidate(r[0], r[1], r[2], r[3]);
			});
		}	
	}

	setClippingRange(range) {
		if (range) {
			this.setClipping(range.rowStart, range.colStart, range.rowEnd, range.colEnd);	
		} else {
			this._clip = null;
		}		
	}

	invalidate(rowStart, colStart, rowEnd, colEnd) {
		const { _clip } = this;
		if (_clip !== null) {
			rowStart = max(_clip.rowStart, rowStart);
			colStart = max(_clip.colStart, colStart);
			rowEnd = min(_clip.rowEnd, rowEnd);
			colEnd = min(_clip.colEnd, colEnd);
		}
		invalidate(this._items, rowStart, colStart, rowEnd, colEnd, 0);
	}

	invalidateRange(range) {
		this.invalidate(range.rowStart, range.colStart, range.rowEnd, range.colEnd);	
	}

	get length() {
		return this._items.length;
	}

	clear() {
		this._items.clear();
	}

	flush(fn) {
		this._items.forEach(fn);
		this._items.clear();
	}
}

