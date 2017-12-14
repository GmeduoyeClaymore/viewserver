import ViewPort from './ViewPort';
import ColumnMetrics from './ColumnMetrics';
import RowMetrics from './RowMetrics';
import Rect from './Rect';
import Range from './Range';

export default class GridViewPort extends ViewPort {
	constructor(width, height, columnMetrics, rowMetrics) {
		super(width, height);
		this.columnMetrics = columnMetrics;
		this.rowMetrics = rowMetrics;
	}

	applyUpdate(options) {
		super.applyUpdate(options);
		
		const { columnMetrics, rowMetrics } = options;

		if (columnMetrics && this.columnMetrics !== columnMetrics) {
			this.columnMetrics = columnMetrics;
			this.setModified();
		}

		if (rowMetrics && this.rowMetrics !== rowMetrics) {
			this.rowMetrics = rowMetrics;
			this.setModified();
		}		
	}
	
	cellFromPoint(x, y) {
		const { rowMetrics, columnMetrics } = this;
		
		// values are either -1 (before), -2 (after), or on a cell
		let columnIndex = -1; 
		if (x >= (this.width + this.left)) {
			columnIndex = -2;
		} else if (x >= 0) {
			columnIndex = columnMetrics.colAt(x);
		}

		let rowIndex = -1;
		if (y >= (this.height + this.top)) {
			rowIndex = -2;
		} else if (y >= 0) {
			rowIndex = rowMetrics.rowAt(y);
		}
		
		return {
			rowIndex,
			columnIndex
		};
	}

	cellFromClientPoint(x, y) {
		return this.cellFromPoint(x + this.left, y + this.top);
	}

	rangeFromRect(rect) {
		const { rowMetrics, columnMetrics } = this;

		// grab range locations
		const rowStart = rowMetrics.rowAt(rect.top);
		if (rowStart < 0) {
			return null;
		}
		
		const colStart = columnMetrics.colAt(rect.left);
		if (colStart < 0) {
			return null;
		}
		
		const rowEnd = rowMetrics.lastRowAt(rect.bottom - 1);
		if (rowEnd < 0) {
			return null;
		}
		
		const colEnd = columnMetrics.lastColAt(rect.right - 1);
		if (colEnd < 0) {
			return null;
		}

		return new Range(rowStart, colStart, rowEnd -1, colEnd);			
	}

	rangeFromClientRect(rect) {
		rect = this.rectToView(rect || this.clientRect);
		return this.rangeFromRect(rect);
	}

	rectFromCell(rowIndex, colIndex) {
		return this.rectFromRange(rowIndex, colIndex, rowIndex, colIndex);
	}
	
	clientRectFromCell(rowIndex, colIndex) {
		return this.clientRectFromRange(rowIndex, colIndex, rowIndex, colIndex);		
	}

	rectFromRange(rowStart, colStart, rowEnd, colEnd) {
		const { columnMetrics, rowMetrics } = this;
		
		// verify its in range (should help track down defects)
		if (rowStart < 0 || colStart < 0 || colEnd > columnMetrics.count - 1 || rowEnd > rowMetrics.count - 1) {
			throw new Error('Range is not within current grid.');
		}
		
		// calculate the rectangle 
		const left = columnMetrics.getStart(colStart);
		const width = colStart === colEnd ? 
			columnMetrics.getWidth(colStart) :
			columnMetrics.getEnd(colEnd) - left;
		const top = rowMetrics.getStart(rowStart);
		const height = rowStart === rowEnd ? 
			rowMetrics.getHeight(rowStart) : 
			rowMetrics.getEnd(rowEnd) - top;		
		
		// return
		return new Rect(left, top, width, height);		
	}
	
	clientRectFromRange(rowStart, colStart, rowEnd, colEnd) {
		// get view rect
		const rect = this.rectFromRange(rowStart, colStart, rowEnd, colEnd)
		// convert to client coordinates
		return this.rectToClient(rect, false);
	}	
}