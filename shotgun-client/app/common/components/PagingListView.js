import React, {Component} from 'react';
import {Dimensions, ScrollView} from 'react-native';
import PropTypes from 'prop-types';
import {updateSubscriptionAction, resetDataAction} from 'common/dao/DaoActions';
import {connectAdvanced} from 'custom-redux';
import {bindActionCreators} from 'redux';
import {isEqual} from 'lodash';
import {ErrorRegion} from 'common/components';
import {getDaoCommandStatus, getDaoCommandResult, getDaoState, getDaoSize} from 'common/dao';
import Logger from 'common/Logger';
import {List} from 'native-base';
import shotgun from 'native-base-theme/variables/shotgun';

class PagingListView extends Component {
  constructor(props) {
    super(props);
    this.onScroll = this.onScroll.bind(this);
    this.daoName = props.daoName;
    this.state = {pageSize: props.pageSize, limit: props.pageSize};
  }

  componentWillMount() {
    const {options = {}, pageSize, setOptions} = this.props;

    if (typeof pageSize != undefined) {
      setOptions({...options, limit: pageSize});
    } else {
      setOptions({...options});
    }
  }

  componentWillReceiveProps(newProps) {
    const {options, setOptions, reset} = this.props;

    if (newProps.options !== null && !isEqual(options, newProps.options)) {
      reset();
      setOptions(newProps.options);
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
        doPage(limit);
      }
    }
  }

  renderItem = ({item, isLast, isFirst}) => this.props.rowView({item, isLast, isFirst});

  render() {
    const {data = [], errors, busy, emptyView, paginationWaitingView, headerView: HeaderView} = this.props;

    return (
      <ErrorRegion errors={errors}>
        <ScrollView style={{flex: 1}} onScroll={this.onScroll}>
          <HeaderView/>
          {(data.length === 0 && !busy) ? emptyView() : <List style={styles.list}>
            {data.map((c, i) => this.renderItem({item: c, isLast: i == data.length - 1, isFirst: i == 0}))}
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
  const actions = bindActionCreators({updateSubscriptionAction, resetDataAction}, dispatch);
  const {daoName} = initializationProps;
  const setOptions = options => actions.updateSubscriptionAction(daoName, options);
  const reset = () => actions.resetDataAction(daoName);
  const doPage = limit => actions.updateSubscriptionAction(daoName, {limit});
  return (nextState, nextOwnProps) => {
    const data = getDaoState(nextState, initializationProps.dataPath, daoName);
    const size = getDaoSize(nextState, daoName);
    const daoPageStatus = getDaoCommandStatus(nextState, 'updateSubscription', daoName);
    const daoPageResult = getDaoCommandResult(nextState, 'updateSubscription', daoName);
    const busy = daoPageStatus === 'start';
    const limit = daoPageStatus === 'success' ? daoPageResult : (ownProps.limit || initializationProps.pageSize);
    const errors = daoPageStatus === 'fail' ? daoPageResult : undefined;
    const nextResult = {
      busy,
      data,
      size,
      doPage,
      setOptions,
      reset,
      errors,
      limit,
      ...nextOwnProps
    };
    ownProps = nextOwnProps;
    if (!isEqual(result, nextResult)) {
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
  selectorFactory
)(PagingListView);

export {ConnectedPagingListView as PagingListView};


