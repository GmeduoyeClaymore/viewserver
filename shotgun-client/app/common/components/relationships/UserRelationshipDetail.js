import React, {Component} from 'react';
import { View, Text, TouchableHighlight, Dimensions, TouchableOpacity} from 'react-native';
import ReactNativeModal from 'react-native-modal';
import {PagingListView, Icon, Swiper, AverageRating} from 'common/components';
import {Spinner, Row, Grid, Col, ListItem, Button} from 'native-base';
import shotgun from 'native-base-theme/variables/shotgun';
import {callUser} from 'common/actions/CommonActions';
import PhoneCallService from 'common/services/PhoneCallService';
import {ReduxRouter, Route, withExternalState} from 'custom-redux';
import {getOperationError} from 'common/dao';
import moment from 'moment';
import {RelatedUser, StatusButton} from './RelatedUser';
import Logger from 'common/Logger';
import {isEqual} from 'lodash';
const {height, width} = Dimensions.get('window');
const BACKGROUND_COLOR = 'white';
const BORDER_RADIUS = 13;
const BUTTON_FONT_COLOR = '#007ff9';
const BUTTON_FONT_SIZE = 20;
const ELEMENT_WIDTH = width - 40;
const ELEMENT_HEIGHT = height - 40;
const styles = {
  userSelector: {
    backgroundColor: BACKGROUND_COLOR,
    borderRadius: BORDER_RADIUS,
    margin: 0,
    overflow: 'hidden',
    marginBottom: 10
  },
  container: {
    flex: 1,
    flexDirection: 'row',
    padding: 5,
    paddingTop: 10
  },
  picture: {
    borderRadius: 40,
    width: 80,
    height: 80,
    marginRight: 10
  },
  view: {
    alignItems: 'flex-start',
    justifyContent: 'flex-start',
    flexDirection: 'row'
  },
  title: {
    fontWeight: 'bold',
    color: '#848484',
    fontSize: 18
  },
  cancelButton: {
    backgroundColor: BACKGROUND_COLOR,
    borderRadius: BORDER_RADIUS,
    height: 57,
    justifyContent: 'center',
  },
  cancelText: {
    padding: 10,
    textAlign: 'center',
    color: BUTTON_FONT_COLOR,
    fontSize: BUTTON_FONT_SIZE,
    fontWeight: '600',
    backgroundColor: 'transparent',
  },
  star: {
    fontSize: 15,
    padding: 2,
    color: shotgun.gold,
  },
  summary: {
    fontSize: 10,
    paddingTop: 5,
    paddingBottom: 5
  },
  orderRequest: {
    paddingTop: 10,
    paddingBottom: 10,
  },
  last: {
    marginBottom: 0
  },
  first: {
    marginTop: 1
  },
  locationRow: {
    paddingBottom: 20,
  },
  origin: {
    alignSelf: 'flex-start'
  },
  price: {
    fontSize: 18,
    fontWeight: 'bold',
    lineHeight: 18,
    alignSelf: 'flex-end'
  },
  orderStatus: {
    alignSelf: 'flex-end'
  },
  noRequiredForOffloadCol: {
    alignItems: 'flex-end'
  },
  forwardIcon: {
    fontSize: 14,
  },
  buttonWrapper: {
    backgroundColor: 'transparent',
    flexDirection: 'row',
    position: 'absolute',
    top: 0,
    left: 0,
    flex: 1,
    justifyContent: 'space-between',
    alignItems: 'center'
  },

  buttonText: {
    fontSize: 50,
    color: '#007aff',
    fontFamily: 'Arial'
  }
};


const cancelButton = <Text style={[styles.cancelText]}>Cancel</Text>;
const Paging = () => <View><Spinner /></View>;
const NoItems = () => <View style={{flex: 1}}><Text>No items to display</Text></View>;

const JobSummary = ({item: orderSummary, isLast, isFirst}) => {
  const {delivery, contentType} = orderSummary;
  return <ListItem key={orderSummary.orderId} style={[styles.orderRequest, isLast ? styles.last : undefined, isFirst ?  styles.first : undefined ]}>
    <Grid>
      <Col size={60}>
        <Row style={{width: '100%'}}>
          <Text style={{marginBottom: 8}}>{orderSummary.product.name }</Text>
        </Row>
        {contentType.fromTime ? <Row style={{paddingRight: 10, marginBottom: 8}}><Icon paddedIcon name="delivery-time"/><Text style={{paddingTop: 3}}>{moment(delivery.from).format('Do MMM, h:mma')}</Text></Row> : null}
      </Col>
      <Col size={60}>
        <AverageRating rating={orderSummary.customerRating} text="No Rating"/>
      </Col>
    </Grid>
  </ListItem>;
};


class RelatedUserComponent extends Component{
  constructor(props){
    super(props);
    this.navigateTo = this.navigateTo.bind(this);
  }
  navigateTo(user, isReverse){
    const {navigateToUser} = this.props;
    navigateToUser(user, isReverse);
  }

