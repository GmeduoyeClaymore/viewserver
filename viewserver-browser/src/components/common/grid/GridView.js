import React, { Component } from 'react';
import ReactDom from 'react-dom';
import Grid from './Grid';
import Header from './Header';
import ColumnDefinition from './ColumnDefinition';
import ColumnMetrics from './ColumnMetrics';
import { RowRangeSelectionBehavior } from './RowRangeSelection';
import cx from 'classnames';
import Autosizer from './Autosizer';

// TODO: hack
const style = { display: 'flex', flexDirection: 'column', flex: '1 1 auto', overflow: 'hidden', position: 'relative' };

const getTargetFontSize = ({ fontSize, rowHeight }) => (fontSize || rowHeight || 24) - 8;

const createTempSpan = (props) => {
    const tempSpan = document.createElement('span');
    tempSpan.setAttribute('style', `display:inline-block; opacity:0; position:absolute;font-size:${getTargetFontSize(props)}px`);
    document.body.appendChild(tempSpan);
    return tempSpan;
}

export default class GridView extends Component {
    static defaultProps = {}

    constructor(props) {
        super(props);

        this.state = this.mergeState(props);
        this.handleColumnAutoSize = this.handleColumnAutoSize.bind(this);
        this.handleColumnResize = this.handleColumnResize.bind(this);
        this.handleColumnResizeEnd = this.handleColumnResizeEnd.bind(this);
        this.handleColumnHeaderClick = this.handleColumnHeaderClick.bind(this);
        this.handleColumnHeaderHeaderContextMenu = this.handleColumnHeaderHeaderContextMenu.bind(this);
        this.handleCopy = this.handleCopy.bind(this);
        this.handleFocus = this.handleFocus.bind(this);
        this.handleBlur = this.handleBlur.bind(this);
        this._suspendRenderHandle = null;
    }

    mergeState(props) {
        const columns = (props.columns && ColumnDefinition.from(props.columns)) || [];
        return {
            columns,
            columnInfo: {
                // set default resize index
                resizeIndex: -1,
                // inherit any existing state
                ...(this.state && this.state.columnInfo),
                // override metrics as columns have changed
                metrics: new ColumnMetrics(columns)
            },
            scrollLeft: this.state && this.state.scrollLeft || 0
        }
    }

    handleClick(cellEvent) {
        const { onClick } = this.props;
        if (onClick) {
            onClick(cellEvent);
        }
    }

    handleDblClick(cellEvent) {
        const { onDblClick } = this.props;
        if (onDblClick) {
            onDblClick(cellEvent);
        }
    }

    handleSelectionChanged() {
        const { onSelectionChanged } = this.props;
        if (onSelectionChanged) {
            onSelectionChanged({
                component: this
            });
        }
    }

    handleColumnHeaderClick(col) {
        const { onColumnHeaderClick } = this.props;
        if (onColumnHeaderClick) {
            onColumnHeaderClick(col);
        }
    }

    handleColumnHeaderHeaderContextMenu(column, e) {
        const { onColumnHeaderContextMenu } = this.props;
        if (onColumnHeaderContextMenu) {
            onColumnHeaderContextMenu({
                trigger: e,
                column
            });
        }
    }

    autoSizeColumn = (colIndex, tempSpan) => {
        const { columns } = this.state;
        const column = columns[colIndex];

        if (!column.resizable) {
            return;
        }

        const dataSource = this.grid && this.grid.dataSource;

        let destroyTempSpan = false;
        if (!tempSpan) {
            tempSpan = createTempSpan(this.props);
            destroyTempSpan = true;
        }

        tempSpan.innerText = `${column.title}00000`; // extra 00 because we need to make room for the settings cog icon
        const desiredHeaderWidth = tempSpan.offsetWidth;
        let longestCharCount = 0, longestString = '';
        for (let i = dataSource.view.rowFrom; i < dataSource.view.rowToExclusive; i++) {
            const row = dataSource.get(i);
            const value = row && row[column.key];
            if (value !== null && typeof value !== 'undefined') {
                const string = String(value);
                if (string.length > longestCharCount) {
                    longestCharCount = string.length;
                    longestString = string;
                }
            }
        }

        tempSpan.innerText = `${longestString}000`; // 000 to create a bit of extra room, we don't want the text touching the side
        this.setColumnWidth(colIndex, Math.min(450, Math.max(desiredHeaderWidth, tempSpan.offsetWidth)));

        if (destroyTempSpan) {
            tempSpan.remove();
        }
    }

