import React, {Component} from 'react';
import {ScrollView} from 'react-native';
import PropTypes from 'prop-types';
import {updateSubscriptionAction, resetDataAction, clearCommandStatus} from 'common/dao/DaoActions';
import {isEqual, connectAdvanced} from 'custom-redux';
import {bindActionCreators} from 'redux';
import {ErrorRegion} from 'common/components';
import {getOperationErrors, getDaoCommandStatus, getDaoCommandResult, getDaoState, getDaoSize, getDaoOptions, isAnyOperationPending, getSnapshotComplete} from 'common/dao';
import Logger from 'common/Logger';
import {List, Row} from 'native-base';
import shotgun from 'native-base-theme/variables/shotgun';

class PagingListView extends Component {
  constructor(props) {
    super(props);
    this.onScroll = this.onScroll.bind(this);
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

  componentWillReceiveProps(newProps) {
    const {options} = this.props;

    if (newProps.options !== null && !isEqual(options, newProps.options, true)) {
      this.setOptions(options, newProps.options);
      Logger.debug('PagingProps-' + JSON.stringify(newProps.options));
    }
  }

  onScroll(e) {
    const {busy, size, doPage} = this.props;
    const windowHeight = shotgun.contentHeight;
    const height = e.nativeEvent.contentSize.height;
    const offset = e.nativeEvent.contentOffset.y;
    if (windowHeight + offset >= (height * 0.75)) {
      const limit = this.state.limit + this.state.pageSize;
      if (!busy && limit < (size + this.state.pageSize)) {
        this.setState({limit});
        Logger.debug('PAGINGLISTVIEW - SUBSCRIBIING BECAUSE - Paging paging list view because the scroll area has changed');
        doPage(limit);
      }
    }
  }

  renderItem = ({item, isLast, isFirst, ...rest}) => this.props.rowView({item, isLast, isFirst, ...rest});

  render() {
    const {data = [], errors, busy, emptyView: EmptyView, paginationWaitingView, elementContainerStyle = styles.list, elementContainer: ElementContainer = List, headerView: HeaderView = () => null, ...rest} = this.props;

    return [<ErrorRegion errors={errors} key='errors'/>,
      <ScrollView key='scrollView' onScroll={this.onScroll}>
        {!busy ? <HeaderView {...this.props}/> : null}
        {(data.length === 0 && !busy) ? <EmptyView {...this.props}/> : <ElementContainer style={elementContainerStyle}>
          {data.map((c, i) => this.renderItem({item: c, index: i, isLast: i == data.length - 1, isFirst: i == 0, ...rest}))}
        </ElementContainer>}
        {busy ? paginationWaitingView() : null}
      </ScrollView>];
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
  const actions = bindActionCreators({updateSubscriptionAction, resetDataAction, clearCommandStatus}, dispatch);
  const {daoName} = initializationProps;

  return (nextState, nextOwnProps) => {
    const daoPageStatus = getDaoCommandStatus(nextState, 'updateSubscription', daoName);
    const daoPageResult = getDaoCommandResult(nextState, 'updateSubscription', daoName);
    const optionsFromDao = getDaoOptions(nextState, daoName);
    const {options: optionsFromProps} = nextOwnProps;
    const options = {...optionsFromProps, ...optionsFromDao};
    
    const clearStatusThen = func => {
      actions.clearCommandStatus(daoName, 'updateSubscription');
      func();
    };

    const errors = getOperationErrors(nextState, [{[daoName]: 'updateSubscription'}]);

    const nextResult = {
      busy: isAnyOperationPending(nextState, [{ [daoName]: 'updateSubscription'}]) || (!getSnapshotComplete(nextState, daoName) && !errors),
      data: getDaoState(nextState, initializationProps.dataPath, daoName),
      size: getDaoSize(nextState, daoName),
      errors,
      options,
      doPage: limit => clearStatusThen(() => actions.updateSubscriptionAction(daoName, {limit})),
      setOptions: options => clearStatusThen(() => actions.updateSubscriptionAction(daoName, options)),
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


