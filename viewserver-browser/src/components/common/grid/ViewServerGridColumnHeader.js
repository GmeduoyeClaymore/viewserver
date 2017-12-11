import React, { Component } from 'react';

/* eslint react/prop-types: 0 */

const InputStyle = {
    width: '100%',
    border: 'none',
    background: 'rgb(24,31,37)',
    boxShadow: 'inset 0px 0px 4px 1px rgba(0,0,0,.8), inset 0 1px 0 0 rgba(255,255,255,.15),inset 0 -1px 0 0 rgba(255,255,255,.15)',
    padding: '0 3px'
}

const bindOnChange = (column, onChange) => (e) => {
    onChange({
        target: {
            value: {
                column,
                style: e.target.value
            }
        }
    })
};

const StyleTrigger = () => (
    <span className="canv-grid__header__cell__settings material-icons md-12 md-light m1r" title="click for settings...">
        more_vert
    </span>
);

class ViewServerGridColumnHeaderContent extends Component {
    constructor(props) {
        super(props);
        this.handleFilterChanged = this.handleFilterChanged.bind(this);
        this.handleFilterClick = this.handleFilterClick.bind(this);
        this.recalcStyle(props);
    }

    handleFilterChanged(e) {
        e.persist();
        const { onFilterChange } = this.props;
        if (onFilterChange) {
            onFilterChange({
                column: this.props.column,
                filter: e.target.value
            });
        }
    }

    handleFilterClick(e) {
        e.stopPropagation();
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.height !== this.props.height || nextProps.showFilter !== this.props.showFilter) {
            this.recalcStyle(nextProps);
        }
    }

    recalcStyle({ showFilter, height }) {
        this.style = { height: showFilter ? height * 2 : height, whiteSpace: 'nowrap' };
    }

    render = () => {
        const {
            column,
            filters,
            showFilter
        } = this.props;

        const filter = filters && filters[column.key];
        return showFilter ? (
            <div className="ViewServerGridColumnHeader flex" style={this.style}>
                {this.renderTitle()}
                <div className="display-flex flex">
                    <input onClick={this.handleFilterClick} style={InputStyle} onChange={this.handleFilterChanged} value={filter} />
                </div>
            </div>
        ) : this.renderTitle();
    }

    renderStyleEditor = (column, onColumnStyleUpdated) => {
        return null;
    }

    renderTitle = () => {
        const {
            column,
            onColumnStyleUpdated,
            renderTitle
        } = this.props;

        return (
            <div className="header-container display-flex flex justify-space-between">
                {renderTitle(column, this.props)}
                {this.renderStyleEditor(column, onColumnStyleUpdated)}
            </div>
        );
    }
}

export const renderColumnHeaderContent = props => <ViewServerGridColumnHeaderContent {...props} />