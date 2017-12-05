import { toRGBATemplate } from './colors';
import { Style, StyleCache } from './styles';

export const DEFAULT_STYLE = Style.create({
    fontFamily: "'Open Sans'",
	fontSize: 9,
	borderRightWidth: 1,
	borderBottomWidth: 1,
	borderColor: 'white',
	color: 'white',
	marginLeft: 6,
	marginRight: 6
});

const overrideStyle = StyleCache.global.get({
	textAlign: 'start'
});

export default class StyleCellPainter {
	static instance = new StyleCellPainter();

	constructor() {
		// bind the paint function so it can be passed around stand alone
		this.paint = this.paint.bind(this);
	}

	paintBackground(ctx, style, bounds) {
		const { paint } = ctx;
		paint.beginPaint(0);
    	paint.background(style, bounds);
	}

	applyMargin(bounds, style) {
    	bounds = bounds.clone();
    	bounds.offset(style.marginLeft, style.marginTop);
    	bounds.grow(-(style.marginRight + style.marginLeft), -(style.marginBottom + style.marginTop));
    	return bounds;
	}

	paintContent(ctx, style, bounds) {
		const { paint, column, row } = ctx;
		let text = column.getFormattedValue(row) || null;
    	if (text) {
    		paint.beginPaint(1);
    		bounds = this.applyMargin(bounds, style);
    		if (style.textAlign !== 'start') {
    			if (!paint.canFitText(style, text, bounds.width)) {
    				style = StyleCache.global.getOverrideById(style.id, overrideStyle.id);
    			}
    		}
    		paint.fillText(style, text, bounds);
    	}
	}

	paintBorder(ctx, style, bounds) {
		const { paint } = ctx;
		paint.beginPaint(1);
    	paint.border(style, bounds);
	}

	getStyle(ctx) {
		const { column } = ctx;
		return column.getStyle && column.getStyle(ctx) || DEFAULT_STYLE;
	}

	paint(ctx, bounds) {
		if (ctx.row) {
        	// select style
        	const style = this.getStyle(ctx);
        	// paint sections
    		this.paintBackground(ctx, style, bounds);
    		this.paintBorder(ctx, style, bounds);
    		this.paintContent(ctx, style, bounds);
    	}
	}
}