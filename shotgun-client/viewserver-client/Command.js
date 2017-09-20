export default class Command {
    constructor(command,data) {
        this._id = -1;
        this._handler = undefined;
        this._command = command;
        this._data = data;
        this._continuous = false;
    }

    get id() {
        return this._id;
    }

    get handler() {
        return this._handler;
    }

    set handler(value) {
        this._handler = value;
    }

    get command() {
        return this._command;
    }

    get data() {
        return this._data;
    }

    get continuous() {
        return this._continuous;
    }

    set continuous(value) {
        this._continuous = value;
    }
}