import Range from './Range';
import Rect from './Rect';
import Cell from './Cell';
import GridViewPort from './GridViewPort';
import ColumnMetrics from './ColumnMetrics';
import RowMetrics from './RowMetrics';
import * as dom  from './Dom';
import DirtyRangeManager from './DirtyRangeManager';
import RenderScheduler from './RenderScheduler';
import {Rx} from 'common/rx'
import CanvasDrawing from './CanvasDrawing';
import tooltipBehavior from './tooltipBehavior';
import StyleSheet from './StyleSheet';
import ColumnPaintingStrategy from './ColumnPaintingStrategy';
import ReactDOM from 'react-dom'
import EventSource from './EventSource'

const GridTemplate = `<div class="canv-grid" >
	 	<div class="canv-grid__scroll" tabIndex="0">
	 		<div class="canv-grid__scroll__content" tabIndex="0"></div>
		</div>
		<canvas class="canv-grid__canvas"></canvas>
		<div class="canv-grid__tooltip"></div>
	</div>`;

const styles = {
	row: StyleSheet.create({
		odd: {
			background: 'rgb(255, 255, 255)'
		},
		even: {
			background: 'rgb(255, 255, 255)'
		},
		hover: {
			background: '#269793'
		},
		selected: {
			background: '#00716d'
		}
	})
};

function wrapDebug(name, original) {
	name = name + ':';
	const result = function(...args) {
		console.log.apply(console, [name].concat(args));
		return original.apply(this, args);
	}
	result.$$original = original;
	return result;
}


export default class Grid {
	constructor(element, options) {
		this.element = element;
		this._disposables = [];

		// initialize options
		this._mergeOptions(this.options = {}, options || {});

		// create view port
		this.viewPort = new GridViewPort(0, 0, this.options.columnMetrics, this.options.rowMetrics);
		this.viewPort.onModified.subscribe(this._updateVisibleRange.bind(this));

		// dirty region management
		this._dirtyRanges = new DirtyRangeManager();
		this._dirtyRangesInvalidate = this._dirtyRanges.invalidate.bind(this._dirtyRanges);
		this._dirtyAnimationRanges = new DirtyRangeManager();

		// initialize rendering
		this._renderSchedulerId = RenderScheduler.register(
			this._renderAnimationFrame.bind(this));
		this._renderSuspendCount = 0;
		this._isRenderPending = false;
		this._isRendering = false;
		this._renderRange = this._renderRange.bind(this);
		this._animationTimeoutHandle = null;
		this._columnDrawing = null;
		this._initializeEvents();

		// configure dom
		this._bindElement();

		// go
		options.behaviors = (options.behaviors || [])
			.concat([tooltipBehavior({delay: 1000})]);

			// behaviours are attached after dom events (this allows behaviours to be intercepted via events, e.g. cancel mouse down)
		this._attachBehaviors(options.behaviors);
		this._optionsChanged(this.options);
	}

	_initializeEvents() {
		this._mouseCell = new Cell();
		this._clickCell = new Cell();
		this._clickCellCount = 0;
		this._dblClickCell = new Cell();
		this._createMouseEvent = this._createMouseEvent.bind(this);
	}

	get rowMetrics() {
		return this.viewPort.rowMetrics;
	}

	get columnMetrics() {
		return this.viewPort.columnMetrics;
	}

	get columnDefinitions() {
		return this.options.columnDefinitions;
	}

	get onClick() {
		return this._click;
	}

	get onDblClick() {
		return this._dblClick;
	}

	get onScroll() {
		return this._scroll;
	}

	get onScrollStart() {
		return this._scrollStart;
	}

	get onMouseDown() {
		return this._mouseDown;
	}

	get onMouseUp() {
		return this._mouseUp;
	}

	get onMouseMove() {
		return this._mouseMove;
	}

	get onMouseEnter() {
		return this._mouseEnter;
	}

	get onMouseLeave() {
		return this._mouseLeave;
	}

	get onKeyDown() {
		return this._keyDown;
	}

	get onKeyUp() {
		return this._keyUp;
	}

	get onKeyPress() {
		return this._keyPress;
	}

