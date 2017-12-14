import React, {Component, PropTypes, cloneElement} from 'react';
import EventSource from './EventSource'
const listeners = [];
let isNotifying = false;

const observer = new MutationObserver(function() {
    if (!isNotifying) {
        isNotifying = true;
        setTimeout(function() {
            isNotifying = false;
            const notifications = listeners.slice();
            for (let i=0, n=notifications.length; i<n; i++) {
                notifications[i]();
            }
        }, 0);
    }
});



function addListener(listener) {
    if (listeners.push(listener) === 1) {
        observer.observe(document, {
            attributes: true,
            childList: true,
            characterData: true,
            subtree: true
        });
    }
}

function removeListener(listener) {
    const index = listeners.indexOf(listener);
    if (index !== -1) {
        listeners.splice(index, 1);
        if (listeners.length) {
            observer.disconnect();
        }
    }
}

export default class Autosizer extends Component {
    static propTypes = {
        onResize: PropTypes.func.isRequired,
        children: React.PropTypes.oneOfType([
            React.PropTypes.arrayOf(React.PropTypes.node),
            React.PropTypes.node
        ])
    }

    constructor(props, context) {
        super(props, context);
        this._clientWidth = 0;
        this._clientHeight = 0;
        this._isPending = false;
        this._update = this._update.bind(this);
        this._bindElement = this._bindElement.bind(this);
        this.element = null;
        this.disposables = [];
        this.disposables.push(EventSource.fromDOM(window,'resize',this._update).subscribe(this._update));
        
    }

    _update() {
        const {element} = this;
        if (element) {
            if (this._clientHeight !== element.clientHeight || this._clientWidth !== element.clientWidth) {
                this._clientHeight = element.clientHeight;
                this._clientWidth = element.clientWidth;
                const { onResize } = this.props;
                if (onResize) {
                    onResize();
                }
            }
        }
    }

    _bindElement(element) {
        this.element = element;
    }

    componentDidMount() {
        addListener(this._update);
    }

    componentWillUnmount() {
        removeListener(this._update);
        this.disposables.forEach(c=> c.unsubscribe());
    }

    render() {
        let host = React.Children.only(this.props.children);
        host = cloneElement(host, {
            ref: this._bindElement
        });
        return host;
    }
}