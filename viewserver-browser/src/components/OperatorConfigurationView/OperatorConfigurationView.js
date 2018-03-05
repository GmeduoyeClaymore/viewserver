import React, { Component } from 'react';
import PagingListView from 'components/Dao/PagingListView'
import LoadingScreen from 'common-components/LoadingScreen'
import { connect } from 'react-redux';
import ViewServerGrid from 'canv-grid/ViewServerGrid';
import { withRouter } from 'react-router';
import {actions as configurationViewActions} from 'components/OperatorConfigurationView/component';
import { updateSubscriptionAction} from 'common/dao/DaoActions';
import {getDaoContext, getDaoCommandStatus} from 'common/dao';
import {stringify,parse} from 'query-string';
import {isEqual} from 'lodash';
import NodeGraph from 'common-components/NodeGraph';
import JSONPretty from 'react-json-pretty';

const OperatorConfigurationView_mapStateToProps = (state, props) => { 
  const {search} = props.location;
  const queryStringParams = parse(search);

  return {
  ...props,
  ...queryStringParams,
  ...state.OperatorConfigurationView.graphConfig,
  selectedNode: state.OperatorConfigurationView.selectedNode,
} }

const OperatorConfigurationView_mapDispatchToProps = (dispatch, props) => { 
  return {
    dispatch
  } 
}

class OperatorConfigurationView extends Component{
  constructor(props){
    super(props)
    this.state = {};
    this.selectNode = this.selectNode.bind(this);
  }

  selectNode(ev,nd){
    const {dispatch} = this.props;
    dispatch(configurationViewActions.selectReportNode(nd));
  }

  render(){
    const {context={},mode,history ,graphConfig = {}, nodes = [], links = [], selectedNode = {}, report, parameters} = this.props;
    const {data: nodeData = {}} = selectedNode;
    const {selectNode} = this;
    if(!report){
      return null;
    }
    return <div style={{flex:1}}>
              <h1>{report.name}</h1>
                <ul>
                {parameters.map(c=> <li>{JSON.stringify(c)}</li>)}
                </ul>
                <NodeGraph nodes={nodes} links={links} height={800} width={1000} selectNode={selectNode}/>
                <div style={{flex: 1}}>
                  <h2>{nodeData.name}</h2>
                  <JSONPretty id="json-pretty" json={nodeData}></JSONPretty>
                </div>
            </div>
  }
}
export default withRouter(connect(OperatorConfigurationView_mapStateToProps,OperatorConfigurationView_mapDispatchToProps)(OperatorConfigurationView));
