import {Rx} from 'common/rx'
const fns = [function (es) {
    for (let i = 0, n = es.length; i < n; i++) {
        const e = es[i];
        if (e.ctx === undefined) {
            (0, e.fn)();
        } else {
            e.fn.call(e.ctx);
        }
    }
}, function (es, a1) {
    for (let i = 0, n = es.length; i < n; i++) {
        const e = es[i];
        if (e.ctx === undefined) {
            (0, e.fn)(a1);
        } else {
            e.fn.call(e.ctx, a1);
        }
    }
}, function (es, a1, a2) {
    for (let i = 0, n = es.length; i < n; i++) {
        const e = es[i];
        if (e.ctx === undefined) {
            (0, e.fn)(a1, a2);
        } else {
            e.fn.call(e.ctx, a1, a2);
        }
    }
}, function (es, a1, a2, a3) {
    for (let i = 0, n = es.length; i < n; i++) {
        const e = es[i];
        if (e.ctx === undefined) {
            (0, e.fn)(a1, a2, a3);
        } else {
            e.fn.call(e.ctx, a1, a2, a3);
        }
    }
}, function (es, a1, a2, a3, a4) {
    for (let i = 0, n = es.length; i < n; i++) {
        const e = es[i];
        if (e.ctx === undefined) {
            (0, e.fn)(a1, a2, a3, a4);
        } else {
            e.fn.call(e.ctx, a1, a2, a3, a4);
        }
    }
}, function (es, a1, a2, a3, a4, a5) {
    for (let i = 0, n = es.length; i < n; i++) {
        const e = es[i];
        if (e.ctx === undefined) {
            (0, e.fn)(a1, a2, a3, a4, a5);
        } else {
            e.fn.call(e.ctx, a1, a2, a3, a4, a5);
        }
    }
}, function (es, args) {
    for (let i = 0, n = es.length; i < n; i++) {
        const e = es[i];
        e.fn.apply(e.ctx, args);
    }
}];

const dispatchFactories = [
    () => {
        const fn = fns[0];
        return function () {
            this._dispatches++;
            try {
                fn(this._entries);
            }
            finally {
                if (this._dispatches-- === this._mutates) {
                    this._mutates--;
                }
            }
        }
    },
    () => {
        const fn = fns[1];
        return function (a) {
            this._dispatches++;
            try {
                fn(this._entries, a);
            }
            finally {
                if (this._dispatches-- === this._mutates) {
                    this._mutates--;
                }
            }
        }
    },
    () => {
        const fn = fns[2];
        return function (a, b) {
            this._dispatches++;
            try {
                fn(this._entries, a, b);
            }
            finally {
                if (this._dispatches-- === this._mutates) {
                    this._mutates--;
                }
            }
        }
    },
    () => {
        const fn = fns[3];
        return function(a, b, c) {
            this._dispatches++;
            try {
                fn(this._entries, a, b, c);
            }
            finally {
                if (this._dispatches-- === this._mutates) {
                    this._mutates--;
                }
            }
        }
    },
    () => {
        const fn = fns[4];
        return function(a, b, c, d) {
            this._dispatches++;
            try {
                fn(this._entries, a, b, c, d);
            }
            finally {
                if (this._dispatches-- === this._mutates) {
                    this._mutates--;
                }
            }
        }
    },
    () => {
        const fn = fns[5];
        return function(a, b, c, d, e) {
            this._dispatches++;
            try {
                fn(this._entries, a, b, c, d, e);
            }
            finally {
                if (this._dispatches-- === this._mutates) {
                    this._mutates--;
                }
            }
        }
    },
    () => {
        const fn = fns[6];
        return function (...args) {
            this._dispatches++;
            try {
                fn(this._entries, args);
            }
            finally {
                if (this._dispatches-- === this._mutates) {
                    this._mutates--;
                }
            }
        }
    }
];

function mutating(source) {
    // if mutating during dispatch then we need to clone the array
    if (source._dispatches !== 0 && source._dispatches !== source._mutates) {
        source._mutates = source._dispatches;
        source._entries = source._entries.slice(0);
    }
}

function remove(source, index) {
    if (index !== -1) {
        mutating(source);
        source._entries.splice(index, 1);
        source.entriesChanged();
    }
    return index !== -1;
}

class EventEntry {
    constructor(source, fn, ctx) {
        this.fn = fn;
        this.ctx = ctx;
        this.source = source;
    }

    unsubscribe() {
        const { source } = this;
        if (source) {
            remove(source, source._entries.indexOf(this));
            this.source = null;
        }
    }
}

class Event {
    constructor(source) {
        this._source = source;
    }
    add(handler, context) {
        return this._source.add(handler, context);
    }
    remove(handler, context) {
        return this._source.remove(handler, context);
    }
}

function findIndex(entries, fn, ctx) {
    for (let i = 0, n = entries.length; i < n; i++) {
        const entry = entries[i];
        if (entry.fn === fn && entry.ctx === ctx) {
            return i;
        }
    }
    return -1;
}

export default class EventSource {
    constructor(argCount) {
        this._entries = [];
        this._dispatches = 0;
        this._mutates = 0;
        if (isNaN(argCount) || argCount < 0) {
            argCount = dispatchFactories.length - 1;
        }
        this.dispatch = dispatchFactories[Math.min(argCount, 6)]();
        this.event = new Event(this);
    }
    entriesChanged() {}
    add(handler, context) {
        if (typeof handler !== 'function') {
            throw new Error('value must be a function, arg: handler');
        }
        mutating(this);
        const entry = new EventEntry(this, handler, context);
        this._entries.push(entry);
        this.entriesChanged();
        return entry;
    }
    remove(handler, context) {
        return remove(this, findIndex(this._entries, handler, context));
    }
    get isSubscribed() {
        return this._entries.length > 0;
    }
    /**
     * Creates an EventSource bound to the specified DOM element and event.
     * @static
     * @deprecated This method needs review as it makes a number of assumptions.
     * @param {HTMLElement} element The dom element to create an event source for
     * @param {string} event The event type
     * @param {boolean} capture whether to capture
     * @param {function} transform a  function that can be used to transform the raw event
     * @returns {EventSource} The event source
     */
    static fromDOM(element, event, capture, transform) {
        return Rx.Observable.create(observer => {
            const dispatch = transform ? function (e) {
                const transformed = transform(e);
                if (transformed) {
                    observer.next(transformed);
                }
            } : (ev) => observer.next(ev);
            element.addEventListener(event, dispatch, false);
            return () => {
                element.removeEventListener(event, dispatch);
            };
        });
        return result;
    }

}


