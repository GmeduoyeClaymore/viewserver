import React, {Component} from 'react';
import {View, ScrollView, StyleSheet, Dimensions } from 'react';
import PropTypes from 'prop-types';
import { updateSubscriptionAction} from 'common/dao/DaoActions';
import { bindActionCreators} from 'redux';
import connectAdvanced from 'custom-redux/connectAdvanced';
import { isEqual } from 'lodash';
import ErrorRegion from 'common-components/ErrorRegion';
import {getDaoCommandStatus, getDaoCommandResult, getDaoState} from 'common/dao';
const styles = {
  contentContainer: {
    paddingVertical: 20
  }
};

class PagingListView extends Component{
    static propTypes = {
      data: PropTypes.array,
      daoName: PropTypes.string.isRequired,
      busy: PropTypes.bool.isRequired,
      rowView: PropTypes.func.isRequired,
      paginationWaitingView: PropTypes.func.isRequired,
      emptyView: PropTypes.func.isRequired
    };

    constructor(props){
      super(props);
      this._onScroll = this._onScroll.bind(this);
      this.daoName = props.daoName;
      this.state = {pageSize: props.pageSize, limit: props.pageSize};
    }

    componentWillMount(){
      const {options = {}, pageSize} = this.props;
      if (typeof pageSize != undefined){
        this.props.setOptions({...options, limit: pageSize });
      } else {
        this.props.setOptions({...options});
      }
    }

    componentWillReceiveProps(newProps){
      if (newProps.options !== null && !isEqual(this.props.options, newProps.options)){
        this.props.setOptions(newProps.options);
      }
      console.log('PagingProps-' + JSON.stringify(newProps.options));
    }

    _onScroll(e){
      const windowHeight = Dimensions.get('window').height;
      const height = e.nativeEvent.contentSize.height;
      const offset = e.nativeEvent.contentOffset.y;
      if ( windowHeight + offset >= (height * 0.75) ){
        if (!this.props.busy){
          this.loadNextPage();
        }
      }
    }
    
    loadNextPage(){
      const newLimit = this.state.limit + this.state.pageSize;
      this.props.doPage(newLimit);
    }
    
    render() {
      const { data = [], errors, rowView : RowView, emptyView : EmptyView, paginationWaitingView : PaginationWaitingView, headerView: HeaderView, ...rest} = this.props;
      return (
        <ErrorRegion errors={errors}>
          <div style={{flex: 1, flexDirection: 'column', display: 'flex'}}>
            <HeaderView/>
            {(data.length === 0 && !this.props.busy)  ? <EmptyView/> : <div style={{flex: 1, flexDirection: 'column', overflow : 'scroll'}} onScroll={this._onScroll}>
              {data.map( c => <RowView key={c.rowId} row={c} {...rest}/>)}
              {this.props.busy ? <PaginationWaitingView {...rest}/> : null}
            </div >}
          </div>
        </ErrorRegion>
      );
    }
}

const selectorFactory = (dispatch, initializationProps) => {
  let result = {};
  let ownProps = {};
  const actions = bindActionCreators({updateSubscriptionAction}, dispatch);
  const {daoName} = initializationProps;
  const setOptions = options => actions.updateSubscriptionAction(daoName, options);
  const doPage = limit => actions.updateSubscriptionAction(daoName, {limit});
  return (nextState, nextOwnProps) => {
    const data = getDaoState(nextState, initializationProps.dataPath, daoName);
    const daoPageStatus = getDaoCommandStatus(nextState, 'updateSubscription', daoName);
    const daoPageResult = getDaoCommandResult(nextState, 'updateSubscription', daoName);
    const busy = daoPageStatus === 'start';
    const limit =  daoPageStatus === 'success' ? daoPageResult : (ownProps.limit || initializationProps.pageSize);
    const errors = daoPageStatus === 'fail' ? daoPageResult : undefined;
    const nextResult = {
      busy,
      data,
      doPage,
      setOptions,
      errors,
      limit,
      ...nextOwnProps
    };
    ownProps = nextOwnProps;
    if (!isEqual(result, nextResult)){
      result = nextResult;
    }
    return result;
  };
};
const connection = connectAdvanced(
  selectorFactory
);
export default connection(PagingListView);