  renderNextButton = () => {
    const {next} = this.props;
    return <TouchableOpacity onPress={() => this.navigateTo(next)} disabled={!next}>
      <View  style={{padding: 5}}>
        <Text style={styles.buttonText}>›</Text>
      </View>
    </TouchableOpacity>;
  }

  renderPrevButton = () => {
    const {prev} = this.props;
    return <TouchableOpacity onPress={() => this.navigateTo(prev, true)} disabled={!prev}>
      <View style={{padding: 5}}>
        <Text style={styles.buttonText}>‹</Text>
      </View>
    </TouchableOpacity>;
  }

  renderButtons = () => {
    const {width, height} = this.props;
    const {renderPrevButton: PrevButton, renderNextButton: NextButton } = this;
    return (
      <View pointerEvents='box-none' style={[styles.buttonWrapper, {width, height}]}>
        <PrevButton/>
        <NextButton/>
      </View>
    );
  }

  render(){
    const {user, onPressCallUser, onPressAssignUser, errors, handleCancel, selectedUser = {}} = this.props;
    const {renderButtons: DirectionButtons} = this;
    return [<View style={{flex: 1, margin: 20, flexDirection: 'column', maxHeight: ELEMENT_HEIGHT - 120}}>
      {
        onPressAssignUser ? <Button style={{marginBottom: 15, minHeight: 40, justifyContent: 'flex-start'}} fullWidth statusButton onPress={() => {
          onPressAssignUser(user);
          handleCancel();
        }}>
          <Icon name="dashed" paddedIcon/>
          <Text uppercase={false}>Assign Job To User</Text>
        </Button> : null
      }
      <RelatedUser {...{user, onPressCallUser, onPressAssignUser, errors, handleCancel}} style={{maxHeight: 120, minHeight: 120}}/>
      <View  style={{flex: 4, paddingLeft: 10, paddingRight: 10}}>
        <PagingListView
          ref={oc => {this.ordersControl = oc;}}
          daoName='orderSummaryDao'
          dataPath={['orders']}
          elementContainerStyle={{borderWidth: 0.5, borderColor: '#edeaea', padding: 5}}
          options={{driverId: user.userId, reportId: 'driverOrderSummary', isCompleted: true}}
          rowView={JobSummary   }
          paginationWaitingView={Paging}
          emptyView={NoItems}
        />
      </View>
      <StatusButton user={user} style={{justifyContent: 'flex-start', marginLeft: 10, marginRight: 10}}/>
    </View>, <DirectionButtons/>];
  }
}

class UserRelationshipDetail extends Component{
  constructor(props){
    super(props);
    this.handleCancel = this.handleCancel.bind(this);
    this.onNavigateToUser = this.onNavigateToUser.bind(this);
  }

  onNavigateToUser(user, isReverse){
    const {history, path} = this.props;
    history.push({pathname: `${path}/SelectedUser${user.userId}X`, isReverse});
  }

  handleCancel(){
    const {history, userRelationshipBasePath} = this.props;
    history.push({pathname: userRelationshipBasePath, transition: 'immediate'});
  }

  render(){
    const {relatedUsers, path, history} = this.props;
    const {onNavigateToUser} = this;
    const scrollViewStyle = {...styles.userSelector, width: ELEMENT_WIDTH, height: ELEMENT_HEIGHT};
    const {selectedUser = relatedUsers[0]} = this.props;
    return selectedUser ? <ReactNativeModal
      isVisible={history.location.pathname.includes(path)}
      backdropOpacity={0.4}>
      <View style={{flex: 1, ...scrollViewStyle}}>
        <ReduxRouter       navigateToUser={onNavigateToUser} path={path} parentPath={path} selectedUser={selectedUser} {...this.props} height={ELEMENT_HEIGHT} width={ELEMENT_WIDTH} defaultRoute={`SelectedUser${selectedUser.userId}X`} style={styles.wrapper} showsButtons={true}>
          {relatedUsers.map((v, i) => <Route prev={relatedUsers[i == 0 ? relatedUsers.length - 1 : i - 1]} next={relatedUsers[i == (relatedUsers.length - 1) ? 0 : i + 1 ]}  component={RelatedUserComponent} path={`SelectedUser${v.userId}X`} handleCancel={this.handleCancel} user={v} key={i}/>)}
        </ReduxRouter>
      </View>
      <TouchableHighlight
        style={styles.cancelButton}
        underlayColor="#ebebeb"
        onPress={this.handleCancel}>
        {cancelButton}
      </TouchableHighlight>
    </ReactNativeModal> : null;
  }
}

const mapStateToProps = (state, initialProps) => {
  const {dispatch} = initialProps;
  const onPressCallUser = async (user) => {
    const {userId, relationshipStatus} = user;
    if (relationshipStatus === 'ACCEPTED'){
      PhoneCallService.call(user.contactNo);
    } else {
      dispatch(callUser({userId}));
    }
  };
  return {
    ...initialProps,
    onPressCallUser,
    errors: getOperationError(state, 'userRelationshipDao', 'callUser'),
  };
};

export default withExternalState(mapStateToProps)(UserRelationshipDetail);


