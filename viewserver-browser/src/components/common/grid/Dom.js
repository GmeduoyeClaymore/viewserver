export function bindElements(element, obj) {
	return Object.getOwnPropertyNames(obj).reduce((result, name) => {
		result[name] = element.querySelector(obj[name]); 
		return result;
	}, {});
}

var dimensions = null;
export function measureScrollbar() {
	if (dimensions) {
		return dimensions;
	}
 	
 	const div = document.createElement('div');
 	div.style.cssText = 'position:absolute; top:-10000px; left:-10000px; width:100px; height:100px; overflow:scroll';
 	document.body.appendChild(div);
  	dimensions = {
    	width: div.offsetWidth - div.clientWidth,
    	height: div.offsetHeight - div.clientHeight
  	};
  	document.body.removeChild(div);
  	return dimensions;
}

export function px(value) {
	return value + 'px';
}

export function setSize(element, width, height) {
	const { style } = element;
	const w = px(width);
	const h = px(height);
	if (style.width !== w || style.height !== h) {
		style.width = w;
		style.height = h;
		if(element.children[1]){
			element.children[1].height = h
			element.children[1].width = w
		}
		return true;
	}
	return false;
}

export function setCanvasSize(canvas, context, width, height, ratio) {
    if (isNaN(ratio)) { 
        ratio = window.devicePixelRatio || 1 / context.backingStorePixelRatio || 1; 
    }
    canvas.width = width * ratio;
    canvas.height = height * ratio;
    setSize(canvas, width, height);
    context.setTransform(ratio, 0, 0, ratio, 0, 0);
    return ratio;
}
