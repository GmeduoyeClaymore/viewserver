import { Rx } from 'common/rx';
import * as keyboard from './keyboard';

function min(x, y) {
	return x < y ? x : y;
}

function max(x, y) {
	return x > y ? x : y;
}

function binarySearch(array, item, compare) {
	let m = 0, k = 0;
	let n = array.length - 1;
	while (m <= n) {
    	k = n + m >> 1;
    	const cmp = compare(array[k], item);
    	if (cmp > 0) {
        	m = k + 1;
    	} else if (cmp < 0) {
        	n = k - 1;
    	} else {
    		return k;
    	}
	}

	return -m - 1;
}

export class RowRange {
	static coerce(rangeOrStart, end) {
		if (rangeOrStart instanceof RowRange) {
			return rangeOrStart;
		}

		const start = Number(rangeOrStart);
		if (!isNaN(start)) {
			return new RowRange(start, end);
		}

		if (typeof rangeOrStart === 'object' || typeof rangeOrStart === 'function') {
			return new RowRange(rangeOrStart.start, rangeOrStart.end);
		}

		throw new Error('Invalid range');
	}

	constructor (start, end) {
		this.start = min(start, end);
		this.end = max(start, end);
	}

	get length() {
		return this.end - this.start + 1;
	}

	get isEmpty() {
		return this.length === 0;
	}

	update(start, end) {
		const length = this.length;
		this.start = min(start, end);
		this.end = max(start, end);
		return this.length - length;
	}

	subtract(start, end) {
		const maxStart = max(this.start, start);
		const minEnd = min(this.end, end);
		if (maxStart > minEnd) {

			return;
		}

		let split = null;
		if (this.start === maxStart) {
			this.start = minEnd + 1;
		} else {
			if (this.end !== minEnd) {
				split = new RowRange(minEnd + 1, this.end);
			}
			this.end = maxStart - 1;
		}

		return split;
	}

	intersects(start, end) {
		return max(this.start, start) <= min(this.end, end);
	}

	toString() {
		return `(start=${this.start}, end=${this.end})`;
	}
}

export class RowRangeSelector {
	constructor(ranges) {
		this._ranges = ranges || [];
		this._count = 0;
	}

	findRangeIndex(row) {
		return binarySearch(this._ranges, row, range => {
			if (row < range.start) {
				return -1;
			}
			if (row > range.end) {
				return 1;
			}
			return 0;
		});
	}

	getRanges(target) {
		if (target) {
			target.push.apply(target, this._ranges);
			return target;
		}
		return this._ranges.slice(0);
	}

	findRange(rowIndex) {
		const index = this.findRangeIndex(rowIndex);
		return index >= 0 ? this._ranges[index] : null;
	}

	isSelected(rowIndex) {
		return this.findRangeIndex(rowIndex) >= 0;
	}

	getRange(index) {
		return index >= 0 && index < this._ranges.length ?
			this._ranges[index] :
			null;
	}

	get count() {
		return this._count;
	}

	remove(rangeOrStart, rangeEnd) {
		// ensure we have a valid range (with start <= end)
		const { start, end } = RowRange.coerce(
			rangeOrStart, rangeEnd);

		let rangeIndex = this.findRangeIndex(start);
		if (rangeIndex < 0) {
			rangeIndex = -(rangeIndex + 1);
		}

		// track range deletions
		let deleteIndex = -1;
		let deleteCount = 0;

		// iterate forward and back deleting ranges
		let range = null;
		let count = this._count;
		while ((range = this.getRange(rangeIndex)) && range.intersects(start, end)) {
			count -= range.length;

			const split = range.subtract(start, end);
			count += range.length;

			// if the range is not empty it must be removed
			if (range.isEmpty) {
				if (deleteIndex === -1) {
					deleteIndex = rangeIndex;
				}
				deleteCount++;
			}
			rangeIndex += 1;

			// insert any split
			if (split) {
				count += split.length;
				this._ranges.splice(rangeIndex, 0, split);
			}
		}

		if (deleteCount > 0) {
			this._ranges.splice(deleteIndex, deleteCount);
		}

		if (this.count !== count) {
			this._count = count;
			return true;
		}
		return false;
	}


