import CanvasDrawing from './CanvasDrawing';

class ColumnDrawing {
	constructor(columnIndex, column) {
		this.rows = [];
		this.columnIndex = columnIndex;
		this.column = column;
		this.rowIndex = -1;
	}

	addRow(row) {
		const { rows } = this;
		if (row > this.rowIndex) {
			rows.push(row);
			this.rowIndex = row;
		} else if (rows.indexOf(row) === -1) {
			rows.push(row);
		}
	}
}

function applyClippingMargin(bounds, style) {
    bounds = bounds.clone();
    bounds.offset(style.marginLeft, 0);
    bounds.grow(-(style.marginRight + style.marginLeft), 0);
    return bounds;
}

class GridPaintContext {
	constructor(gc) {
		this._isAnimated = false;
		this.paint = new CanvasDrawing(gc);
		this.dataSource = null;
		this.column = null;
		this.columnIndex = -1;
		this.rowIndex = -1;
		this.timestamp = 0;
	}

	beginPaint() {
		this.timestamp = Date.now();
	}

	setCell(dataSource, rowIndex, columnIndex) {
		this.dataSource = dataSource;
		this.column = dataSource.columns[columnIndex];
		this.columnIndex = columnIndex;
		this.row = dataSource.get(rowIndex);
		this.rowIndex = rowIndex;
		this._isAnimated = false;
	}

	setAnimated() {
		this._isAnimated = true;
	}

	get isAnimated() {
		return this._isAnimated;
	}
}

export default class ColumnPaintingStrategy {
	constructor(grid) {
		this.grid = grid;
		this.viewPort = grid.viewPort;
		this.visibleRange = grid.visibleRange;
		this.items = [];
		this.columnMetrics = grid.columnMetrics;
		this._context = new GridPaintContext(grid.context);
	}

	getOrCreate(index) {
		const { items, viewPort, visibleRange, columnMetrics } = this;

		// adjust the index to be within the array of columns renders for visibleRange
		index -= visibleRange.colStart;
		while (index > items.length - 1) {
			items.push(null);
		}

		let result = items[index];
		if (!result) {
			// ensure we use the actual column index for measuring and getting the column data
			const columnIndex = visibleRange.colStart + index;

			// store the column drawing object
			result = items[index] = new ColumnDrawing(columnIndex,
				this.grid.columns[columnIndex]);
		}

		// return
		return result;
	}

	renderColumn(item) {
		const visibleRange = this.visibleRange;
		const {dataSource} = this.grid;
		const renderer = item.column.render;

		/*
			We clip by column (trusting cells not to draw out of their bounds horizontally) as this
			is primarily to ensure text does not overflow. It is much faster to do this per column
			than per cell
		*/

		for (var i=0, n=item.rows.length; i<n; i++) {
			const rowIndex = item.rows[i];
			const row = dataSource.get(rowIndex);
			if (row) {
				this._context.setCell(dataSource, rowIndex, item.columnIndex);
				const bounds = this.viewPort.clientRectFromRange(rowIndex, item.columnIndex, rowIndex, item.columnIndex);
				item.column.render(this._context, bounds);
				if (this._context.isAnimated) {
					this.grid._dirtyAnimationRanges.invalidate(rowIndex, item.columnIndex, rowIndex, item.columnIndex);
				}
			}
		}

		this._context.paint.flush();
	}

	render() {
		this._context.beginPaint();
		for (let i=0, n=this.items.length; i<n; i++) {
			const item = this.items[i];
			if (item) {
				this.renderColumn(item);
			}
		}
	}
}