import { EventSource } from 'falcon-system';

export default class ArrayDataSource {
	constructor(data, columns) {
		this._data = data;
		this._columns = columns;
		this._onResized = new EventSource();
		this._onChanged = new EventSource();
	}

	get columns() {
		return this._columns;
	}

	get(index) {
		if (index < 0 || index >= this.size) {
			throw new Error('Index out of bounds');
		}
		return this._data[index];
	}

	getKey(row) {
		return row.$key;
	}

	get size() {
		return this._data.length;
	}

	get onResized() {
		return this._onResized.event;
	}

	get onChanged() {
		return this._onChanged.event;
	}
}