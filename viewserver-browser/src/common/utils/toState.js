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
        const linksFromMe = links.filter(ln => ln.source.id === node.id);
        return linksFromMe.map(ln => nodesForName[ln.target.id])
    }

    const getChildren = node => {
        const children = getNodeChildren(node)
        const result = {};
        children.forEach(ch => {
            result[ch.id] = getChildren(ch);
        });
        return result;
    }

    const rootNodes = Object.values(nodesForName).filter(nd => !linksByTarget[nd.id]);
    const state = {};
    rootNodes.forEach(nd => {
        state[nd.id] = getChildren(nd);
    }
    );

    return state;
}

export default nodesAndLinksToState;