    autoSizeColumns = () => {
        const { columns } = this.state;
        const tempSpan = createTempSpan(this.props);
        columns.forEach((x, i) => this.autoSizeColumn(i, tempSpan));
        tempSpan.remove();
    }

    setColumnWidth(colIndex, width) {
        const { columnInfo } = this.state;
        if (columnInfo) {
            columnInfo.metrics.setWidth(colIndex, width);
            this.setState({ columnInfo },
                () => {
                    const { onColumnResized } = this.props;
                    if (onColumnResized) {
                        onColumnResized({ index: colIndex, width });
                    }

                    this.updateLayout();
                }
            );
        }
    }

    handleColumnAutoSize(col) {
        const { columns } = this.state;
        this.autoSizeColumn(columns.indexOf(col));
    }

    handleColumnResize(col, width) {
        let { columnInfo, columns } = this.state;
        let updated = false;

        if (columnInfo.resizeIndex === -1 || columns[columnInfo.resizeIndex] !== col) {
            this._suspendRenderHandle = this._suspendRenderHandle || this.grid.suspendRendering();
            columnInfo = {
                ...columnInfo,
                resizeIndex: columns.indexOf(col)
            };
            updated = true;
        }

        if (columnInfo.metrics.setWidth(columnInfo.resizeIndex, width) && !updated) {
            columnInfo = {
                ...columnInfo
            };
            updated = true;
        }

        if (updated) {
            this.setState({
                columnInfo
            });
        }
    }

    handleColumnResizeEnd() {
        const { columnInfo } = this.state;
        const { onColumnResized } = this.props;

        if (columnInfo.resizeIndex !== -1) {
            this._suspendRenderHandle.dispose();
            this._suspendRenderHandle = null;

            this.setState({
                columnInfo: {
                    ...columnInfo,
                    resizeIndex: -1
                }
            });

            if (onColumnResized) {
                onColumnResized({
                    index: columnInfo.resizeIndex,
                    width: columnInfo.metrics.getWidth(columnInfo.resizeIndex)
                });
            }
        }

        this.updateLayout();
    }

    // TODO: rename to selection or add support for getting at the data into the selecto object
    get selected() {
        return this.selectionBehavior && this.selectionBehavior.selector;
    }

    get selectedItems() {
        const { dataSource } = this.props;
        return this.selected.count > 0 ?
            this.selected.getRanges().reduce((result, range) => {
                for (let i = range.start; i <= range.end; i++) {
                    result.push(dataSource.get(i));
                }
                return result;
            }, []) : [];
    }

    //No longer needs to be public.
    //There may be a couple of places left that are calling this but I have removed some alreaady.
    updateLayout = (e) => {
        if (this.grid) {
            this.grid.updateLayout();
        }
    }

    componentWillReceiveProps(nextProps) {
        const options = {
            ...nextProps,
            // ensure no one is messing things up by passing in metrics
            columnMetrics: this.state.columnInfo.metrics
        };

        // as actual columns can be derived we need to merge state first
        if (this.props.columns !== nextProps.columns) {
            const state = this.mergeState(nextProps)
            this.setState(state);

            // now set the final state metrics/columns
            options.columnMetrics = state.columnInfo.metrics;
            options.columns = state.columns;
        }

        // let grid process changes
        this.grid.setOptions(options);
    }