	get onContextMenu() {
		return this._contextMenu;
	}


	get onSelectionChanged() {
		return this._selectionChanged;
	}

	suspendRendering() {
		let updateInstance = this;
		this._renderSuspendCount++;
		return {
			dispose() {
				const instance = updateInstance;
				if (instance) {
					updateInstance = null;
					instance._renderSuspendCount--;
					if (instance._renderSuspendCount === 0) {
						instance.invalidateRange();
					}
				}
			}
		}
	}

	setOptions(options) {
		if (this._mergeOptions(this.options, options)) {
			this._optionsChanged(this.options);
		}
	}

	updateLayout() {
		// resync with outer size
		if (dom.setSize(this.refs.grid, this.element.clientWidth, this.element.clientHeight)) {
			this._updateViewPortSize();
		}

		// resync viewport content size as column metrics may have changed (this would not be needed if we could observe column metrics)
		this._updateViewPortContentSize();

		// full repaint
		this.invalidateRange();
	}

	showToolTip(cell, toolTip) {
		function fit(low, high, size, maxSize) {
			function makeResult(value, isFit) {
				return {
					value,
					isFit
				}
			}

			if (high + size < maxSize) {
				return makeResult(high, true);
			}

			if (low - size > 0) {
				return makeResult(low - size, true);
			}

			return size > maxSize ?
				makeResult(maxSize / 2 - size / 2, false) :
				makeResult(maxSize - size, false);
		}

		const cellBounds = this.viewPort.clientRectFromCell(cell.rowIndex, cell.columnIndex);
		if (cellBounds) {
			const element = this.refs.toolTip;
			element.style.visibility = 'visible';
			element.textContent = toolTip;
			const width = element.clientWidth;
			const height = element.clientHeight;
			const centerX = (cellBounds.left + cellBounds.right) / 2;
			const fitY = fit(cellBounds.top, cellBounds.bottom, height, this.viewPort.height);
			const fitX = fitY.isFit ?
				fit(cellBounds.left, centerX, width, this.viewPort.width) :
				fit(cellBounds.left, cellBounds.right, width, this.viewPort.width);

			element.style.left = fitX.value + 'px';
			element.style.top = fitY.value + 'px';
		}
	}

	hideToolTip(cell, toolTip) {
		this.refs.toolTip.style.visibility = 'hidden';
	}

	_optionsChanged(options) {
		this._setDataSource(options.dataSource);
		this.viewPort.update(options);
		this._setDebug(options.debug);
		this.updateLayout();
	}

	_setDebug(value) {
	 	value = !!value;
		if (this.debug !== value) {
			this.debug = value;
			if (value) {
				Object.getOwnPropertyNames(Object.getPrototypeOf(this)).forEach(name => {
					if (!name.match(/^_render/) && typeof(this[name]) === 'function') {
						this[name] = wrapDebug(name, this[name]).bind(this);
					}
				});
			} else {
				Object.getOwnPropertyNames(Object.getPrototypeOf(this)).forEach(name => {
					const value = this[name];
					if (typeof(value) === 'function' && value.$$original) {
						this[name] = value.$$original;
					}
				});
			}
		}
	}

	_setDataSource(value) {
		const oldValue = this.dataSource;
		if (oldValue !== value) {
			if(this.resizeSubscription){
				this.resizeSubscription.unsubscribe();
			}
			if(this.onChangedSubscription){
				this.onChangedSubscription.unsubscribe();
			}
			this.resizeSubscription = 
			this.dataSource = value;
			if (value) {
				this.resizeSubscription = value.onResized.subscribe(this._onDataSourceResized.bind(this));
				this.onChangedSubscription = value.onChanged.subscribe(this.invalidate.bind(this));
			}

			this._onDataSourceChanged();
		}
	}

	_onDataSourceChanged() {
		this._setRowCount(this.dataSource && this.dataSource.size || 0);
	}

	_onDataSourceResized() {
		if(this.dataSource){
			this._setRowCount(this.dataSource.size);
		}
	}

