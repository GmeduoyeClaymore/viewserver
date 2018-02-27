
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

export const parseReportFromJson = (descriptor) => {
    const parametersObj = descriptor.parameters;
    const parameters = [];
    Object.keys(parametersObj).forEach(
      (key) => {
        const param = parametersObj[key];
        parameters.push(`${param.label} (${param.name} - ${param.type})`)
      }
    )
    const nodes = [];
    const links = [];
    const nodesForName = {};

    const getNodeForName = (name) => {
      let node = nodesForName[name];
      if(!node){
        node = {key: name, id: name, radius: 25};
        nodes.push(node);
        nodesForName[name] = node;
      }
      return node;
    }
  
    descriptor.nodes.forEach(
      node => {
        const nd = getNodeForName(node.name);
        nd.data = node;
        nd.color = getColor(node);
      });
  
    descriptor.nodes.forEach(
      node => {
        node.connections.forEach(
          connection => {
            const newObj = {...node,...connection};
            const {name : target, operator: source} = newObj;
            const link = {key: 'src-'+ source + 'target-' + target, source : getNodeForName(source), target: getNodeForName(target)};
            links.push(link);
          }
        )
      }
    );

    return {
      parameters,
      nodes, 
      links
    }
  }