	add(rangeOrStart, rangeEnd) {
		// ensure we have a valid range (with start < end)
		let { start, end } = RowRange.coerce(rangeOrStart, rangeEnd);

		// get range index if found, or index of items before our insertion point
		let rangeIndex = this.findRangeIndex(start);
		if (rangeIndex < 0) {
			rangeIndex = -(rangeIndex + 2);
		}

		// determine if we are joining an existing range
		let count = this._count;
		let range = null;
		if ((range = this.getRange(rangeIndex)) && range.intersects(start - 1, end)) {
			count += range.update(range.start, max(end, range.end));
		}  else if ((range = this.getRange(++rangeIndex)) && range.intersects(start, end + 1)) {
			count += range.update(min(start, range.start), range.end);
		} else {
			count += (range = new RowRange(start, end)).length;
		}

		// was the range distinct or do we need to prune
		if (count - this._count === range.length) {
			this._ranges.splice(rangeIndex, 0, range);
		} else {
			// move to next range
			rangeIndex++;
			// increase end point to capture adjacent in intersects
			end++;
			// determine how many ranges we can merge
			let joinRange = null;
			let deleteIndex = rangeIndex;
			while ((joinRange = this.getRange(rangeIndex)) && joinRange.intersects(start, end)) {
				count += range.update(range.start, max(range.end, joinRange.end)) - joinRange.length;
				rangeIndex++;
			}

			// delete merged ranges
			this._ranges.splice(deleteIndex, rangeIndex - deleteIndex);
		}

		this._count = count;
	}

	set(rangeOrStart, rangeEnd) {
		this.clear();
		this.add(rangeOrStart, rangeEnd);
	}

	clear() {
		this._ranges.length = 0;
		this._count = 0;
	}

	toggle(row) {
		var rangeIndex = this.findRangeIndex(row)
		if (rangeIndex >= 0) {
			this.remove(row, row);
			return false;
		}
		this.add(row, row);
		return true;
	}

	getSelectedRowIndices() {
		const { _ranges } = this;
		const result = [];
		for (let i=0, il=_ranges.length; i<il; i++) {
			const range = _ranges[i];
			for (let j = range.start, je = range.end; j<=je; j++) {
				result.push(j);
			}
		}

		return result;
	}
}

export class RowRangeSelectionBehavior {
	constructor(selector) {
		this.selector = selector || new RowRangeSelector();
		this._selectionStart = -1;
		this._selectionEnd = -1;
		this._shiftSelection = null;
		this._onChanged = new Rx.Subject();
		this._onDragStart = new Rx.Subject();
		this._mouseDownRowIndex = -1;
		this._mouseMoveRowIndex = -1;
		this._selectionStartWasSelected = false;
	}

	attach(grid) {
		this._grid = grid;

		grid.onMouseDown.add(this.handleMouseDown, this);
		grid.onMouseMove.add(this.handleMouseMove, this);
		grid.onMouseUp.add(this.handleMouseUp, this);
		grid.onKeyDown.add(this.handleKeyDown, this);

		return ['Selection'];
	}

	get rowCount() {
		const { _grid } = this;
		return _grid && _grid.dataSource && _grid.dataSource.size || 0;
	}

	handleKeyDown(e) {
		let handled = true;
	    switch (keyboard.getShortcutKey(e)) {
	    	case 'ArrowUp':
	    		this.select(this._selectionEnd - 1);
	    		this._grid.scrollRowIntoView(this._selectionEnd);
	    		break;
	    	case 'ArrowDown':
	    		this.select(this._selectionEnd + 1);
	    		this._grid.scrollRowIntoView(this._selectionEnd);
	    		break;
	    	case 'Shift+ArrowUp':
	    		this.selectRange(this._selectionEnd - 1)
	    		this._grid.scrollRowIntoView(this._selectionEnd);
	    		break;
	    	case 'Shift+ArrowDown':
	    		this.selectRange(this._selectionEnd + 1)
	    		this._grid.scrollRowIntoView(this._selectionEnd);
	    		break;
	    	case 'Control+A':
	    		this.selectAll();
	    		break;
	    	default:
	    		handled = false;
	    		break;
    	}

    	if (handled) {
    		e.preventDefault();
    	}
	}