	_setRowCount(value) {
		const { rowMetrics } = this;
		if (rowMetrics.count !== value) {
			rowMetrics.count = value;

			// TODO: trigger this in viewport automatically
			this._updateVisibleRange();
			this._updateViewPortContentSize();
			this._updateViewPortData();

			// should always enqueue a render to update any blank space that may be revealed if the source is getting smaller and there is no invalidated range
			this.enqueueRender();
		}
	}

	_updateViewPortData() {
		const { visibleRange, dataSource } = this;
		if (visibleRange && dataSource && dataSource.view) {
			dataSource.view.request(visibleRange.rowStart, visibleRange.rowEnd, visibleRange.colStart, visibleRange.colEnd + 1);
		}
	}

	_updateViewPortContentSize() {
		const { canvas, viewPort, viewPortContent } = this.refs;

		// the size of the viewport content has changed, now we need to determine if we have a scroll bar
		if (dom.setSize(viewPortContent, this.columnMetrics.totalWidth, this.rowMetrics.totalHeight)) {
			this._updateViewPortSize();
		}

		// the overall sizes may not have changed even if the metrics have so we should update the
		// visible range just in case.
		this._updateVisibleRange();
	}

	_updateVisibleRange() {
		const oldValue = this.visibleRange;
		const newValue = this.viewPort.rangeFromClientRect();
		if ((newValue && !newValue.equals(oldValue)) || (!newValue && oldValue)) {
			this.visibleRange = newValue;
			this._onVisibleRangeChanged();
		}
	}

	_updateViewPortSize() {
		const { canvas, viewPort } = this.refs;
		const clientWidth = viewPort.clientWidth;
		const clientHeight = viewPort.clientHeight;

		if (dom.setSize(canvas, clientWidth, clientHeight)) {
			// if canvas size is changed then ensure we fully configure the canvas
			dom.setCanvasSize(canvas, this.context, clientWidth, clientHeight);
			// synchronize the viewport size
			this.viewPort.setSize(clientWidth, clientHeight);
			this.invalidateRange();
		}
	}

	_updateViewPortLocation() {
		const { viewPort } = this.refs;
		this.viewPortSnapshot = this.viewPortSnapshot || this.viewPort.createSnapshot();
		this.viewPort.setLocation(viewPort.scrollLeft, viewPort.scrollTop);
		this.enqueueRender();
	}

	_onVisibleRangeChanged() {
		this._dirtyRanges.setClippingRange(this.visibleRange);
		this._dirtyAnimationRanges.setClippingRange(this.visibleRange);
		this._updateViewPortData();
	}

	_mergeOptions(existingOptions, newOptions) {
		let { columnMetrics, rowMetrics, rowHeight, dataSource, ...rest } = newOptions;
		const {columnDefinitions } = newOptions;

		const modifiedOptions = {
			// take current options
			...existingOptions,
			// copy the rest (e.g. ones we don't care about so much)
			...rest
		};

		// select columns to use
		modifiedOptions.columnDefinitions = columnDefinitions || existingOptions.columnDefinitions || [];

		// select columnsf metrics to use
		if (modifiedOptions.columnDefinitions !== existingOptions.columnDefinitions) {
			columnMetrics = columnMetrics || new ColumnMetrics(columnDefinitions);
		}
		modifiedOptions.columnMetrics = columnMetrics || existingOptions.columnMetrics;

		// select datasource
		modifiedOptions.dataSource = dataSource || existingOptions.dataSource;
		const rowCount = modifiedOptions.dataSource && modifiedOptions.dataSource.size || 0;

		// select row metrics to use (can be based on explicit row metrics or rowHeight)
		if (!rowMetrics && rowHeight && (!existingOptions.rowMetrics || existingOptions.rowMetrics.rowHeight !== rowHeight)) {
			rowMetrics = new RowMetrics(rowHeight, rowCount);
		}
		modifiedOptions.rowMetrics = rowMetrics = rowMetrics || existingOptions.rowMetrics || new RowMetrics(24, rowCount);

		// update row metrics count
		let isModified = false;
		if (rowMetrics.count != rowCount) {
			rowMetrics.count = rowCount;
			isModified = true;
		}

		// determine whether anything was modified
		if (Object.getOwnPropertyNames(modifiedOptions).some(name => existingOptions[name] !== modifiedOptions[name])){
			Object.assign(existingOptions, modifiedOptions);
			isModified = true;
		}

		return isModified;
	}

