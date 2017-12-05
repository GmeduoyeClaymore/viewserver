import { numeric } from './Invariants';

export default class RowMetrics {
	constructor(rowHeight = 0, count = 0) {
		this._rowHeight = numeric.between(rowHeight, 'rowHeight', 0);
		this._count = numeric.between(count, 'count', 0);
	}

	rowAt(y) {
		if (y >= this.totalHeight) {
			return -2;
		}
		if (y < 0) {
			return -1;
		}
		return Math.floor(y / this._rowHeight);		
	}

	lastRowAt(y) {
		var result = this.rowAt(y);
		return result === -2 ? this.count - 1 : result;		
	}

	getStart(index) {
		return index >= 0 && index < this._count ? Math.floor(this._rowHeight * index) : -1;
	}

	getEnd(index) {
		return index >= 0 && index < this._count ? Math.floor(this._rowHeight * (index + 1)) : -1;	
	}

	getHeight(index) {
		return index >= 0 && index < this._count ? this._rowHeight : 0;
	}

	get totalHeight() {		
		return this._rowHeight * this._count;
	}

	get count() {
		return this._count;
	}			

	set count(value) {
		this._count = value;``
	}

	get rowHeight() {
		return this._rowHeight;
	}

	set rowHeight(value) {
		this._rowHeight = value;
	}
}