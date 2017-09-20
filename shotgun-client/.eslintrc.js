const path = require('path');

module.exports = {
    parser: 'babel-eslint',
    parserOptions: {
        ecmaVersion: 8,
        sourceType: 'module',
        ecmaFeatures: {
            jsx: true
        }
    },
    extends: [
        'plugin:react/recommended'
    ],
    plugins: [
        'import',
        'react'
    ],
    rules: {
        'react/display-name': 0,
        'react/no-string-refs': 1,
        // =============================================================================
        // Import errors
        // =============================================================================
        'import/no-unresolved': 2,
        'import/named': 2,
        'import/namespace': 2,
        'import/default': 2,
        'import/export': 2,
        // =============================================================================
        // Possible Errors
        //
        // The following rules point out areas where you might have made mistakes.
        // =============================================================================
        'comma-dangle': 2, // disallow or enforce trailing commas
        'no-cond-assign': 2, // disallow assignment in conditional expressions
        'no-console': 0, // disallow use of console (off by default in the node environment)
        'no-constant-condition': 2, // disallow use of constant expressions in conditions
        'no-control-regex': 2, // disallow control characters in regular expressions
        'no-debugger': 2, // disallow use of debugger
        'no-dupe-args': 2, // disallow duplicate arguments in functions
        'no-dupe-keys': 2, // disallow duplicate keys when creating object literals
        'no-duplicate-case': 2, // disallow a duplicate case label.
        'no-empty': 1, // disallow empty statements
        'no-empty-character-class': 2, // disallow the use of empty character classes in regular expressions
        'no-ex-assign': 2, // disallow assigning to the exception in a catch block
        'no-extra-boolean-cast': 2, // disallow double-negation boolean casts in a boolean context
        'no-extra-parens': 0, // disallow unnecessary parentheses (off by default)
        'no-extra-semi': 2, // disallow unnecessary semicolons
        'no-func-assign': 2, // disallow overwriting functions written as function declarations
        'no-inner-declarations': 2, // disallow function or variable declarations in nested blocks
        'no-invalid-regexp': 2, // disallow invalid regular expression strings in the RegExp constructor
        'no-irregular-whitespace': 2, // disallow irregular whitespace outside of strings and comments
        'no-negated-in-lhs': 2, // disallow negation of the left operand of an in expression
        'no-obj-calls': 2, // disallow the use of object properties of the global object (Math and JSON) as functions
        'no-regex-spaces': 2, // disallow multiple spaces in a regular expression literal
        'quote-props': [2, 'as-needed', { 'keywords': true }],
        'no-sparse-arrays': 2, // disallow sparse arrays
        'no-unreachable': 2, // disallow unreachable statements after a return, throw, continue, or break statement
        'use-isnan': 2, // disallow comparisons with the value NaN
        'valid-jsdoc': 2, // Ensure JSDoc comments are valid (off by default)
        'valid-typeof': 2, // Ensure that the results of typeof are compared against a valid string
        // =============================================================================
        // These are rules designed to prevent you from making mistakes.
        // They either prescribe a better way of doing something or help you avoid footguns.
        // =============================================================================
        'block-scoped-var': 0, // treat var statements as if they were block scoped (off by default). 0: deep destructuring is not compatible https://github.com/eslint/eslint/issues/1863
        'complexity': 0, // specify the maximum cyclomatic complexity allowed in a program (off by default)
        'consistent-return': 2, // require return statements to either always or never specify values
        'curly': 2, // specify curly brace conventions for all control statements
        'default-case': 2, // require default case in switch statements (off by default)
        'dot-notation': 2, // encourages use of dot notation whenever possible
        'eqeqeq': 2, // require the use of === and !==
        'guard-for-in': 2, // make sure for-in loops have an if statement (off by default)
        'no-alert': 2, // disallow the use of alert, confirm, and prompt
        'no-caller': 2, // disallow use of arguments.caller or arguments.callee
        'no-div-regex': 2, // disallow division operators explicitly at beginning of regular expression (off by default)
        'no-else-return': 2, // disallow else after a return in an if (off by default)
        'no-eq-null': 2, // disallow comparisons to null without a type-checking operator (off by default)
        'no-eval': 2, // disallow use of eval()
        'no-extend-native': 2, // disallow adding to native types
        'no-extra-bind': 2, // disallow unnecessary function binding
        'no-fallthrough': 2, // disallow fallthrough of case statements
        'no-floating-decimal': 2, // disallow the use of leading or trailing decimal points in numeric literals (off by default)
        'no-implied-eval': 2, // disallow use of eval()-like methods
        'no-iterator': 2, // disallow usage of __iterator__ property
        'no-labels': 2, // disallow use of labeled statements
        'no-lone-blocks': 2, // disallow unnecessary nested blocks
        'no-loop-func': 2, // disallow creation of functions within loops
        'no-multi-spaces': 2, // disallow use of multiple spaces
        'no-multi-str': 2, // disallow use of multiline strings
        'no-native-reassign': 2, // disallow reassignments of native objects
        'no-new': 2, // disallow use of new operator when not part of the assignment or comparison
        'no-new-func': 2, // disallow use of new operator for Function object
        'no-new-wrappers': 2, // disallows creating new instances of String,Number, and Boolean
        'no-octal': 2, // disallow use of octal literals
        'no-octal-escape': 2, // disallow use of octal escape sequences in string literals, such as var foo = 'Copyright \251';
        'no-param-reassign': [0, { 'props': false }], // disallow reassignment of function parameters (off by default)
        'no-process-env': 2, // disallow use of process.env (off by default)
        'no-proto': 2, // disallow usage of __proto__ property
        'no-redeclare': 2, // disallow declaring the same variable more then once
        'no-return-assign': 2, // disallow use of assignment in return statement
        'no-script-url': 2, // disallow use of javascript: urls.
        'no-self-compare': 2, // disallow comparisons where both sides are exactly the same (off by default)
        'no-sequences': 2, // disallow use of comma operator
        'no-throw-literal': 2, // restrict what can be thrown as an exception (off by default)
        'no-unused-expressions': 2, // disallow usage of expressions in statement position
        'no-void': 2, // disallow use of void operator (off by default)
        'no-warning-comments': [0, {'terms': ['todo', 'fixme'], 'location': 'start'}], // disallow usage of configurable warning terms in comments': 2, // e.g. TODO or FIXME (off by default)
        'no-with': 2, // disallow use of the with statement
        'radix': 2, // require use of the second argument for parseInt() (off by default)
        'vars-on-top': 2, // requires to declare all vars on top of their containing scope (off by default)
        'wrap-iife': 2, // require immediate function invocation to be wrapped in parentheses (off by default)
        'yoda': 2, // require or disallow Yoda conditions
        // =============================================================================
        // Strict Mode
        //
        // These rules relate to using strict mode.
        // =============================================================================
        'strict': 0, // controls location of Use Strict Directives. 0: required by `babel-eslint`
        'no-catch-shadow': 2, // disallow the catch clause parameter name being the same as a variable in the outer scope (off by default in the node environment)
        'no-delete-var': 2, // disallow deletion of variables
        'no-label-var': 2, // disallow labels that share a name with a variable
        'no-shadow': 2, // disallow declaration of variables already declared in the outer scope
        'no-shadow-restricted-names': 2, // disallow shadowing of names such as arguments
        'no-undef': 2, // disallow use of undeclared variables unless mentioned in a /*global */ block
        'no-undef-init': 2, // disallow use of undefined when initializing variables
        'no-undefined': 0, // disallow use of undefined variable (off by default)
        'no-unused-vars': ["error", { "ignoreRestSiblings": true }], // disallow declaration of variables that are not used in the code
        'no-use-before-define': 2, // disallow use of variables before they are defined
    },
    globals: {
        'falcon': true
    },
    env: {
        'browser': true,
        'node': true,
        'es6': true
    },
    settings: {
        'import/resolver':{ 
            webpack: {
                config:{
                    resolve:{
                        extensions: ['.js','.jsx']
                    }
                }
            }
        },
        react : {
            version: '0.14.7'
        }
    }
}