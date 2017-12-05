import React, { Component, PropTypes } from 'react';

function eventHandled(e) {
    e.preventDefault();
    e.stopPropagation();
}

function dragEvent(draggable, e) {
    eventHandled(e);

    // update coordinates and calculate delta
    const { clientX, clientY } = e;
    const { dragCoordinates } = draggable;
    const deltaX = clientX - dragCoordinates.clientX;
    const deltaY = clientY - dragCoordinates.clientY;
    dragCoordinates.clientX = clientX;
    dragCoordinates.clientY = clientY;

    // notify
    return {
        dragObject: draggable.state.dragObject,
        x: dragCoordinates.clientX - dragCoordinates.startX,
        y: dragCoordinates.clientY - dragCoordinates.startY,
        deltaX,
        deltaY
    };
}

export default class Draggable extends Component {
    static propTypes = {
        onDragStart: PropTypes.func,
        onDragEnd: PropTypes.func,
        onDrag: PropTypes.func,
        component: PropTypes.oneOfType([PropTypes.func, PropTypes.constructor])
    };

    constructor(props) {
        super(props);

        // bind events
        this.handleMouseDown = this.handleMouseDown.bind(this);
        this.handleMouseUp = this.handleMouseUp.bind(this);
        this.handleMouseMove = this.handleMouseMove.bind(this);

        // intialize state
        this.state = {
            dragObject: null
        }

        this.dragCoordinates = null;
    }

    render() {
        const { component:Component, ...props } = this.props;
        return Component ?
            <Component onMouseDown={this.handleMouseDown} onDoubleClick={this.handleDoubleClick} {...props} /> :
            <div onMouseDown={this.handleMouseDown} onDoubleClick={this.handleDoubleClick} {...props} />;
    }

    startDrag() {
        const { onDragStart } = this.props;

        let dragObject = onDragStart && onDragStart();
        dragObject = dragObject === null || typeof dragObject === 'undefined' ?
            null :
            dragObject
        this.setState({
            dragObject
        });

        return dragObject !== null;
    }

    drag(e) {
        const { onDrag } = this.props;
        if (onDrag) {
            onDrag(e);
        }
    }

    endDrag(e) {
        this.removeEventListeners();
        this.setState({
            dragObject: null
        });

        const { onDragEnd } = this.props;
        if (onDragEnd) {
            onDragEnd(e);
        }
    }

    handleMouseDown(e) {
        eventHandled(e);
        if (!this.isDragging && this.startDrag()) {
            this.dragCoordinates = {
                startX: e.clientX,
                startY: e.clientY,
                clientX: e.clientX,
                clientY: e.clientY,
            };
            this.addEventListeners();
        }
    }

     handleDoubleClick(e) {
         eventHandled(e);
        const { onDoubleClick } = this.props;
        if (onDoubleClick) {
            onDoubleClick(e);
        }
     }

    handleMouseMove(e) {
        if (this.isDragging) {
            this.drag(dragEvent(this, e));
        }
    }

    handleMouseUp(e) {
        if (this.isDragging) {
            this.endDrag(dragEvent(this, e));
        }
    }

    get isDragging() {
        return this.state.dragObject !== null;
    }

    componentWillUnmount() {
        this.removeEventListeners();
    }

    addEventListeners() {
        // ensure we don't double subscribe
        this.removeEventListeners();
        window.addEventListener('mouseup', this.handleMouseUp);
        window.addEventListener('mousemove', this.handleMouseMove);
    }

    removeEventListeners() {
        window.removeEventListener('mouseup', this.handleMouseUp);
        window.removeEventListener('mousemove', this.handleMouseMove);
    }
}