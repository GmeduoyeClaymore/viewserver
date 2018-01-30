import React, {Component} from 'react';
import {View, ScrollView, StyleSheet, Dimensions } from 'react-native';
import PropTypes from 'prop-types';
import {updateSubscriptionAction, resetDataAction} from 'common/dao/DaoActions';
import { connectAdvanced} from 'custom-redux';
import { bindActionCreators} from 'redux';
import { isEqual } from 'lodash';
import ErrorRegion from 'common/components/ErrorRegion';
import {getDaoCommandStatus, getDaoCommandResult, getDaoState} from 'common/dao';
import Logger from 'common/Logger';

const styles = StyleSheet.create({
  contentContainer: {
    paddingVertical: 0
  }
});

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
      const {options = {}, pageSize, setOptions} = this.props;
      if (typeof pageSize != undefined){
        setOptions({...options, limit: pageSize });
      } else {
        setOptions({...options});
      }
    }

    componentWillReceiveProps(newProps){
      if (newProps.options !== null && !isEqual(this.props.options, newProps.options)){
        this.props.setOptions(newProps.options);
      }
      Logger.debug('PagingProps-' + JSON.stringify(newProps.options));
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
    
    renderItem = (item, isLast) => this.props.rowView(item, isLast);
    
    render() {
      const { data = [], errors, emptyView, paginationWaitingView, headerView: HeaderView} = this.props;
      return (
        <ErrorRegion errors={errors}>
          <View style={{flex: 1, flexDirection: 'column', display: 'flex'}}>
            <HeaderView/>
            {(data.length === 0 && !this.props.busy)  ? emptyView() : <ScrollView contentContainerStyle={styles.contentContainer} style={{flex: 1, flexDirection: 'column'}} onScroll={this._onScroll}>
              {data.map((c, i) => this.renderItem(c, i == data.length - 1))}
              {this.props.busy ? paginationWaitingView() : null}
            </ScrollView >}
          </View>
        </ErrorRegion>
      );
    }
}

const selectorFactory = (dispatch, initializationProps) => {
  let result = {};
  let ownProps = {};
  const actions = bindActionCreators({updateSubscriptionAction, resetDataAction}, dispatch);
  const {daoName} = initializationProps;
  const setOptions = options => actions.updateSubscriptionAction(daoName, options);
  const reset = () => actions.resetDataAction(daoName);
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
      reset,
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

export default connectAdvanced(
  selectorFactory
)(PagingListView);

