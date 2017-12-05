import StyleCellPainter from './StyleCellPainter';

const defaultPaint = StyleCellPainter.instance.paint;

export default class ColumnDefinition {
	static fromDataSource(dataSource) {
		if (dataSource) {
			const row = dataSource.size && dataSource.get(0) || null;
			if (row) {
				return ColumnDefinition.from(Object.keys(row));
			}
		}
		return [];
	}

	static from(columns, options) {
		return columns &&
			(Array.isArray(columns) ? columns : [columns]).map(column => {
				if (column instanceof ColumnDefinition) {
					return column;
				}

				if (typeof column === 'string') {
					return new ColumnDefinition({
						...options,
						key: column
					});
				}
				return new ColumnDefinition({
					...options,
					...column
				});
			}) || [];
	}

	constructor(options) {
		const {
			key,
			title,
			width,
			resizable,
			sortable,
			getRenderer,
			getValue,
			getFormattedValue,
			...properties
		} = options;

		// must have key
		if (key === null || typeof key === 'undefined') {
			throw new Error('Invalid column key');
		}

		this.key = key;
		this.title = title || properties.name || key || '';
		this.width = width || 50;
		this.resizable = typeof resizable === 'undefined' ? true : !!resizable;
		this.sortable = typeof sortable === 'undefined' ? true : !!sortable;

		Object.assign(this, properties);

		if (getValue) {
			this.getValue = getValue;
		}

		if (getFormattedValue) {
			this.getFormattedValue = getFormattedValue;
		}

		if (getRenderer) {
			this.getRenderer = getRenderer;
		}
	}

	getStyleId(data) {
		const styles = data.style;
		if (styles) {
			const style = styles[this.key];
			if (style) {
				return Number(style.id);
			}
		}
		return -1;
	}

	shouldInvalidate(newRow, oldRow) {
		return this.getStyleId(oldRow) !== this.getStyleId(newRow);
	}

	getValue(rowData) {
		return rowData ? rowData[this.key] : undefined;
	}

	getFormattedValue(rowData) {
		return this.getValue(rowData);
	}

	getRenderer() {
		return defaultPaint;
	}
}

