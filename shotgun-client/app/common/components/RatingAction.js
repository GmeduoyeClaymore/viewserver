import React, {Component} from 'react';
import {Text, View, Icon} from 'native-base';
import {connect} from 'react-redux';
import shotgun from 'native-base-theme/variables/shotgun';
import {rateCustomer} from 'driver/actions/DriverActions';
import {rateDriver} from 'customer/actions/CustomerActions';

class RatingAction extends Component{
  constructor(){
    super();
  }

  render() {
    const {isDriver, delivery, dispatch} = this.props;
    const name = isDriver ? delivery.customerFirstName : delivery.driverFirstName;
    const rating = isDriver ? delivery.customerRating : delivery.driverRating;
    const ratingFunc = isDriver ? rateCustomer : rateDriver;

    const onPressStar = async(rating) => {
      dispatch(ratingFunc(delivery.deliveryId, rating));
    };

    return <View style={{alignItems: 'center'}}><Text style={{alignItems: 'center'}}>Rate {name}</Text>
      <View style={styles.starView}>
        <Icon name='star' onPress={() => onPressStar(1)} style={[styles.star, rating > 0 ? styles.starFilled : styles.starEmpty]}/>
        <Icon name='star' onPress={() => onPressStar(2)} style={[styles.star, rating > 1 ? styles.starFilled : styles.starEmpty]}/>
        <Icon name='star' onPress={() => onPressStar(3)} style={[styles.star, rating > 2 ? styles.starFilled : styles.starEmpty]}/>
        <Icon name='star' onPress={() => onPressStar(4)} style={[styles.star, rating > 3 ? styles.starFilled : styles.starEmpty]}/>
        <Icon name='star' onPress={() => onPressStar(5)} style={[styles.star, rating > 4 ? styles.starFilled : styles.starEmpty]}/>
      </View>
    </View>;
  }
}

const styles = {
  starView: {
    flexWrap: 'wrap',
    alignItems: 'center',
    flexDirection: 'row'
  },
  star: {
    fontSize: 35,
    padding: 2
  },
  starFilled: {
    color: shotgun.gold,
  },
  starEmpty: {
    color: shotgun.brandLight
  }
}

export default connect()(RatingAction);
