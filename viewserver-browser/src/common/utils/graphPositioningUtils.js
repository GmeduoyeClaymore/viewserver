const Immutable = require("seamless-immutable");

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

    const myNodes =  Immutable.asMutable(nodes, {deep: true});
    const myLinks =  Immutable.asMutable(links, {deep: true});

    myNodes.forEach(nd => {nodesForName[nd.id] =  nd})
    
    myLinks.forEach(ln => {
        linksByTarget[ln.target.id] = ln;
        linksBySource[ln.source.id] = ln;
        resultLinks.push({...ln});
    })

    const getMaxLevels = (node, level = 0) => {
        if(!node){
            return level-1;
        }
        let maxChild = level;
        const linksForChild = myLinks.filter(ln => ln.source.id === node.id);
        linksForChild.forEach((ln,index)  => 
        { 
            let maxLevel = getMaxLevels(nodesForName[ln.target.id], level + 1)  
            if(maxLevel > maxChild){
            maxChild = maxLevel;
            }
        });
        return maxChild;
    }

    const getNodeChildren = (node) => {
        const linksFromMe = myLinks.filter(ln => ln.source.id === node.id);
        return linksFromMe.map(ln => nodesForName[ln.target.id])
    }

    const howMuchOfTheYAxesDoINeed = (node) => {
        const nodeChildren = getNodeChildren(node);
        if(!nodeChildren.length){
            return 1;
        }
        let range = 0;
        nodeChildren.forEach(
            nd => {
                range += howMuchOfTheYAxesDoINeed(nd);
            }
        )
        return range;
    }

    const getNodePositions  = (roots, yl, yp, xl, xu, level) => {

        const howMuchOfYs = roots.map(rt => howMuchOfTheYAxesDoINeed(rt));
        const totalY = howMuchOfYs.reduce((a, b) => a + b, 0); 

        let yLower = yl;
        let yUpper = yp;

        roots.forEach(
            (rt,idx) => {
                const maxLevels = getMaxLevels(rt);
                const howMuchOfY = howMuchOfYs[idx];
                const xOffset = maxLevels ? ((xu - xl)/maxLevels) * level : 0;
                const newUpper = ((yUpper - yLower) * (howMuchOfY/totalY));
                rt.fy = ((newUpper - yLower)/2) + yLower;
                rt.fx = xl + xOffset

                getNodePositions(getNodeChildren(rt), yLower, yUpper, xl + xOffset, xu, level + 1);
                
                yLower = yUpper;
                yUpper = newUpper;
            }
        )
    }

    const rootNodes = Object.values(nodesForName).filter(nd => !linksByTarget[nd.id]);

    getNodePositions(rootNodes, PaddingY, Width - PaddingY, PaddingX, Height - PaddingX, 1);

    return {
        nodes: Object.values(nodesForName),
        links: resultLinks
    };
}

export default determinePositionFactory;