	_attachBehaviors(behaviors) {
		if (!behaviors) {
			return;
		}

		behaviors.forEach(behavior => {
			var features = behavior.attach(this);
			if (features) {
				features = Array.isArray(features) ?
					features :
					[features];
				features.forEach(feature => {
					switch (feature) {
						case 'Selection':
							this.isSelected = behavior.isSelected.bind(behavior);
							behavior.onChanged.subscribe(this._onSelectionChanged.bind(this));
							break;
					}
				});
			}
		});
	}

	// default to no selection supported, see attached behaviors
	isSelected() {
		return false;
	}

	_onSelectionChanged(e) {
		e.added.forEach(x =>
			this.invalidateRows(x.start, x.end));
		e.removed.forEach(x =>
			this.invalidateRows(x.start, x.end));

		/*
			Notify (don't send added and removed yet as
			these are NOT accurate but good enough to render correctly, needs fixing)
		*/
		this._selectionChanged.next();
	}

	_addDisposable(value) {
		this._disposables.push(value);
		return value;
	}

	_bindDomEvent(target, event, transform, capture) {
		return this._addDisposable(
			EventSource.fromDOM(target, event, capture, transform));
	}

	_bindElement() {
		// set template
		// this.element.innerHTML = GridTemplate;
		const tplFrag = document.createRange().createContextualFragment(GridTemplate);
		this.element.appendChild(tplFrag);

		// bind refs
		const { viewPort, grid, canvas } = this.refs = dom.bindElements(this.element, {
			grid: '.canv-grid',
			canvas: '.canv-grid__canvas',
			viewPort: '.canv-grid__scroll',
			viewPortContent: '.canv-grid__scroll__content',
			toolTip: '.canv-grid__tooltip'
		});

		/*
		   Allow the root container to be overriden for handling keyboard
		   events (not sure this is the best way to do things but we are
		   short on time).
		*/
		const container = this.refs.container = this.options.container ||
			this.element || grid;

		// make disposable
		const scrolling = this._bindDomEvent(viewPort, 'scroll', undefined, false);


		scrolling.debounceTime(100).subscribe(this._handleScrolled.bind(this));
		scrolling.subscribe(this._handleScrollStart.bind(this));
		//this._bindDomEvent(viewPort, 'mousewheel').subscribe(this._handleScrolled.bind(this));

		// expose events to outside world (via _this.createMouseEvent transform)
		const { _createMouseEvent } = this;
		this._mouseDown = this._bindDomEvent(
			viewPort, 'mousedown', _createMouseEvent);
		this._mouseUp = this._bindDomEvent(
			viewPort, 'mouseup', _createMouseEvent);
		this._mouseMove = this._bindDomEvent(
			viewPort, 'mousemove', _createMouseEvent);
		this._contextMenu = this._bindDomEvent(
			viewPort, 'contextmenu', _createMouseEvent);

		// mouse enter and leave are not driven directly from their counterparts
		this._mouseEnter = new Rx.Subject();
		this._mouseLeave = new Rx.Subject();
		this._click = new Rx.Subject();
		this._dblClick = new Rx.Subject();
		this._scroll = new Rx.Subject();
		this._scrollStart = new Rx.Subject();
		this._selectionChanged = new Rx.Subject();

		// bind to track enter/leave/click at cell level
		this.onMouseDown.subscribe(this._handleMouseDown.bind(this));
		this.onMouseMove.subscribe(this._handleMouseMove.bind(this));
		// don't expose these directly, we need to do some work before dispatching
		this._bindDomEvent(grid, 'mouseout', _createMouseEvent)
			.subscribe(this._handleMouseOut.bind(this));
		this._bindDomEvent(grid, 'click', _createMouseEvent)
			.subscribe(this._handleClick.bind(this));
		this._bindDomEvent(grid, 'dblclick', _createMouseEvent)
			.subscribe(this._handleDblClick.bind(this));

		// keyboard handling
		this._keyDown = this._bindDomEvent(container, 'keydown');
		this._keyUp = this._bindDomEvent(container, 'keyup');
		this._keyPress = this._bindDomEvent(container, 'keypress');

		// clipboard
		this._copy = this._bindDomEvent(viewPort, 'copy');

		this.context = canvas.getContext('2d', { alpha: false, preserveDrawingBuffer: true, antialias: true });
		this.drawing = CanvasDrawing(this.context);
	}

