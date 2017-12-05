export default class Cell {
	constructor(columnIndex = -1, rowIndex = -1) {
		this.columnIndex = columnIndex;
		this.rowIndex = rowIndex;
	}

	update(columnIndex, rowIndex) {
		const result = this.equals(columnIndex, rowIndex) ? null : new Cell(this.columnIndex, this.rowIndex);
		if (result) {
			this.columnIndex = columnIndex;
			this.rowIndex = rowIndex;				
		}
		return result;
	}

	clear() {
		this.columnIndex = -1;
		this.rowIndex = -1;
	}

	equals(columnIndex, rowIndex) {
		return this.columnIndex === columnIndex && this.rowIndex === rowIndex;	
	}

	get isValid() {
		return this.rowIndex >= 0 && this.columnIndex >= 0;
	}

	toString() {
		return `(${this.rowIndex}, ${this.columnIndex})`;
	}
}