export default class Point {
	constructor(x, y) {
		this.x = x;
		this.y = y;
	}	

	offset(dx, dy) {
		this.x += dx;
		this.y += dy;	
	}

	equals(other) {
		return this.x === other.x && this.y === other.y;
	}

	toString() {
		return `{x=${this.x}, y=${this.y}}`;
	}
}