	_unbindElement() {
		// clear disposables
		this._disposables.forEach(x => this.getRid(x));
		this._disposables.length = 0;

		// remove content
		this.element.innerHTML = null;
	}

	getRid(x){
		if(x.unsubscribe){
			x.unsubscribe()
		}
		else if(x.dispose){
			x.dispose()
		}
		else{
			console.error("Unable to get rid of element " + x);
		}
	}

	_getColumn(columnIndex) {
		return columnIndex >= 0 && columnIndex < this.columnDefinitions.length ? this.columnDefinitions[columnIndex] : undefined;
	}

	_getRowData(rowIndex) {
		const { dataSource } = this;
		return dataSource && dataSource.get(rowIndex) || undefined;
	}

	_createMouseEvent(e) {
		const { viewPort } = this;

		// get canvas bounds
		const bounds = this.refs.canvas.getBoundingClientRect();

		// get coordinates in termins of the canvas and verify we are in bounds
		const x = e.clientX - Math.ceil(bounds.left);
		const y = e.clientY - Math.ceil(bounds.top);

		// grab the cell indices at the specified point
		const { columnIndex, rowIndex } = viewPort.cellFromClientPoint(x, y);

		// extract column
		let column;
		if (columnIndex >= 0) {
			column = this.columnDefinitions[columnIndex];
		}

		// extract row data
		let row;
		if (rowIndex >= 0) {
			row = this._getRowData(rowIndex);
		}

		let cellMetrics;

		// build the event
		const result = {
			trigger: e,
			x,
			y,
			columnIndex,
			rowIndex,
			column,
			row,
			getCellMetrics() {
				if (rowIndex >= 0 && columnIndex >= 0) {
					if (!cellMetrics) {
						// get the bounds of the cell
						const bounds = viewPort.clientRectFromRange(rowIndex, columnIndex, rowIndex, columnIndex);

						// calculate in terms of cell offset
						const offsetX = x - bounds.left;
						const offsetY = y - bounds.top;
						cellMetrics = Object.freeze({
							offsetX,
							offsetY,
							width: bounds.width,
							height: bounds.height
						});
					}
				}
				return cellMetrics;
			}
		};

		// extract hit test regions
		result.hitRegions = rowIndex >= 0 && columnIndex >= 0 ? this._hitTest(result) : [];

		return result;
	}

	_hitTest(e) {
		const result = [];

		let { hitTest } = e.column;
		hitTest = Array.isArray(hitTest) ? hitTest : (hitTest && [hitTest] || null);
		if (!(hitTest && hitTest.length)) {
			return result;
		}

		// search for matched hit test result
		for (let i=0, n=hitTest.length; i<n; i++) {
			const region = hitTest[i](e);
			if (region) {
				result.push(region);
			}
		}

		return result;
	}


	_updateMouseCellInfo(e) {
		const { _mouseCell } = this;

		let { rowIndex, columnIndex } = e;

		// if either are out of bounds then we are NOT over a cell
		if (rowIndex < 0 || columnIndex < 0) {
			rowIndex = -1;
			columnIndex = -1;
		}

		// update mouse cell (return value will beold cell if modified)
		const oldValue = _mouseCell.update(columnIndex, rowIndex);
		if (oldValue) {
			this._mouseCellChanged(oldValue, _mouseCell);

			// leaving a cell?
			if (oldValue.isValid) {
				this._mouseLeave.next({
					...e,
					columnIndex: oldValue.columnIndex,
					rowIndex: oldValue.rowIndex,
					column: this._getColumn(oldValue.columnIndex),
					row: this._getRowData(oldValue.rowIndex)
				});
			}

			// entering a new cell?
			if (_mouseCell.isValid) {
				this._mouseEnter.next(e);
			}
		}
	}

