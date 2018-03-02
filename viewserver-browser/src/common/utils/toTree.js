const Immutable = require("seamless-immutable");

export const nodesAndLinksToState = (links = [], nodes = []) => 
{
    const nodesForName = {};
    const linksByTarget = {};
    const linksBySource = {};
    const resultLinks = [];


    nodes.forEach(nd => {nodesForName[nd.id] =  nd})
    
    links.forEach(ln => {
        linksByTarget[ln.target.id] = ln;
        linksBySource[ln.source.id] = ln;
        resultLinks.push({...ln});
    })

    const getNodeChildren = (node) => {
        const linksFromMe = links.filter(ln => ln.target.id === node.id);
        return linksFromMe.map(ln => nodesForName[ln.source.id])
    }

    const toTreeNode = node => {
        const result = {name: node.id};
        result.children = [];
        result.leafCount = 0;
        let numberLeaves = 0;
        const children = getNodeChildren(node);
        if(children.length){
            children.forEach(ch => {
                const nd = toTreeNode(ch);
                result.leafCount += nd.leafCount;
                result.children.push(nd);
            });
        }else{
            result.leafCount = 1;
        }
        return result;
    }

    const endsOfGraph = Object.values(nodesForName).filter(nd => !linksBySource[nd.id]);
    const tree = {name: 'tree'};
    tree.children = [];
    endsOfGraph.forEach(nd => {
        tree.children.push(toTreeNode(nd));
    });

    return tree.children;
}

export default nodesAndLinksToState;