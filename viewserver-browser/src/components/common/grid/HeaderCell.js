import React, { Component } from 'react';
import cx from 'classnames';
import Draggable from './Draggable';
import { isEqual } from 'lodash';

const MINIMUM_COLUMN_WIDTH = 10;

const DefaultRender = ({ renderTitle, column }) => {
    return renderTitle(column);
}

const createTooltip = (colName, description) => description && description.length ? `${colName} - ${description}` : colName;

export default class HeaderCell extends Component {
    static defaultProps = {
        rowHeight: 24,
        renderTitle: column => <span className="canv-grid__header__cell__title">{column.title || column.name || column.key}</span>
    };

    constructor(props) {
        super(props);
        this.handleDoubleClick = this.handleDoubleClick.bind(this);
        this.handleDrag = this.handleDrag.bind(this);
        this.handleDragStart = this.handleDragStart.bind(this);
        this.handleDragEnd = this.handleDragEnd.bind(this);
        this.handleClick = this.handleClick.bind(this);
        this.handleContextMenu = this.handleContextMenu.bind(this);
        this.state = {
            isResizing: false
        }
    }

    handleDragStart() {
        this.setState({
            isResizing: true
        });

        // return dragObject used to track drag
        return {
            width: this.props.width
        };
    }

    handleDrag(e) {
        const { onResize, column } = this.props;
        if (onResize) {
            var width = Math.max(column.minWidth || 0, e.dragObject.width + e.x, MINIMUM_COLUMN_WIDTH);
            if (width !== column.width) {
                onResize(column, width);
            }
        }
    }

    handleDoubleClick(e) {
        const { onAutoSize, column } = this.props;
        if (onAutoSize) {
            onAutoSize(column);
        }
    }

    handleDragEnd(e) {
        const { onResizeEnd, column } = this.props;
        if (onResizeEnd) {
            var width = Math.max(column.minWidth || 0, e.dragObject.width + e.x);
            onResizeEnd(column, width);
            this.setState({
                isResizing: false
            });
        }
    }

    handleClick(e) {
        if (!this.state.isResizing) {
            const { onClick, column } = this.props;
            if (onClick) {
                onClick(column, e);
            }
        }
    }

    renderCellContent(props) {
        const { renderProps, ...rest } = this.props;
        const render = this.props.render || DefaultRender;
        return React.createElement(render, {
            ...renderProps,
            ...rest
        });
    }

    renderDragHandle() {
        if (this.props.column.resizable) {
            return <Draggable className="canv-grid__header__cell__resize-handle"
                onDoubleClick={this.handleDoubleClick}
                onDrag={this.handleDrag}
                onDragStart={this.handleDragStart}
                onDragEnd={this.handleDragEnd}></Draggable>
        }
        return null;
    }

    handleContextMenu(e) {
        const { onContextMenu, column } = this.props;
        if (onContextMenu) {
            onContextMenu(column, e);
        }
    }

    shouldComponentUpdate(nextProps) {
        const { column, width, height, className, renderTitle, renderProps } = this.props;
        return nextProps.width !== width || nextProps.height !== height || nextProps.column !== column ||
            nextProps.renderTitle !== renderTitle || !isEqual(renderProps, nextProps.renderProps);
    }

    render() {
        const { column, width, height, className, fontSize, onColumnStyleUpdated } = this.props;

        const handleClick = this.handleClick;

        // get cell class
        const cellClassName = cx('canv-grid__header__cell', 'canv-grid__header__cell-sortable', column.className, className);

        // calculate header cell styles depending on height
        const headerStyle = {
            width: width + 'px'
        };

        const cellContentStyle = {
            lineHeight: height + 'px',
            fontSize: fontSize,
            paddingLeft: sortIndicator && 14
        };

        const sortIndicator = column.sorted && column.sortable ? ' ' + (column.sorted === 'asc' ? '↑' : '↓') : '';

        // render
        return <div className={cx(cellClassName, 'HeaderCell')}
            style={headerStyle}
            onContextMenu={this.handleContextMenu}>
            <div className="canv-grid__header__cell__sort-indicator">{sortIndicator}</div>
            <div data-test-column-name={column.name}
                className="canv-grid__header__cell__content display-flex"
                style={cellContentStyle}
                title={createTooltip(column.title || column.name || column.key, column.description)}
                onClick={handleClick}>
                {this.renderCellContent({ cellContentStyle })}
            </div>
            {column.sortAbs ? <div className="canv-grid__header__cell__abs-indicator">ABS</div> : null}
            {this.renderDragHandle()}
        </div>;
    }
}