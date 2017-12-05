const MAX_CLIPPING_CACHE_SIZE = 100;

class TextRenderer {
	static instance = new TextRenderer();

	static textAlignRenderers = {
		start(gc, bounds, text) {
			gc.fillText(text, bounds.left, bounds.top + (bounds.height / 2));
		},
		center(gc, bounds, text) {
			gc.fillText(text, bounds.left + (bounds.width / 2), bounds.top + (bounds.height / 2));
		},
		end(gc, bounds, text) {
			gc.fillText(text, bounds.right, bounds.top + (bounds.height / 2));
		}
	};

	constructor() {
		this._renderText = null;
	}

	configure(gc, style) {
		gc.beginPath();
		gc.shadowColor = style.textShadowColor;
		gc.shadowOffsetX = style.textShadowOffsetX;
		gc.shadowOffsetY = style.textShadowOffsetY;
		gc.shadowBlur = style.textShadowBlur;
		gc.font = style.font;
		gc.fillStyle = style.color;
		gc.textAlign = style.textAlign;
		gc.textBaseline = 'middle';
		this._renderText = TextRenderer.textAlignRenderers[style.textAlign];
	}

	render(context, style, data) {
		this._renderText(context, data.bounds, data.text);
	}

	finalize(gc) {
		gc.shadowColor = 'black';
		gc.shadowOffsetX = 0;
		gc.shadowOffsetY = 0;
		gc.shadowBlur = 0;
	}
}

class ImageRenderer {
	static instance = new ImageRenderer();

	configure(context, style) {

	}

	render(context, style, data) {
		const { imageSettings, bounds, image } = data;

		const { sourceWidth, sourceHeight, destinationWidth, destinationHeight,
			locX, locY, offsetX, offsetY } = imageSettings;

		context.drawImage(image, locX, locY, sourceWidth, sourceHeight, bounds.x + offsetX, bounds.y + offsetY, destinationWidth, destinationHeight);
	}

	finalize(context) {

	}
}

class BackgroundRenderer {
	static instance = new BackgroundRenderer();

	configure(context, style) {
		context.beginPath();
		context.fillStyle = style.background;
	}

	render(context, style, data) {
		context.rect(data.left, data.top, data.width, data.height);
	}

	finalize(context) {
		context.fill();
	}
}

class BorderRenderer {

	static instance = new BorderRenderer();

	configure(context, style) {
		context.beginPath();
		context.strokeStyle = style.borderColor;
	}

	render(context, style, data) {
		let width = style.borderLeftWidth || style.borderRightWidth ||
			style.borderTopWidth || style.borderBottomWidth;
		if (width <= 0) {
			return;
		}

		context.lineWidth = width;

		width = width / 2;
		let position = '';
		if (style.borderRightWidth) {
			context.moveTo(data.right - width, data.top);
			context.lineTo(data.right - width, data.bottom);
		}

		if (style.borderBottomWidth) {
			context.moveTo(data.left, data.bottom - width);
			context.lineTo(data.right, data.bottom - width);
		}

		if (style.borderLeftWidth) {
			context.moveTo(data.left + width, data.top);
			context.lineTo(data.left + width, data.bottom);
		}

		if (style.borderTopWidth) {
			context.moveTo(data.left, data.top + width);
			context.lineTo(data.right, data.top + width);
		}
	}

	finalize(context) {
		context.stroke();
	}
}

class FillPathRenderer {
	static instance = new FillPathRenderer();

	configure(context, style) {
		context.beginPath();
		context.fillStyle = style.background;
	}

	render(context, style, path) {
		context.fill(path);
	}

	finalize(context) {
	}
}


class PaintStyle {
	constructor(style) {
		this.style = style;
		this.paintData = [];
		this.painters = [];
	}

	setRenderer(renderer, renderData) {
		const { painters, paintData } = this;
		let index = painters.indexOf(renderer);
		if (index === -1) {
			index = painters.length;
			painters.push(renderer);
			paintData.push([renderData]);
		} else {
			paintData[index].push(renderData);
		}
	}

	fillPath(path) {
		this.setRenderer(FillPathRenderer.instance, path);
	}

	// TODO: rename to background
	background(bounds) {
		this.setRenderer(BackgroundRenderer.instance, bounds);
	}

	fillText(text, bounds) {
		this.setRenderer(TextRenderer.instance, { text, bounds });
	}

	border(rect) {
		this.setRenderer(BorderRenderer.instance, rect);
	}

	renderRenderer(context, renderer, data) {
		renderer.configure(context, this.style);
		for (let i = 0, n = data.length; i < n; i++) {
			renderer.render(context, this.style, data[i]);
		}
		renderer.finalize(context);
	}

	drawImage(bounds, imageSettings, image) {
		this.setRenderer(ImageRenderer.instance, { bounds, imageSettings, image });
	}

	paint(gc) {
		const { painters } = this;
		for (let i = 0, n = painters.length; i < n; i++) {
			this.renderRenderer(gc, painters[i], this.paintData[i]);
		}
	}
}

