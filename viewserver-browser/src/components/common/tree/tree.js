import d3 from 'd3'
import { isEmpty } from 'ramda'
import map2tree from 'map2tree'
import deepmerge from 'deepmerge'
import { getTooltipString, toggleChildren, visit, getNodeGroupByDepthCount } from './utils'
import d3tooltip from 'd3tooltip'

const defaultOptions = {
  state: undefined,
  rootKeyName: 'state',
  pushMethod: 'push',
  tree: undefined,
  id: 'd3svg',
  style: {
    node: {
      colors: {
        'default': '#ccc',
        collapsed: 'lightsteelblue',
        parent: 'white'
      },
      radius: 6
    },
    text: {
      colors: {
        'default': 'black',
        hover: 'skyblue'
      }
    },
    link: {
      stroke: '#000',
      fill: 'none'
    }
  },
  size: 1000,
  aspectRatio: 1.0,
  initialZoom: 1,
  margin: {
    top: 10,
    right: 10,
    bottom: 10,
    left: 50
  },
  isSorted: false,
  heightBetweenNodesCoeff: 2,
  widthBetweenNodesCoeff: 1,
  transitionDuration: 750,
  onClickText: () => {},
  tooltipOptions: {
    disabled: false,
    left: undefined,
    right: undefined,
    offset: {
      left: 0,
      top: 0
    },
    style: undefined
  }
}

