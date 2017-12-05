import React, { Component } from 'react';
import HeaderRow from './HeaderRow';

export default class Header extends Component {
    render() {
        return <HeaderRow {...this.props}></HeaderRow>;
    }
}