	_mouseCellChanged(oldValue, newValue) {
		// invalidate for row hover
		if (oldValue.rowIndex >= 0 && newValue.rowIndex !== oldValue.rowIndex) {
			this.invalidateRow(oldValue.rowIndex);
		}
		if (newValue.rowIndex >= 0 && newValue.rowIndex !== oldValue.rowIndex) {
			this.invalidateRow(newValue.rowIndex);
		}
	}

	_updateClickInfo(columnIndex, rowIndex) {
		const { _clickCellInfo } = this;
		_clickCellInfo.columnIndex = columnIndex;
		_clickCellInfo.rowIndex = rowIndex;
	}

	_handleMouseDown({ rowIndex, columnIndex }) {
		if(this._clickCell.update(columnIndex, rowIndex)) {
			this._clickCellCount = 1;
		} else {
			this._clickCellCount++;
		}
	}

	_handleClick(e) {
		const { _clickCell } = this;
		if (!_clickCell.update(e.columnIndex, e.rowIndex) && _clickCell.isValid) {
			/*
			   There should be no need to reset tracking as we are verifying that there are at least
			   two mouse downs on the same cell within the timeframe of a native "click".
			*/
			this._click.next(e);
		}
	}

	_handleDblClick(e) {
		const { _clickCell } = this;
		if (!_clickCell.update(e.columnIndex, e.rowIndex) && _clickCell.isValid && this._clickCellCount >= 2) {
			/*
				There should be no need to reset tracking as we are verifying that there are at least
			   two mouse downs on the same cell within the timeframe of a native "dblclick".
			*/
			this._dblClick.next(e);
		}
	}

	_handleMouseMove(e) {
		this._updateMouseCellInfo(e);
	}

	_handleMouseOut(e) {
		this._updateMouseCellInfo(e);
	}

	invalidateRange(range) {
		if (!range) {
			this._dirtyRanges.clear();
			range = this.visibleRange;
			if (!range) {
				return;
			}
		}
		this.invalidate(range);
	}

	invalidateRows(rowStart, rowEnd) {
		this.invalidate({rowStart, colStart: 0, rowEnd, colEnd : this.columnDefinitions.length - 1});
	}

	invalidateRow(index) {
		if (index >= 0 && index < this.rowMetrics.count) {
			this.invalidate({rowStart : index, colStart: 0, rowEnd  : index, colEnd : this.columnDefinitions.length - 1});
		}
	}

	invalidateRect(rect) {
		this.invalidateRange(this.viewPort.rangeFromClientRect(rect));
	}

	invalidate({rowStart, colStart, rowEnd, colEnd}) {
		if (this.visibleRange && this.visibleRange.intersects(rowStart, colStart, rowEnd, colEnd)) {
			this._dirtyRanges.invalidate(rowStart, colStart, rowEnd, colEnd);
			this.enqueueRender();
		}
	}

	renderScroll(source, target) {
		this.context.drawImage(
			this.context.canvas,
			source.x,
			source.y,
			source.width,
			source.height,
			target.x,
			target.y,
			source.width,
			source.height);
	}

	enqueueRender() {
		if (this._renderSchedulerId && !this._isRenderPending) {
			this._isRenderPending = true;
			RenderScheduler.enqueue(this._renderSchedulerId);
		}
	}

	_renderAnimationFrame() {
		this._isRenderPending = false;
		if (this._renderSuspendCount) {
			return;
		}

		try {
			this._isRendering = true;
			this._renderGrid();
		}
		finally{
			this._isRendering = false;
		}
	}

	_renderGrid() {
		const range = this.visibleRange;
		if (range) {
			// render viewport changes
			if (this.viewPortSnapshot) {
				this.viewPort.renderDelta(this.viewPortSnapshot, this);
				this.viewPortSnapshot = null;
			}

			this._columnDrawing = new ColumnPaintingStrategy(this);
			this._dirtyRanges.flush(this._renderRange);
			this.drawing.flush();
			this._columnDrawing.render();
		}

		// clear any unrendered space
		this._renderBackground();

		this.enqueueAnimations();
	}

