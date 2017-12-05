function rgbaTemplateElement(elements, template, index) {
	if (template[index]) {
		return '{0}';		
	}
	return elements[index];
}

export function toRGBATemplate(colors, template) {	
	template = 'rgba'.split('')
		.map(x => !!~template.indexOf(x));
	
	const div = document.createElement('div');
	const container = document.body || document.lastChild;  
	container.appendChild(div);
	var result = colors.map(function(color) {
		div.style.color = color;
		var elements = window.getComputedStyle(div).color.match(/\d+/g);
		if (elements.length < 3) {
			return '';
		} 
		if (elements.length === 3)  {
			elements.push(1.0);
		}
		return `rgba(${elements.map((x, i) => rgbaTemplateElement(elements, template, i)).join(',')})`;
	});
	container.removeChild(div);	
	return result;
}
