/*
	Map of keys we can "reliably" (I hope) map in keyup and	keydown event to KeyboardEvent.key.
	Once we upgrade to a later version of chrome we won't need this.
*/
const _u = undefined;

const KeyboardMap = [
	_u, 		 		// [0]
	_u, 		 		// [1]
	_u, 		 		// [2]
	_u, 		 		// [3]
	_u, 		 		// [4]
	_u, 		 		// [5]
	_u, 		 		// [6]
	_u, 		 		// [7]
	"Backspace", 		// [8]
	"Tab", 		 		// [9]
	_u,	 	 			// [10]
	_u, 				// [11]
	_u,    				// [12]
	"Enter", 			// [13]
	_u,					// [14]
	_u, 				// [15]
	"Shift", 			// [16]
	"Control", 			// [17]
	"Alt", 				// [18]
	"Pause", 			// [19]
	"CapsLock", 		// [20]
	_u, 				// [21]
	_u,		 			// [22]
	_u, 				// [23]
	_u, 				// [24]
	_u,		 			// [25]
	_u, 				// [26]
	"Escape", 			// [27]
	_u,		 			// [28]
	_u,			 		// [29]
	_u, 				// [30]
	_u, 				// [31]
	" ", 				// [32]
	"PageUp", 			// [33]
	"PageDown", 		// [34]
	"End", 				// [35]
	"Home", 			// [36]
	"ArrowLeft", 		// [37]
	"ArrowUp", 			// [38]
	"ArrowRight", 		// [39]
	"ArrowDown", 		// [40]
	_u, 				// [41]
	_u, 				// [42]
	_u, 				// [43]
	"PrintScreen", 		// [44]
	"Insert", 			// [45]
	"Delete", 			// [46]
	_u, 				// [47]
	_u, 				// [48]
	_u, 				// [49]
	_u, 				// [50]
	_u, 				// [51]
	_u, 				// [52]
	_u, 				// [53]
	_u, 				// [54]
	_u, 				// [55]
	_u, 				// [56]
	_u, 				// [57]
	_u, 				// [58]
	_u, 				// [59]
	_u, 				// [60]
	_u, 				// [61]
	_u, 				// [62]
	_u, 				// [63]
	_u, 				// [64]
	'A', 				// [65]
	'B', 				// [66]
	'C', 				// [67]
	'D', 				// [68]
	'E', 				// [69]
	'F', 				// [70]
	'G', 				// [71]
	'H', 				// [72]
	'I', 				// [73]
	'J', 				// [74]
	'K', 				// [75]
	'L', 				// [76]
	'M', 				// [77]
	'N', 				// [78]
	'O', 				// [79]
	'P', 				// [80]
	'Q', 				// [81]
	'R', 				// [82]
	'S', 				// [83]
	'T', 				// [84]
	'U', 				// [85]
	'V', 				// [86]
	'W', 				// [87]
	'X', 				// [88]
	'Y', 				// [89]
	'Z', 				// [90]
	"Meta", 			// [91] Windows Key (Windows) or Command Key (Mac)
	_u,     			// [92]
	"ContextMenu", 		// [93]
	_u,     			// [94]
	_u,     			// [95]
	_u,    				// [96]
	_u,    				// [97]
	_u,    				// [98]
	_u,    				// [99]
	_u,    				// [100]
	_u,    				// [101]
	_u,    				// [102]
	_u,    				// [103]
	_u,    				// [104]
	_u,    				// [105]
	_u,    				// [106]
	_u,    				// [107]
	_u,     			// [108]
	_u,    				// [109]
	_u,    				// [110]
	_u,    				// [111]
	"F1",   			// [112]
	"F2",   			// [113]
	"F3",   			// [114]
	"F4",   			// [115]
	"F5",   			// [116]
	"F6",   			// [117]
	"F7",   			// [118]
	"F8",   			// [119]
	"F9",   			// [120]
	"F10",  			// [121]
	"F11",  			// [122]
	"F12",  			// [123]
	"F13",  			// [124]
	"F14",  			// [125]
	"F15",  			// [126]
	"F16",  			// [127]
	"F17",  			// [128]
	"F18",  			// [129]
	"F19",  			// [130]
	"F20",  			// [131]
	"F21",  			// [132]
	"F22",  			// [133]
	"F23",  			// [134]
	"F24"   			// [135]
];

function mapKey(e, isSafe) {
	switch (e.type) {
		case 'keypress':
			return mapKeyPress(e);
		case 'keyup':
		case 'keydown':
			return mapKeyUpDown(e, isSafe);
	}
}

/*
	Maps a keyboard press event to a key.
*/
function mapKeyPress(e) {
	return String.fromCharCode(e.charCode ? e.charCode : e.keyCode);
}

/*
	Maps a keyboard up/down event to a key.

	isSafe: Chars 65 to 90 are NOT safe to use in KeyboardEvent.key as the specification for this
	deals with casing which is impossible in our version of chrome as we cannot determine
	the sate of the caps lock key (We indicate this by us).
*/
function mapKeyUpDown(e, isSafe) {
	const code = e.charCode ? e.charCode : e.keyCode;
	return code >= 0 && code < KeyboardMap.length && (!isSafe || code < 65 || code > 90) ? KeyboardMap[code] : _u;
}

(function(global) {
	const prototype = global && global.KeyboardEvent && global.KeyboardEvent.prototype;
	if (prototype) {
		if (!('key' in prototype)) {
			Object.defineProperty(prototype, 'key', {
				get: function() {
					return mapKey(this, true);
				}
			});
		}
	}
})(window);

export function getShortcutKey(e) {
	// map key (allow unsafe alpha codes)
	let key = mapKey(e, false);
	if (!key) {
		return null;
	}

	// convert alpha chars to uppercase
	if (key.length === 1 && /[a-z]/i.test(key)) {
		key = key.toUpperCase();
	}

	const keys = [];
	if (e.ctrlKey || key === 'Control')  {
		keys.push('Control');
	}
	if (e.shiftKey || key === 'Shift')  {
		keys.push('Shift');
	}
	if (e.altKey || key === 'Alt')  {
		keys.push('Alt');
	}
	if (e.metaKey || key === 'Meta')  {
		keys.push('Meta');
	}
	if (key && keys.indexOf(key) === -1) {
		keys.push(key);
	}
	return keys.join('+');
}