	_renderBackground() {
		const { visibleRange, viewPort, context } = this;

		context.fillStyle = 'rgb(255, 255, 255)';
		context.beginPath();
		if (visibleRange) {
			var rect = viewPort.clientRectFromRange(
				visibleRange.rowStart,
				visibleRange.colStart,
				visibleRange.rowEnd,
				visibleRange.colEnd);

			const bgRightWidth = viewPort.clientRect.right - rect.right;
		 	if (bgRightWidth > 0) {
		 		context.rect(rect.right, 0, bgRightWidth, viewPort.height);
		 	}

		 	const bgBottomHeight = viewPort.clientRect.bottom - rect.bottom;
			if (bgBottomHeight > 0) {
				context.rect(0, rect.bottom, viewPort.width - bgRightWidth, bgBottomHeight);
			}
		} else {
			context.rect(0, 0, viewPort.width, viewPort.height);
		}
		context.fill();
	}

	_renderRange(rowStart, colStart, rowEnd, colEnd) {
		var { context, dataSource, } = this;
		for (let i=rowStart; i <= rowEnd; i++) {
			this._renderRow(context, dataSource, i, colStart, colEnd);
		}
	}

	_isMouseOverRow(rowIndex) {
		return this._mouseCell.rowIndex === rowIndex;
	}

	selectRowStyle(dataSource, rowIndex) {
		if (this.isSelected(rowIndex, dataSource.getKey(dataSource.get(rowIndex)))) {
			return styles.row.selected;
		}

		if (this._isMouseOverRow(rowIndex)) {
			return styles.row.hover;
		}

		return rowIndex % 2 === 0 ?
			styles.row.even :
			styles.row.odd;
	}


	_renderRow(ctx, dataSource, rowIndex, colStart, colEnd) {
		let { viewPort } = this;

		const rowStyle = this.selectRowStyle(dataSource, rowIndex);
		const rowRect = this.viewPort.clientRectFromRange(
			rowIndex, colStart, rowIndex, colEnd);
		rowRect.intersect(viewPort.clientRect);

		// render the row background
 		this.drawing.beginPaint(0);
 		this.drawing.background(rowStyle, rowRect);

		for (let i=colStart; i <= colEnd; i++) {
			const _columnDrawing = this._columnDrawing.getOrCreate(i);
			if (_columnDrawing) {
		 		_columnDrawing.addRow(rowIndex);
		 	}
		}
	}

	clearAnimationRenderTimeout() {
		if (this._animationTimeoutHandle) {
			clearTimeout(this._animationTimeoutHandle);
			this._animationTimeoutHandle = null;
		}
	}

	enqueueAnimations() {
		if (this._dirtyAnimationRanges.length) {
			this._dirtyAnimationRanges.flush(this._dirtyRangesInvalidate);
			if (this._animationTimeoutHandle == null) {
				this._animationTimeoutHandle = setTimeout(() => {
					this._animationTimeoutHandle = null;
					this.enqueueRender();
				}, 1000 / 30);
			}
		}
	}

	scrollRowIntoView(rowIndex) {
		const { viewPort } = this;

		// locate the row rectangle
		const rect = viewPort.rectFromCell(rowIndex, 0);

		//	 calculate the amount to move by
		let scrollDown = rect.bottom - (viewPort.top + viewPort.height);
		let scrollUp = viewPort.top - rect.top;
		if (scrollUp >= 0) {
			this.refs.viewPort.scrollTop -= scrollUp;
		} else if (scrollDown >= 0) {
			this.refs.viewPort.scrollTop += scrollDown;
		}
		// TODO: handle both (middle, edge case)
	}

	destroy() {
		this._renderSchedulerId = RenderScheduler.unregister(this._renderSchedulerId);
		this._unbindElement();
	}

	_handleScrolled(e) {
		require('electron').webFrame.setZoomFactor(1)		
		const susp = this.suspendRendering();
		this._updateViewPortLocation();
		susp.dispose();
		this._scroll.next({
			trigger: e,
			left: this.viewPort.left,
			top: this.viewPort.top
		});
		 
	}

	_handleScrollStart(e) {
		this._scroll.next({
			trigger: e,
			left: this.viewPort.left,
			top: this.viewPort.top
		});
	}
}

