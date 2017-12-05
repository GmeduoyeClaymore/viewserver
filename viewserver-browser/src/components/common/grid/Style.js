let nextStyleId = 0;
let nextFontId = 0;

/*
	Map for font styles
*/
const StyleFontMap = {};

/*
	Map fonts to string constants, this improves the speed at which we can
	compare fonts significantly.
*/
function getFontInfo(font) {
	return StyleFontMap[font] || (StyleFontMap[font] = {
		id: nextFontId++,
		font
	});
}

const StyleSchema = {
	fontFamily: {
		defaultValue: 'arial'
	},
	fontSize: {
		defaultValue: 11
	},
	fontStyle: {
		defaultValue: 'normal'
	},
	fontWeight: {
		defaultValue: 'normal'
	},
	textAlign: {
		defaultValue: 'start'
	},
	textShadowColor: {
		defaultValue: 'black'
	},
	textShadowOffsetX: {
		defaultValue: 0
	},
	textShadowOffsetY: {
		defaultValue: 0
	},
	textShadowBlur: {
		defaultValue: 0
	},
	lineWidth: {
		defaultvalue: 0.5
	},
	strokeStyle: {
		defaultValue: 'black'
	},
	color : {
		defaultValue: 'black'
	},
	background: {
		defaultValue: null
	},
	marginTop: {
		fallbackProperty: 'margin'
	},
	marginRight: {
		fallbackProperty: 'margin'
	},
	marginLeft: {
		fallbackProperty: 'margin'
	},
	marginBottom: {
		fallbackProperty: 'margin'
	},
	margin: {
		defaultValue: 0
	},
	borderWidth: {
		defaultValue: 0
	},
	borderTopWidth: {
		fallbackProperty: 'borderWidth'
	},
	borderBottomWidth: {
		fallbackProperty: 'borderWidth'
	},
	borderLeftWidth: {
		fallbackProperty: 'borderWidth'
	},
	borderRightWidth: {
		fallbackProperty: 'borderWidth'
	},
	borderColor: {
		defaultValue: 'black'
	},
	borderTopColor: {
		fallbackProperty: 'borderColor'
	},
	borderBottomColor: {
		fallbackProperty: 'borderColor'
	},
	borderLeftColor: {
		fallbackProperty: 'borderColor'
	},
	borderRightColor: {
		fallbackProperty: 'borderColor'
	}
};

/*
	All schema properties, sorted for predictable key generation and optimized
	Style object usage (v8 hidden classes).
*/
const StyleSchemaProperties = Object.keys(StyleSchema).sort();

/**
	Calculates the style value making use of the schema to select default,
	fallback values.
*/
function calculateProperty(style, propertyName) {
	if (style.hasOwnProperty(propertyName)) {
		return style[propertyName];
	}

	const schemaProperty = StyleSchema[propertyName];

	// if the property has a fallback then use that, otherwise use defaultValue
	const fallbackProperty = schemaProperty.fallbackProperty;
	if (fallbackProperty) {
		return style.hasOwnProperty(fallbackProperty) ?
			style[fallbackProperty] :
			StyleSchema[fallbackProperty].defaultValue;
	}
	return schemaProperty.defaultValue;
}

function validateProperties(style) {
	const { id, ...properties} = style;
	for (let key in properties) {
		if (!(key in StyleSchema)) {
			throw new Error(`Unknown style property '${key}'.`);
		}

		const styleProperty = properties[key];
		if ((styleProperty === null && StyleSchema[key].defaultValue !== null) || typeof styleProperty === 'undefined') {
			throw new Error(`Invalid style property value '${key}=${styleProperty}'.`);
		}

		const validator = StyleSchema[key].validator;
		if (validator) {
			validator(styleProperty);
		}
	}
	return properties;
}

/**
	For "read" performance reasons we always flatten all
	style properties. However we maintain "source" to
	ensure we can determine which properties were used
	to create the style.
 */
class Style {
	constructor(style) {
		// allocate unique id
		this.id = ++nextStyleId;

		// set properties in consistent order (v8 hidden classes)
		StyleSchemaProperties.forEach(property => {
			this[property] = calculateProperty(style, property);
		});

		// compile the font into a single value
		const fontInfo = getFontInfo(`${this.fontStyle} ${this.fontWeight} ${this.fontSize}px ${this.fontFamily}`);
		this.font = fontInfo.font;
		this.fontId = fontInfo.id;

		// persist the source so we can determine which properties where explicitly set for inheritance.
		this.source = {
			...style
		};

		// freeze (must be immutable)
		Object.freeze(this);
		Object.freeze(this.source);
	}

	isDefaultValue(propertyName) {
		return propertyName !== 'id' &&
			DEFAULT_STYLE[propertyName] === this[propertyName];
	}

	override(...styles) {
		return StyleApi.flatten.apply(StyleApi, [this].concat(styles));
	}
}

const DEFAULT_STYLE = new Style({});

const StyleApi = {
	key(style) {
		return StyleSchemaProperties.map(
			property => style[property]).join();
	},

	create(style) {
		if (style instanceof Style) {
			return style;
		}

		if (Object.keys(style).length === 0) {
			return DEFAULT_STYLE;
		}

		return new Style(validateProperties(style));
	},

	flatten(...styles) {
		return StyleApi.create(styles.reduce((style, next) => {
			if (next instanceof Style) {
				next = next.source;
			}
			return Object.assign(style, next);
		}, {}));
	},

	get DEFAULT() {
		return DEFAULT_STYLE;
	}
};

export default StyleApi;