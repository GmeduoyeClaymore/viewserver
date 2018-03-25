import React, {Component} from 'react';
import { View, Text, TouchableHighlight, Dimensions} from 'react-native';
import ReactNativeModal from 'react-native-modal';
import {PagingListView, Icon, Swiper} from 'common/components';
import {Spinner, Row, Grid, Col, ListItem, Button} from 'native-base';
import shotgun from 'native-base-theme/variables/shotgun';
import {callUser} from 'common/actions/CommonActions';
import RNImmediatePhoneCall from 'react-native-immediate-phone-call';
import {connect} from 'custom-redux';
import {getOperationError} from 'common/dao';
import moment from 'moment';
import {RelatedUser, StarsControl, StatusButton} from './RelatedUser';
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
  }
};


const cancelButton = <Text style={[styles.cancelText]}>Cancel</Text>;
const Paging = () => <View><Spinner /></View>;
const NoItems = () => <View style={{flex: 1}}><Text>No items to display</Text></View>;

const JobSummary = ({item: orderSummary, isLast, isFirst}) => {
  const {delivery, contentType} = orderSummary;
  return <ListItem key={orderSummary.orderId} style={[styles.orderRequest, isLast ? styles.last : undefined, isFirst ?  styles.first : undefined ]}>
    <Grid>
      <Row size={75} style={styles.locationRow}>
        <Col size={60}>
          <Text style={{marginBottom: 8}}>{orderSummary.product.name }</Text>
          <StarsControl style={{}} rating={orderSummary.customerRating}/>
        </Col>
        <Col size={60}>
          {contentType.fromTime ? <Row style={{paddingRight: 10, marginBottom: 8}}><Icon paddedIcon name="delivery-time"/><Text style={{paddingTop: 3}}>{moment(delivery.from).format('Do MMM, h:mma')}</Text></Row> : null}
        </Col>
      </Row>
    </Grid>
  </ListItem>;
};


class UserRelationshipDetail extends Component{
  constructor(props){
    super(props);
    this.handleCancel = this.handleCancel.bind(this);
    this.RelatedUser = this.RelatedUser.bind(this);
    this.selectUserByIndex = this.selectUserByIndex.bind(this);
    this.show = this.show.bind(this);
    this.updateSelectedIndexForUser(this.props);
    this.state = {
      isVisible: false
    };
  }

  RelatedUser = ({user, onPressCallUser, onPressAssignUser, errors, handleCancel, selectedUser = {}}) => {
    const isSelected = selectedUser.userId === user.userId;
    return <View style={{flex: 1, margin: 20, flexDirection: 'column', maxHeight: ELEMENT_HEIGHT - 120}}>
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
        {isSelected ? <PagingListView
          ref={oc => {this.ordersControl = oc;}}
          daoName='orderSummaryDao'
          dataPath={['orders']}
          elementContainerStyle={{borderWidth: 0.5, borderColor: '#edeaea', padding: 5}}
          options={{driverId: user.userId, reportId: 'driverOrderSummary'}}
          rowView={JobSummary   }
          paginationWaitingView={Paging}
          emptyView={NoItems}
        /> : <Spinner />}
      </View>
      <StatusButton user={user} style={{justifyContent: 'flex-start', marginLeft: 10, marginRight: 10}}/>
    </View>;
  }

  componentWillReceiveProps(newProps){
    this.updateSelectedIndexForUser( newProps, this.props);
  }

  updateSelectedIndexForUser(newProps, oldProps = {}){
    Logger.info(`Attempting to update selected index selected user is ${JSON.stringify(newProps.selectedUser)}`);
    if (!isEqual(newProps.selectedUser, oldProps.selectedUser) && newProps.selectedUser){
      const {relatedUsers, context} = newProps;
      const selectedUserIndex = relatedUsers.findIndex(c=> c.userId === newProps.selectedUser.userId);
      Logger.info(`Attempting to update selected index to ${selectedUserIndex}`);
      context.setState({selectedUserIndex});
    }
  }

  handleCancel(){
    const {context} = this. props;
    this.setState({isVisible: false});
    context.setState({selectedUser: undefined, selectedUserIndex: -1});
  }

  show(){
    this.setState({isVisible: true});
  }

  selectUserByIndex(idx){
    const {relatedUsers, context} = this.props;
    const selectedUser = relatedUsers[idx];
    Logger.info(`Selected user is ${selectedUser ? selectedUser.userId : 'undefined'} index is ${idx} related users are ${relatedUsers.map(c=>c.userId).join(',')}`);
    context.setState({selectedUser, selectedUserIndex: idx});
  }

  render(){
    const {selectedUser, relatedUsers, onPressAssignUser, onPressCallUser, navigationStrategy, selectedUserIndex} = this.props;
    const {RelatedUser} = this;
    const {isVisible} = this.state;
    const scrollViewStyle = {...styles.userSelector, width: ELEMENT_WIDTH, height: ELEMENT_HEIGHT};
    return <ReactNativeModal
      isVisible={isVisible}
      backdropOpacity={0.4}>
      <View style={{flex: 1, ...scrollViewStyle}}>
        <Swiper height={ELEMENT_HEIGHT} width={ELEMENT_WIDTH} index={selectedUserIndex} contentContainerStyle={{width: '100%', backgroundColor: 'white'}} loop={false} animated={false} bounces={false} showsPagination={false} loadMinimal={true} onIndexChanged={this.selectUserByIndex} style={styles.wrapper} showsButtons={true}>
          {relatedUsers.map((v, i) => <RelatedUser navigationStrategy={navigationStrategy} selectedUserIndex={selectedUserIndex} selectedUser={selectedUser} handleCancel={this.handleCancel} onPressCallUser={onPressCallUser} onPressAssignUser={onPressAssignUser} user={v} key={i}/>)}
        </Swiper>
      </View>
      <TouchableHighlight
        style={styles.cancelButton}
        underlayColor="#ebebeb"
        onPress={this.handleCancel}>
        {cancelButton}
      </TouchableHighlight>
    </ReactNativeModal>;
  }
}

const mapStateToProps = (state, initialProps) => {
  const {dispatch} = initialProps;
  const onPressCallUser = async (user) => {
    const {userId, relationshipStatus} = user;
    if (relationshipStatus === 'ACCEPTED'){
      RNImmediatePhoneCall.immediatePhoneCall(`+${user.contactNo}`);
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

export default connect(
  mapStateToProps, true, true
)(UserRelationshipDetail);


