import {
	isEqual as deepIsEqual
}
from 'lodash';

const isArrayEqual = (arrayA, arrayB) => {
	if (arrayA.length !== arrayB.length) {
		return false;
	}
	for (let i = 0, n = arrayA.length; i < n; i++) {
		if (arrayA[i] !== arrayB[i]) {
			return false;
		}
	}
	return true;
};

/**
 * Gets a value indicating whether the specified values are considered "equal"
 * @function
 * @param  {any} objA The first object to compare
 * @param  {any} objB The second object to compare
 * @param  {boolean} [deep=false] A value indicating whether the comparison should be deep.
 * @return {boolean}        true if the values are considered equal; otherwise, false.
 */
export const isEqual = (objA, objB, deep) => {
	if (deep) {
		return deepIsEqual(objA, objB);
	}

	// eslint-disable-next-line no-self-compare
	if (objA === objB || (objA !== objA && objB !== objB)) { // covers NaN
		return true;
	}
	
	if (!objA || !objB || typeof objA !== 'object' || typeof objB !== 'object') {
		return false;
	}
	
	// handle array case
	if (Array.isArray(objA)) {
		return Array.isArray(objB) && isArrayEqual(objA, objB);
	} else if (Array.isArray(objB)) {
		return false;
	}

	const keysA = Object.keys(objA);
	const keysB = Object.keys(objB);
	if (keysA.length !== keysB.length) {
		return false;
	}

	for (let i = 0, n = keysA.length; i < n; i ++) {
		const key = keysA[i];
		if (!objB.hasOwnProperty(key) || objA[key] !== objB[key]) {
			return false;
		}
	}

	return true;
};

export default isEqual;
