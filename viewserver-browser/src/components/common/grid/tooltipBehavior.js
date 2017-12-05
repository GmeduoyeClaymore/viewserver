import Cell from './Cell';

export default function tooltipBehavior({ delay }) {
	let _grid = null;
	let _hoverCell = new Cell();
	let _hoverTimeoutHandle = 0;
	let _delay = delay || 500;

	function _beginHover() {
		_hoverTimeoutHandle = setTimeout(() => {
			_hoverTimeoutHandle = 0;

			const toolTip = _getToolTip();
			if (toolTip) {
				_grid.showToolTip(_hoverCell, toolTip);
			}
		}, _delay);
	}

	function _getToolTip() {
		const { dataSource, columns } = _grid;
		const row = dataSource.get(_hoverCell.rowIndex);
		if (row) {
			const column = columns[_hoverCell.columnIndex];
			return column && column.getToolTip && column.getToolTip(row) || null;
		}
	}

	function _cancelHover() {
		if (_hoverTimeoutHandle) {
			clearTimeout(_hoverTimeoutHandle);
			_hoverTimeoutHandle = 0;
		}
		_grid.hideToolTip();
	}

	function _handleMouseEnter(e) {
		const { columnIndex, rowIndex } = e;
		if (_hoverCell.update(columnIndex, rowIndex)) {
			_cancelHover();
		}
		if (_hoverCell.isValid) {
			_beginHover();
		}
	}

	function _handleMouseLeave() {
		_hoverCell.clear();
		_cancelHover();
	}

	return {
		attach(grid) {
			if (_grid) {
				throw new Error('Behavior already attached');
			}

			_grid = grid;
			_grid.onMouseEnter.add(_handleMouseEnter);
			_grid.onMouseLeave.add(_handleMouseLeave);
		},

		detach() {
			_cancelHover();
			_grid.onMouseEnter.remove(_handleMouseEnter);
			_grid.onMouseLeave.remove(_handleMouseLeave);
			_grid = null;
		}
	}
}