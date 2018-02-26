import React from 'react';
import {InteractiveForceGraph, ForceGraphNode, ForceGraphArrowLink} from 'react-vis-force';
 import uuid from 'uuid/v1';
export default class NodeGraph extends React.Component {
    constructor(props) {
        super(props);
    }

    render() {
      const {nodes = [], links = [], selectNode} = this.props;
      return <InteractiveForceGraph zoom 
      onSelectNode={selectNode}
      zoomOptions={{minScale: 0.25, maxScale: 5}}
      
      key={uuid()} simulationOptions={{ height: 300, width: 1000, radiusMargin: 40, strength: {
        collide: 1,
        animated: true,
        x: ({ radius }) => 15 / radius,
        y: ({ radius }) => 3 / radius,
      } }}>
      {nodes.map(nd => <ForceGraphNode key={uuid()} node={{ ...nd }} fill={nd.color || "red"} />)}
      {links.map(ln => <ForceGraphArrowLink targetRadius={12} key={uuid()} length={200} link={{ source: ln.source.id, target: ln.target.id}}/>)}
    </InteractiveForceGraph>
    }
}
  
  
  
  