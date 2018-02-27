
const DEFAULT_TOTAL_HEIGHT = 1000;
const DEFAULT_TOTAL_WIDTH = 500;
const DEFAULT_PADDING_X = 100;
const DEFAULT_PADDING_Y = 100;

export const determinePositionFactory = ({   
        Height = DEFAULT_TOTAL_HEIGHT,
        Width = DEFAULT_TOTAL_WIDTH,
        PaddingX = DEFAULT_PADDING_X,
        PaddingY = DEFAULT_PADDING_Y
    }) => (links, nodes) => 
{
    const nodesForName = {};
    const linksByTarget = {};
    const linksBySource = {};
    const resultLinks = [];
    let maxLevels = undefined;

    nodes.forEach(nd => {nodesForName[nd.id] = {...nd}} )
    links.forEach(ln => {
        linksByTarget[ln.target.id] = ln;
        linksBySource[ln.source.id] = ln;
        resultLinks.push({...ln});
    })

    const getMaxLevels = (node, level = 0) => {
        if(!node){
            return level-1;
        }
        let maxChild = level;
        const linksForChild = links.filter(ln => ln.target.id === node.id);
        linksForChild.forEach((ln,index)  => 
        { 
            let maxLevel = getMaxLevels(nodesForName[ln.source.id], level + 1)  
            if(maxLevel > maxChild){
            maxChild = maxLevel;
            }
        });
        return maxChild;
    }

    const positionChildren = (node, level, maxLevels) => {
        if(!node){
            return;
        }
        const linksForChild = links.filter(ln => ln.target.id === node.id);
        if(!linksForChild.length){
            return;
        }
        const rangeY = Height - (PaddingY * 2);
        const rangeX = Width - (PaddingX * 2);
        const ySteps = (linksForChild.length + 1);;
        const xSteps = (maxLevels + 1);;
        const incremetY = rangeY/ySteps
        const incremetX = rangeX/xSteps;
        const xposition = (rangeX - (incremetX * level)); 
        linksForChild.forEach((ln,index)  => 
            { 
            const child = nodesForName[ln.source.id];
            child.fy = PaddingY + (incremetY * (index + 1)); 
            child.fx = ln.fx && ln.fx > xposition ? ln.fx : xposition
            positionChildren(child, level + 1, maxLevels)  
            });
    }

    const getNodePosition  = (node) => {
        const {id} = node;
        if(!linksBySource[id]){
            const rangeX = Width - (PaddingX * 2);
            const maxLevels = getMaxLevels(node);
            const xSteps = (maxLevels + 1);;
            const incremetX = rangeX/xSteps;
            const xposition = Width - PaddingX - incremetX; 
            const rangeY = Height - (PaddingY * 2);
            node.fx = xposition;
            node.fy = PaddingY + (rangeY/2);
            positionChildren(node,1, maxLevels);
        }
    }
    Object.values(nodesForName).forEach(getNodePosition);
    return {
        nodes: Object.values(nodesForName),
        links: resultLinks
    };
}

export default determinePositionFactory;