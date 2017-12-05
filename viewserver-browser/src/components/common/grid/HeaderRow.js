import React, { Component } from 'react';
import HeaderCell from './HeaderCell';
import cx from 'classnames';

export default class HeaderRow extends Component {
	static defaultProps = {
		height: 24
	};

	render() {
		const { width, height, scrollLeft } = this.props;
		const style = {
			width: width || 'auto',
			whiteSpace: 'nowrap',
			position: 'relative',
			left: scrollLeft + 'px'
		};
		const className = cx('canv-grid__header__row');
		return (
			<div className={className} style={{ flex: 'none' }}>
				<div style={style}>
					{this.renderCells()}
				</div>
			</div>
		);
	}

	renderCells() {
		const {
			onColumnAutoSize,
			onColumnResize,
			onColumnResizeEnd,
			onColumnHeaderClick,
			onColumnHeaderContextMenu,
			height,
			fontSize,
			columns,
			columnMetrics,
			renderCell,
			renderCellProps,
			renderCellTitle
		} = this.props;

		return columns
			.filter(column => column.isVisible !== false)
			.map((column, i) => <HeaderCell
				key={i}
				column={column}
				onAutoSize={onColumnAutoSize}
				onResize={onColumnResize}
				onResizeEnd={onColumnResizeEnd}
				onClick={onColumnHeaderClick}
				onContextMenu={onColumnHeaderContextMenu}
				width={columnMetrics.getWidth(i)}
				height={height}
				fontSize={fontSize}
				render={renderCell}
				renderProps={renderCellProps}
				renderTitle={renderCellTitle} />);
	}
}