class PaintBlock {
	constructor(clippingPath) {
		this.painterMap = {};
		this.painters = [];
		this.clippingPath = clippingPath;
	}

	getOrCreatePaintStyle(style) {
		const { painterMap } = this;
		var painter = painterMap[style.id];
		if (!painter) {
			painter = new PaintStyle(style);
			painterMap[style.id] = painter;
			this.painters.push(painter);
		}
		return painter;
	}

	paint(gc) {
		const { clippingPath } = this;
		if (clippingPath) {
			gc.save();
			gc.beginPath();
			gc.clip(clippingPath);
		}

		const { painters } = this;
		for (let i = 0, n = painters.length; i < n; i++) {
			painters[i].paint(gc);
		}

		this.painterMap = {};
		this.painters.length = 0;
		if (clippingPath) {
			gc.restore();
		}
	}
}

class PaintLayer {
	constructor() {
		this.paintBlocks = [new PaintBlock()];
		this.activeBlock = this.paintBlocks[0];
	}

	beginBlock(clippingPath) {
		const result = this.activeBlock;
		if (result.clippingPath !== clippingPath) {
			this.activeBlock = this.getOrCreatePaintBlock(clippingPath);
		}
		return result;
	}

	getOrCreatePaintBlock(clippingPath) {
		const { paintBlocks } = this;

		if (!clippingPath) {
			return paintBlocks[0];
		}

		let paintBlock = null;
		for (let i = 0, n = paintBlocks.length; i < n; i++) {
			paintBlock = paintBlocks[i];
			if (paintBlock.clippingPath === clippingPath) {
				return paintBlock;
			}
		}

		paintBlocks.push(paintBlock = new PaintBlock(clippingPath))
		return paintBlock;
	}

	getOrCreatePaintStyle(style) {
		return this.activeBlock.getOrCreatePaintStyle(style);
	}

	paint(gc) {
		const { paintBlocks } = this;
		for (let i = 0, n = paintBlocks.length; i < n; i++) {
			paintBlocks[i].paint(gc);
		}
	}
}

function createLayers(count) {
	const result = [];
	for (var i = 0; i < count; i++) {
		result.push(new PaintLayer());
	}
	return result;
}

const measureGc = document.createElement('canvas').getContext('2d', {
	alpha: false,
	preserveDrawingBuffer: true,
	antialias: false
});

let measureFont = null;
function measureText(style, text) {
	if (measureFont !== style.font) {
		measureGc.font = style.font;
	}
	return measureGc.measureText(text);
}

export default function PaintContext(gc, layerCount = 5) {
	const layers = createLayers(layerCount);
	const canvas = gc.canvas;

	let activeLayer = null;
	let activeLayerIndex = 0;
	let clippingCache = {};
	let clippingCacheSize = 0;
	const fontMeasureCache = {};

	function createTextClippingPath(bounds) {
		const result = new Path2D();
		result.rect(
			bounds.left,
			0,
			bounds.width,
			canvas.height);
		return result;
	}

	function getTextClippingPath(bounds) {
		const clippingKey = (bounds.left + ':' + bounds.width);

		let clippingPath = clippingCache[clippingKey];
		if (!clippingPath) {
			clippingPath = clippingCache[clippingKey] = createTextClippingPath(bounds);
			clippingCacheSize++;
		}
		return clippingPath;
	}

	return {
		beginPaint(index, clippingPath) {
			if (index < 0 || index >= layers.length) {
				throw new Error(`Invalid paint layer ${index}.`);
			}
			activeLayer = layers[activeLayerIndex = index];
			activeLayer.beginBlock(clippingPath);
		},

		fillPath(style, path) {
			activeLayer.getOrCreatePaintStyle(style).fillPath(path);
		},

		background(style, bounds) {
			if (style.background !== null) {
				activeLayer.getOrCreatePaintStyle(style).background(bounds.clone());
			}
		},

		measureText,

		canFitText(style, text, desiredWidth) {
			const width = fontMeasureCache[style.fontId] || (fontMeasureCache[style.fontId] = measureText(style, 'W').width);
			return text.length * width < desiredWidth ||
				measureText(style, text).width < desiredWidth;
		},

		fillText(style, text, bounds) {
			const restoreBlock = activeLayer.beginBlock(getTextClippingPath(bounds));
			activeLayer.getOrCreatePaintStyle(style).fillText(text, bounds.clone());
			activeLayer.activeBlock = restoreBlock;
		},

		border(style, rect) {
			activeLayer.getOrCreatePaintStyle(style).border(rect.clone());
		},

		drawImage(style, bounds, location, image) {
			activeLayer.getOrCreatePaintStyle(style).drawImage(bounds.clone(), location, image);
		},

		flush() {
			for (var i = 0; i < layers.length; i++) {
				layers[i].paint(gc);
			}

			// clipping regions are likely to be re-used when redrawing so
			// lets keep them around and only flush periodically.
			if (clippingCacheSize > MAX_CLIPPING_CACHE_SIZE) {
				clippingCache = {};
				clippingCacheSize = 0;
			}
		}
	}
}