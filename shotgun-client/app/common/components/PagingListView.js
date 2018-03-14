import React, {Component} from 'react';
import {Dimensions, ScrollView} from 'react-native';
import PropTypes from 'prop-types';
import {resetSubscriptionAction, updateSubscriptionAction, resetDataAction, clearCommandStatus} from 'common/dao/DaoActions';
import {isEqual, connectAdvanced} from 'custom-redux';
import {bindActionCreators} from 'redux';
import {ErrorRegion} from 'common/components';
import {getOperationErrors, getDaoCommandStatus, getDaoCommandResult, getDaoState, getDaoSize, getDaoOptions, isAnyOperationPending} from 'common/dao';
import Logger from 'common/Logger';
import {List} from 'native-base';
import shotgun from 'native-base-theme/variables/shotgun';

class PagingListView extends Component {
  constructor(props) {
    super(props);
    this.onScroll = this.onScroll.bind(this);
    this.reset = this.reset.bind(this);
    this.daoName = props.daoName;
    this.state = {pageSize: props.pageSize, limit: props.pageSize};
  }

  componentDidMount() {
    Logger.info('Component would mount called on paging list view');
    const {options} = this.props;
    this.setOptions(undefined, options);
  }

  setOptions(prevOptions, newOptions){
    const {setOptions} = this.props;
    if (!isEqual(newOptions, prevOptions, true)){
      Logger.info('PAGINGLISTVIEW - SUBSCRIBIING BECAUSE - options have been changed');
      setOptions(newOptions);
    }
  }

  reset(){
    const {reset} = this.props;
    if (reset){
      Logger.info('PAGINGLISTVIEW - SUBSCRIBIING BECAUSE - Reset called explicitly on paging list view');
      reset();
    }
  }

  componentWillReceiveProps(newProps) {
    const {options} = this.props;

    if (newProps.options !== null && !isEqual(options, newProps.options, true)) {
      //reset();
      this.setOptions(options, newProps.options);
      Logger.debug('PagingProps-' + JSON.stringify(newProps.options));
    }
  }

  onScroll(e) {
    const {busy, size, doPage} = this.props;
    const windowHeight = Dimensions.get('window').height;
    const height = e.nativeEvent.contentSize.height;
    const offset = e.nativeEvent.contentOffset.y;
    if (windowHeight + offset >= (height * 0.75)) {
      const limit = this.state.limit + this.state.pageSize;
      if (!busy && limit < (size + this.state.pageSize)) {
        this.setState({limit});
        Logger.info('PAGINGLISTVIEW - SUBSCRIBIING BECAUSE - Paging paging list view because the scroll area has changed');
        doPage(limit);
      }
    }
  }

  renderItem = ({item, isLast, isFirst, ...rest}) => this.props.rowView({item, isLast, isFirst, ...rest});

  render() {
    const {data = [], errors, busy, emptyView, paginationWaitingView, headerView: HeaderView = () => null, ...rest} = this.props;

    return (
      <ErrorRegion errors={errors}>
        <ScrollView style={{flex: 1}} onScroll={this.onScroll}>
          <HeaderView {...this.props}/>
          {(data.length === 0 && !busy) ? emptyView() : <List style={styles.list}>
            {data.map((c, i) => this.renderItem({item: c, isLast: i == data.length - 1, isFirst: i == 0, ...rest}))}
          </List>}
          {busy ? paginationWaitingView() : null}
        </ScrollView>
      </ErrorRegion>
    );
  }
}

const styles = {
  list: {
    backgroundColor: shotgun.hairline,
    flex: 1,
    flexDirection: 'column',
    display: 'flex'
  }
};

const selectorFactory = (dispatch, initializationProps) => {
  let result = {};
  let ownProps = {};
  const actions = bindActionCreators({resetSubscriptionAction, updateSubscriptionAction, resetDataAction, clearCommandStatus}, dispatch);
  const {daoName} = initializationProps;

  return (nextState, nextOwnProps) => {
    const daoPageStatus = getDaoCommandStatus(nextState, 'updateSubscription', daoName);
    const daoPageResult = getDaoCommandResult(nextState, 'updateSubscription', daoName);
    const optionsFromDao = getDaoOptions(nextState, daoName);
    const {options: optionsFromProps} = nextOwnProps;
    const options = {...optionsFromProps, ...optionsFromDao};
    
    const clearStatusThen = func => {
      actions.clearCommandStatus(daoName, 'updateSubscription');
      actions.clearCommandStatus(daoName, 'resetSubscription');
      func();
    };

    const nextResult = {
      busy: isAnyOperationPending(nextState, [{ [daoName]: 'updateSubscription'}, {[daoName]: 'resetSubscription'}]),
      data: getDaoState(nextState, initializationProps.dataPath, daoName),
      size: getDaoSize(nextState, daoName),
      options,
      doPage: limit => clearStatusThen(() => actions.updateSubscriptionAction(daoName, {limit})),
      setOptions: options => clearStatusThen(() => actions.updateSubscriptionAction(daoName, options)),
      reset: () => actions.resetDataAction(daoName),
      errors: getOperationErrors(nextState, [{[daoName]: 'updateSubscription'}, {[daoName]: 'resetSubscription'}]),
      limit: daoPageStatus === 'success' ? daoPageResult : (ownProps.limit || initializationProps.pageSize),
      ...nextOwnProps
    };
    ownProps = nextOwnProps;
    if (!isEqual(result, nextResult, true, true)) {
      result = nextResult;
    }
    return result;
  };
};

PagingListView.propTypes = {
  data: PropTypes.array,
  daoName: PropTypes.string.isRequired,
  busy: PropTypes.bool.isRequired,
  rowView: PropTypes.func.isRequired,
  paginationWaitingView: PropTypes.func.isRequired,
  emptyView: PropTypes.func.isRequired
};

const ConnectedPagingListView = connectAdvanced(
  selectorFactory, {withRef: true}
)(PagingListView);

export {ConnectedPagingListView as PagingListView};