	isValidRow(row) {
		return row >= 0 && row < this.rowCount;
	}

	startSelection(row) {
		if (this.isValidRow(row)) {
			// reset the current shift selection
			this._shiftSelection = null;
			// update selection start/end
			this._selectionStart = row;
			this._selectionEnd = row;
			this._selectionStartWasSelected = this.isSelected(row) && this.selector.count === 1;
			// return the row
			return new RowRange(row, row);
		}
		return null;
	}

	endSelection(row) {
		row = min(max(0, row), this.rowCount - 1);
		if (row >= 0) {
			if (this._selectionStart === -1) {
				this._selectionStart = 0;
			}
			this._selectionEnd = row;
			return new RowRange(this._selectionStart, row);
		}
		return null;
	}

	select(row) {
		const select = this.startSelection(row);
		if (select) {
			const remove = this.selector.getRanges();
	    	this.selector.set(select);
	    	this.changed([select], remove);
		}
	}

	selectToggle(row) {
		const select = this.startSelection(row);
		if (select) {
			if (this.selector.toggle(row)) {
	    		this.changed([select], null);
	    	} else {
	    		this.changed(null, [select]);
	    	}
		}
	}

	selectAll() {
		this.selector.set(new RowRange(0, this.rowCount - 1));
		this.changed(this.selector.getRanges(), null);
	}

	selectRange(endRow) {
		const select = this.endSelection(endRow);
		if (select) {
			let remove = null;
			// remove previous shift selection
		    const { _shiftSelection } = this;
		    if (_shiftSelection) {
		    	this.selector.remove(_shiftSelection);
		    	remove = [_shiftSelection];
		    }

		    // add new shift selection
		    this.selector.add(this._shiftSelection = select);

		    // trigger changed
		    this.changed([select], remove);
		}
	}

	isContextMenuClickOnSelection(button, row) {
		return button === 2 && this.selector.isSelected(row);
	}

	handleMouseDown(e) {
		const button = e.trigger.button;
		if (!~[0, 2].indexOf(button)) {
			return;
		}

		let cancelled = false;
		this._onDragStart.next({
			...e,
			cancel() {
				cancelled = true;
			}
		});

		if (cancelled) {
			this._mouseDownRowIndex = -1;
			this._mouseMoveRowIndex = -1;
			return;
		}

		const { rowIndex, columnIndex } = e;
		if (rowIndex >= 0 && columnIndex >= 0) {
			if (this.isContextMenuClickOnSelection(button, rowIndex)) {
				return;
			}

			if (e.trigger.shiftKey) {
	    	 	this.selectRange(rowIndex);
	    	} else if (e.trigger.ctrlKey) {
	    		this.selectToggle(rowIndex);
	    	} else {
	    		this.select(rowIndex);
	    	}
		}

		// update drag selection tracking
		this._mouseDownRowIndex = rowIndex;
		this._mouseMoveRowIndex = rowIndex;
	}

	handleMouseMove(e) {
		// if mouse is down and we shifting then move
		if (~this._mouseDownRowIndex) {
			const { rowIndex } = e;
			if (this._mouseMoveRowIndex !== rowIndex) {
				this.selectRange(this._mouseMoveRowIndex = rowIndex);
			}
		}
	}

	handleMouseUp(e) {
		const { rowIndex } = e;
		if (rowIndex >= 0 && rowIndex === this._mouseDownRowIndex && this._selectionStartWasSelected) {
			this._selectionStartWasSelected = false;
			if (!e.trigger.ctrlKey) {
				this.selectToggle(rowIndex);
			}
		}
		this._mouseDownRowIndex = -1;
		this._mouseMoveRowIndex = -1;
	}

	changed(added, removed) {
		if (((added && added.length) || (removed && removed.length))) {
			removed = removed || [];
			added = added || [];

			// Remove inserted from removed (e.g. overlaps)
			if (removed.length && added.length) {
				new RowRangeSelector(removed).remove(added);
			}

			this._onChanged.next({added, removed});
		}
	}

	isSelected(row) {
		return this.selector.isSelected(row);
	}

	get onDragStart() {
		return this._onDragStart;
	}

	get onChanged() {
		return this._onChanged;
	}
}