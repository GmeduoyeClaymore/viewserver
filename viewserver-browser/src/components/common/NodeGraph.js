import React from 'react';
import InteractiveForceGraph from 'common-components/ForceGraph/InteractiveForceGraph';
import ForceGraphArrowLink from 'common-components/ForceGraph/ForceGraphArrowLink';
import ForceGraphNode from 'common-components/ForceGraph/ForceGraphNode';
import determinePosition from 'common/utils/graphPositioningUtils';
import uuid from 'uuid/v1';

const OPERATOR_TYPE_COLORS = {
  'Filter' : '#9cbd79',
  'Table' :  '#d89676',
  'Join' : '#92135b',
  'Projection': '#f83e04',
  'Sort': '#0ee8f0',
  'GroupBy': '#9a65d2',
  'CalcCol': '#4a49ea'
}

const getColor = node => {
  return OPERATOR_TYPE_COLORS[node.type];
}
 

const TOTAL_HEIGHT = 300;
const TOTAL_WIDTH = 1000;
const PADDING_X = 100;
const PADDING_Y = 30;

export default class NodeGraph extends React.Component {
    constructor(props) {
        super(props);
    }

    graphUpdater() {
      const {graph} = this;
      if(graph){
        //graph.forceUpdate();
      }
      return null;
    }
    render() {
      const {nodes = [], links = [], selectNode, height: Height = TOTAL_HEIGHT, width: Width = TOTAL_WIDTH, PaddingX = PADDING_X, PaddingY = PADDING_Y} = this.props;

      //const {nodes,links} = determinePosition({Height, Width, PaddingX, PaddingY})(immutableLinks, immutableNodes);

      return <div style={{flex:1}}><InteractiveForceGraph zoom 
      key={uuid()}
      ref={gr => {this.graph = gr}}
      onSelectNode={selectNode}
      zoomOptions={{minScale: 0.25, maxScale: 5}}
      simulationOptions={{ animate: true, height: Height, width: Width, radiusMargin: 40, }}>
      {nodes.map(nd => <ForceGraphNode key={uuid()} node={{ ...nd }} fill={getColor(nd.data) || "black"} />)}
      {links.map(ln => <ForceGraphArrowLink key={uuid()} targetRadius={12}  length={200} link={{ source: ln.source.id, target: ln.target.id}}/>)}
    </InteractiveForceGraph>{this.graphUpdater()}</div>
    }
}
  
  
  
  