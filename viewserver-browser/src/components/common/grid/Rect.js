import * as Invariants from './Invariants';

export default class Rect {
	constructor(x, y, width, height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	toString() {
		return `{x=${this.x}, y=${this.y}, width=${this.width}, height=${this.height}}`;
	}

	intersect(rect) {
		let x = Math.max(this.x, rect.x);
		let right = Math.min(this.x + this.width, rect.x + rect.width);
		let y = Math.max(this.y, rect.y);
		let bottom = Math.min(this.y + this.height, rect.y + rect.height);
		if (right >= x && bottom >= y) {
			this.x = x;
			this.y = y;
			this.width = right - x;
			this.height = bottom - y;
		} else {
			this.x = 0;
			this.y = 0;
			this.width = 0;
			this.height = 0;
		}
		return this;
	}

 	get left() {
		return this.x;
	}

	get top() {
		return this.y;
	}

	get right() {
		return this.x + this.width;
	}

	get bottom() {
		return this.y + this.height;
	}

	get isEmpty() {
		return this.width === 0 && this.height === 0;
	}

	setLocation(x, y) {
		this.x = x;
		this.y = y;
	}

	setSize(width, height) {
		this.width = width;
		this.height = height;
	}

	equals(rect) {
		return this.x === rect.x && this.y === rect.y &&
			this.width === rect.width && this.height === rect.height;
	}

	clone() {
		return new Rect(this.x, this.y, this.width, this.height);
	}

	offset(dx, dy) {
		this.x += dx;
		this.y += dy;
		return this;
	}

	inflate(dx, dy) {
		this.x -= dx;
        this.y -= dy;
        this.width += (2 * dx);
        this.height += (2 * dy);
		return this;
	}

	grow(dw, dh) {
		this.width += dw;
		this.height += dh;
		return this;
	}
}