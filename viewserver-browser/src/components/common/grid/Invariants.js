function error(message, value, paramName, condition) {
	message = message.replace('{value}', value);
	if (condition) {
		message += ' ' + condition;
	}
	return new Error(message + (paramName ? `, paramName: '${paramName}'.` : '.'));
}

const NumericErrorMessage = 'expected value \'{value}\' to be a numeric value';

export function numeric(value, paramName) {
	const result = Number(value);
	if (isNaN(value)) {
		throw error(NumericErrorMessage, value, paramName);
	}
	return result;
}

numeric.between = function(value, paramName, min = Number.MIN_VALUE, max = Number.MAX_VALUE) {
	const result = numeric(value, paramName);
	
	if (result < min || result > max) {
		if (max === Number.MAX_VALUE) {
			throw error(NumericErrorMessage, value, paramName, `greater than ${min}.`);
		}	
		
		if (min === Number.MIN_VALUE) {
			throw error(NumericErrorMessage, value, paramName, `less than ${max}.`);
		}

		throw error(NumericErrorMessage, value, paramName, `between ${min} and ${max}.`);
	}

	return result;	
}