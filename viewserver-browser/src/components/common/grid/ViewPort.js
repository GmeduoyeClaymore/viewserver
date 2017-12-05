import Point from './Point';
import Rect from './Rect';
import { numeric } from './Invariants';
import { Rx } from 'common/rx';

class Snapshot {
	constructor(viewPort) {
		this.left = viewPort.left;
		this.top = viewPort.top;
		this.width = viewPort.width;
		this.height = viewPort.height;
	}
}

function valueOrDefault(value, defaultValue) {
	return typeof value !== 'undefined' ? value : defaultValue;
}

export default class ViewPort {
	constructor(width, height) {
		this._left = 0;
		this._top = 0;
		this._width = numeric.between(width, 'width', 0);
		this._height = numeric.between(height, 'height', 0);
		this._clientRect = null;
		this._onModified = new Rx.Subject();
		this._isModified = false;
		this._updateCount = 0;
	}

	beginUpdate() {
		this._updateCount++;
	}

	endUpdate() {
		this._updateCount--;
		if (this._updateCount < 0) {
			throw new Error('View port beginUpdate/endUpdate calls are not matched.');
		}
		this._notifyModified();
	}

	get onModified() {
		return this._onModified;
	}

	get left() {
		return this._left;
	}

	get top() {
		return this._top;
	}

	get width() {
		return this._width;
	}

	get height() {
		return this._height;
	}

	createSnapshot() {
		return new Snapshot(this);
	}

	update(options) {
		this.beginUpdate();
		try {
			this.applyUpdate(options);
		}
		finally{
			this.endUpdate();
		}
	}

	applyUpdate(options) {
		// update options taking current values as defaults
		options = {
			left: this.left,
			top: this.top,
			width: this.width,
			height: this.height,
			...options
		};

		// adjust location
		this.setLocation(options.left, options.top);

		// adjust size
		this.setSize(options.width, options.height);
	}

	setLocation(left, top) {
		if (this._left !== left || this._top !== top) {
			this._left = left;
			this._top = top;
			this.setModified();
		}
	}

	setSize(width, height) {
		if (this.width !== width || this.height !== height) {
			this._width = width;
			this._height = height;
			this.setModified();
		}
	}

	setModified() {
		this._clientRect = null;
		this._isModified = true;
		this._notifyModified();
	}

	_notifyModified() {
		if (this._updateCount === 0 && this._isModified) {
			this._isModified = false;
			this._onModified.next();
		}
	}

	renderScrollDelta(dx, dy, renderer) {
		if (dx === 0 && dy === 0) {
			return;
		}

		let source = new Rect(0, 0, this.width, this.height);
		let target = new Point(0, 0);

		source.grow(-Math.abs(dx), -Math.abs(dy));
		if (dx > 0) {
			source.offset(dx, 0);
		} else {
			target.offset(-dx, 0);
		}

		if (dy > 0) {
			source.offset(0, dy)
		} else {
			target.offset(0, -dy);
		}

		if (source.width > 0 && source.height > 0) {
			renderer.renderScroll(source, target);
			return true;
		}
		return false;
	}

	renderDelta(snapShot, renderer) {
		const dx = this._left - snapShot.left;
		const dy = this._top - snapShot.top;
		if (dx === 0 && dy === 0) {
			return;
		}

		// shunt existing canvas content in the appropriate direction
		if (this.renderScrollDelta(dx, dy, renderer)) {
			let dirtyH = Math.abs(dy);
			let dirtyW = Math.abs(dx);
			if (dirtyH !== 0) {
				let scrollYRect = new Rect(0, dy > 0 ? this.height - dirtyH : 0, this.width, dirtyH);
				if (scrollYRect.width > 0 && scrollYRect.height > 0) {
					renderer.invalidateRect(scrollYRect);
				}
			}

			if (dirtyW !== 0) {
				let scrollXRect = new Rect(dx > 0 ? this.width - dirtyW : 0, dy > 0 ? 0 : dirtyH,
					dirtyW, this.height - dirtyH);
				if (scrollXRect.width > 0 && scrollXRect.height > 0) {
					renderer.invalidateRect(scrollXRect);
				}
			}
		}
		else {
			renderer.invalidateRect(new Rect(0, 0, this.width, this.height));
		}
	}

	rectToClient(rect, clone = true) {
		return (clone && rect.clone() || rect).offset(-this.left, -this.top);
	}

	rectToView(rect, clone = true) {
		return (clone && rect.clone() || rect).offset(this.left, this.top);
	}

	get clientRect() {
		return this._clientRect || (this._clientRect = new Rect(0, 0, this.width, this.height));
	}
}