    componentDidMount() {
        this.selectionBehavior = new RowRangeSelectionBehavior();
        this.selectionBehavior.onDragStart.subscribe(this.handleSelectionDragStart.bind(this));

        const grid = this.grid = new Grid(this.refs.container.element, {
            ...this.props,
            // use state derived columns and metrics
            columns: this.state.columns,
            columnMetrics: this.state.columnInfo.metrics,
            behaviors: [
                this.selectionBehavior
            ],
            container: this.refs.root
        });

        // bind to grid events
        grid.onContextMenu.subscribe(this.handleContextMenu.bind(this));
        grid.onClick.subscribe(this.handleClick.bind(this));
        grid.onScroll.subscribe(this.handleScroll.bind(this));
        grid.onDblClick.subscribe(this.handleDblClick.bind(this));
        grid.onSelectionChanged.subscribe(this.handleSelectionChanged.bind(this));
        grid.onMouseDown.subscribe(this.handleMouseDown.bind(this));

        this.refs.root.addEventListener('copy', this.handleCopy);
    }

    ensureColumnSizes() {
        const { grid } = this;
        if (grid.dataSource.size === 0) {
            const eventEntry = this.grid.dataSource.onChanged.add(() => {
                if (grid.dataSource.size > 0) {
                    this.autoSizeColumns();
                    eventEntry.unsubscribe();
                }
            });
        }
    }

    handleSelectionDragStart(e) {
        const { onSelectionDragStart } = this.props;
        if (onSelectionDragStart) {
            onSelectionDragStart(e);
        }
    }

    handleMouseDown(e) {
        const { onMouseDown } = this.props;
        if (onMouseDown) {
            onMouseDown(e);
        }
    }

    handleScroll() {
        this.setState({
            scrollLeft: -this.grid.viewPort.left
        });
    }

    handleContextMenu(e) {
        const { onContextMenu } = this.props;
        if (onContextMenu) {
            onContextMenu(e);
        }
    }

    componentWillUnmount() {
        const { grid } = this;
        if (grid) {
            grid.destroy();
        }
    }

    handleCopy(e) {
        e.preventDefault();
        const { onCopy } = this.props;
        if (onCopy) {
            onCopy();
        }
    }

    handleFocus(e) {
        this.setState({
            focused: true
        });
    }

    handleBlur(e) {
        this.setState({
            focused: false
        });
    }

    render() {
        const { columns, columnInfo, scrollLeft } = this.state;
        const { rowHeight, headerHeight, fontSize, renderHeaderCell, renderHeaderCellProps, renderHeaderCellTitle, dataTestType } = this.props;
        const className = cx('canv-grid-container', {
            'canv-grid-container--focused': this.state.focused
        });
        return <div
            ref="root"
            className={className}
            tabIndex="0"
            onFocus={this.handleFocus}
            onBlur={this.handleBlur}
            style={style} >
            <Header
                ref={header => this.header = header}
                columns={columns}
                renderCell={renderHeaderCell}
                renderCellProps={renderHeaderCellProps}
                renderCellTitle={renderHeaderCellTitle}
                columnMetrics={columnInfo.metrics}
                height={headerHeight || rowHeight || 24}
                fontSize={getTargetFontSize({ fontSize, rowHeight })}
                scrollLeft={scrollLeft}
                onColumnAutoSize={this.handleColumnAutoSize}
                onColumnResize={this.handleColumnResize}
                onColumnResizeEnd={this.handleColumnResizeEnd}
                onColumnHeaderClick={this.handleColumnHeaderClick}
                onColumnHeaderContextMenu={this.handleColumnHeaderHeaderContextMenu}>
            </Header>
            <Autosizer ref="container" onResize={this.updateLayout}>
                <div className="canv-grid-content-body" style={style} />
            </Autosizer>
        </div>;
    }
}
