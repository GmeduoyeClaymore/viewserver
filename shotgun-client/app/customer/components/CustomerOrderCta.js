import React, {Component} from 'react';
import {Text, Grid, Col, Icon, Button} from 'native-base';
import {withRouter} from 'react-router';
import {OrderStatuses, getFriendlyOrderStatusName} from 'common/constants/OrderStatuses';
import Products from 'common/constants/Products';
import shotgun from 'native-base-theme/variables/shotgun';

class CustomerOrderCta extends Component {
  constructor() {
    super();
  }

  render() {
    const {orderSummary} = this.props;
    const isComplete = orderSummary.status == OrderStatuses.COMPLETED;
    const inOnRoute = orderSummary.status == OrderStatuses.PICKEDUP;
    const isDelivery = orderSummary.productId == Products.DELIVERY;

    if (isComplete) {
      return null;
    }

    return <Grid>
      <Col style={{justifyContent: 'center'}}>
        <Text style={[styles.statusText, inOnRoute ? styles.onRouteText : styles.pendingText]}>{getFriendlyOrderStatusName(orderSummary.status)}</Text>
      </Col>
      {inOnRoute ?
        <Col>
          <Button track style={styles.button} onPress={() => console.log('Should go to track driver screen')}>
            <Icon name='arrow-forward'/>
            <Text uppercase={false}>Track Driver</Text>
          </Button>
        </Col> :
        <Col>
          <Button fullWidth danger style={styles.button} onPress={() => console.log('Should cancel order')}>
            <Text uppercase={false}>Cancel {isDelivery ? 'delivery' : 'collection'}</Text>
          </Button>
        </Col>}
    </Grid>;
  }
}

const styles = {
  button: {
    borderRadius: 0,
  },
  statusText: {
    fontWeight: 'bold',
    paddingLeft: 25
  },
  onRouteText: {
    color: shotgun.brandSecondary,
  },
  pendingText: {
    color: shotgun.brandLight,
  },
  status: {
    color: shotgun.inverseTextColor
  },
  statusContainer: {
    backgroundColor: shotgun.brandSecondary,
    justifyContent: 'center',
    alignItems: 'center',
  },

  trackButton: {
    margin: 0,
    borderRadius: 0,
    flex: 1,
    backgroundColor: shotgun.brandPrimary,
    alignSelf: 'stretch',
    justifyContent: 'center',
    alignItems: 'center',
  }
};
export default withRouter(CustomerOrderCta);
