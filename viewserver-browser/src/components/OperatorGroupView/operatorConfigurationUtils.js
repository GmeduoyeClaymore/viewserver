
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

export const getNodesAndLinksFromConnectionsAndOperators = (connections = [], operators = []) => {
    const nodes = [];
    const links = [];
    const nodesForName = {};
    const hasOperators = operators && operators.length;

    const getNodeForName = (name) => {
      let node = nodesForName[name];
      if(!node){
        node = {key: name, id: name, radius: 25};
        nodes.push(node);
        nodesForName[name] = node;
      }
      return node;
    }
  
    if(hasOperators){
    operators.forEach(
      op => {
        const nd = getNodeForName(op.path);
        nd.data = op;
        nd.color = getColor(op);
      });
    }
  
    connections.forEach(
          connection => {
            const {outputOperator : target, inputOperator: source} = connection;
            if(hasOperators){
                if((!nodesForName[target] || !nodesForName[source])){
                    return;
                }
            }
            const link = {key: 'src-'+ source + 'target-' + target, source : getNodeForName(source), target: getNodeForName(target)};
            links.push(link);
          }
    );

    return {
      nodes, 
      links
    }
  }