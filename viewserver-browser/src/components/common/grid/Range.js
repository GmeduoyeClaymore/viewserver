export default class Range {
	constructor(rowStart, colStart, rowEnd, colEnd) {
		this.rowStart = rowStart;
		this.colStart = colStart;
		this.rowEnd = rowEnd;
		this.colEnd = colEnd;
	}

	equals(other) {
		// only strict comparisons are currently supported
		if (!(other instanceof Range)) {
			return false;
		}
		
		return other.rowStart === this.rowStart && 
			other.colStart === this.colStart &&
			other.rowEnd === this.rowEnd &&  
			other.colEnd === this.colEnd;		
	}

	contains(rowStart, colStart, rowEnd, colEnd) {
		return rowStart >= this.rowStart &&
			rowEnd <= this.rowEnd && 
			colStart >= this.colStart &&
			colEnd <= this.colEnd;
	}
	
	intersects(rowStart, colStart, rowEnd, colEnd) {
		return rowEnd >= this.rowStart && colEnd >= this.colStart && 
			rowStart <= this.rowEnd && colStart <= this.colEnd;				
	}

	toString() {
		return `{(${this.rowStart}, ${this.colStart}), (${this.rowEnd}, ${this.colEnd})}`;
	}	
}