export default function(DOMNode, options = {}) {
  const {
    id,
    style,
    size,
    aspectRatio,
    initialZoom,
    margin,
    isSorted,
    widthBetweenNodesCoeff,
    heightBetweenNodesCoeff,
    transitionDuration,
    state,
    rootKeyName,
    pushMethod,
    tree,
    tooltipOptions,
    onClickText
    } = deepmerge(defaultOptions, options)

  const width = size.width - margin.left - margin.right
  const height = size.height * aspectRatio - margin.top - margin.bottom
  const fullWidth = size.width
  const fullHeight = size.height * aspectRatio

  const attr = {
    id,
    preserveAspectRatio: 'xMinYMin slice'
  }

  if (!style.width) {
    attr.width = fullWidth
  }

  if (!style.width || !style.height) {
    attr.viewBox = `0 0 ${fullWidth} ${fullHeight}`
  }

  const root = d3.select(DOMNode)
  const zoom = d3.behavior.zoom()
    .scaleExtent([0.1, 3])
    .scale(initialZoom)
  const vis = root
    .append('svg')
    .attr(attr)
    .style({cursor: '-webkit-grab', ...style})
    .call(zoom.on('zoom', () => {
      const { translate, scale } = d3.event
      vis.attr('transform', `translate(${translate})scale(${scale})`)
    }))
    .append('g')
    .attr({
      transform: `translate(${margin.left + style.node.radius}, ${margin.top}) scale(${initialZoom})`
    })

  let layout = d3.layout.tree().size([width, height])
  let data

  if (isSorted) {
    layout.sort((a, b) => b.name.toLowerCase() < a.name.toLowerCase() ? 1 : -1)
  }

  return function renderChart(nextState = tree || state) {
    data = !tree ? map2tree(nextState, {key: rootKeyName, pushMethod}) : nextState

    if (isEmpty(data) || !data.name) {
      data = { name: 'error', message: 'Please provide a state map or a tree structure'}
    }

    let nodeIndex = 0
    let maxLabelLength = 0

    visit(data,
        node => maxLabelLength = Math.max(node.name.length, maxLabelLength),
        node => node.children && node.children.length > 0 ? node.children : null
    )

    data.x0 = height / 2
    data.y0 = 0
    /*eslint-disable*/
    update(data)
    /*eslint-enable*/

    function update(source) {
      // path generator for links
      const diagonal = d3.svg.diagonal().projection(d => [d.y, d.x])
      // set tree dimensions and spacing between branches and nodes
      const maxNodeCountByLevel = Math.max(...getNodeGroupByDepthCount(data))

      layout = layout.size([maxNodeCountByLevel * 25 * heightBetweenNodesCoeff, width])

      let nodes = layout.nodes(data)
      let links = layout.links(nodes)

      nodes.forEach(node => node.y = node.depth * (maxLabelLength * 7 * widthBetweenNodesCoeff))

      // process the node selection
      let node = vis.selectAll('g.node')
        .property('__oldData__', d => d)
        .data(nodes, d => d.id || (d.id = ++nodeIndex))

      let nodeEnter = node.enter().append('g')
        .attr({
          'class': 'node',
          transform: d => `translate(${source.y0},${source.x0})`
        })
        .style({
          fill: style.text.colors.default,
          cursor: 'pointer'
        })
        .on({
          mouseover: function mouseover(d, i) {
            d3.select(this).style({
              fill: style.text.colors.hover
            })
          },
          mouseout: function mouseout(d, i) {
            d3.select(this).style({
              fill: style.text.colors.default
            })
          }
        })

      if (!tooltipOptions.disabled) {
        nodeEnter.call(d3tooltip(d3, 'tooltip', {...tooltipOptions, root})
          .text((d, i) => getTooltipString(d, i, tooltipOptions))
          .style(tooltipOptions.style)
        )
      }

      nodeEnter.append('circle')
        .attr({
          'class': 'nodeCircle'
        })
        .on({
          click: clickedNode => {
            if (d3.event.defaultPrevented) return
            update(toggleChildren(clickedNode))
          }
        })

      nodeEnter.append('text')
        .attr({
          'class': 'nodeText',
          dy: '.35em'
        })
        .style({
          'fill-opacity': 0
        })
        .text(d => d.name)
        .on({
          click: onClickText
        })

      // update the text to reflect whether node has children or not
      node.select('text')
        .attr({
          x: d => d.children || d._children ? -(style.node.radius + 10) : style.node.radius + 10,
          'text-anchor': d => d.children || d._children ? 'end' : 'start'
        })
        .text(d => d.name)

      // change the circle fill depending on whether it has children and is collapsed
      node.select('circle.nodeCircle')
        .attr({
          r: style.node.radius
        })
        .style({
          stroke: 'black',
          'stroke-width': '1.5px',
          fill: d => d._children ? style.node.colors.collapsed : (d.children ? style.node.colors.parent : style.node.colors.default)
        })

      // transition nodes to their new position
      let nodeUpdate = node.transition()
        .duration(transitionDuration)
        .attr({
          transform: d => `translate(${d.y},${d.x})`
        })

      // fade the text in
      nodeUpdate.select('text')
        .style('fill-opacity', 1)

      // restore the circle
      nodeUpdate.select('circle').attr('r', 7)

      // blink updated nodes
      nodeUpdate.filter(function flick(d) {
        // test whether the relevant properties of d match
        // the equivalent property of the oldData
        // also test whether the old data exists,
        // to catch the entering elements!
        return (!this.__oldData__ || d.value !== this.__oldData__.value)
      })
        .style('fill-opacity', '0.3').transition()
        .duration(100).style('fill-opacity', '1')

      // transition exiting nodes to the parent's new position
      let nodeExit = node.exit().transition()
        .duration(transitionDuration)
        .attr({
          transform: d => `translate(${source.y},${source.x})`
        })
        .remove()

      nodeExit.select('circle')
        .attr('r', 0)

      nodeExit.select('text')
        .style('fill-opacity', 0)

      // update the links
      let link = vis.selectAll('path.link')
        .data(links, d => d.target.id)

      // enter any new links at the parent's previous position
      link.enter().insert('path', 'g')
        .attr({
          'class': 'link',
          d: d => {
            let o = {
              x: source.x0,
              y: source.y0
            }
            return diagonal({
              source: o,
              target: o
            })
          }
        })
        .style(style.link)

      // transition links to their new position
      link.transition()
        .duration(transitionDuration)
        .attr({
          d: diagonal
        })

      // transition exiting nodes to the parent's new position
      link.exit().transition()
        .duration(transitionDuration)
        .attr({
          d: d => {
            let o = {
              x: source.x,
              y: source.y
            }
            return diagonal({
              source: o,
              target: o
            })
          }
        })
        .remove()

      // delete the old data once it's no longer needed
      node.property('__oldData__', null)

      // stash the old positions for transition
      nodes.forEach(d => {
        d.x0 = d.x
        d.y0 = d.y
      })
    }
  }
}
