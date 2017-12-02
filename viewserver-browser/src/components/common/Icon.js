import React, {Component} from 'react';

export default (props) => (
    <div>
      <span className={"icon icon-" + props.icon}></span>
      {props.label}
    </div>
  );