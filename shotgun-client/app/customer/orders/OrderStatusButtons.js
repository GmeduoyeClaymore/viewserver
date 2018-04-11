import * as OrderStatuses from 'common/constants/OrderStatuses';
import PropTypes from 'prop-types';
import React, {Component} from 'react';
import { SpinnerButton} from 'common/components';
import Text from 'react-native';
import {cancelOrder, rejectDriver, customerCompleteOrder} from 'customer/actions/CustomerActions';
import * as ContentTypes from 'common/constants/ContentTypes';

/*eslint-disable */
const resourceDictionary = new ContentTypes.ResourceDictionary().
  property('RejectButtonCaption', 'Reject').
    delivery('Reject Driver').
    personell('Reject Worker').
    rubbish('Reject Driver')
/*eslint-disable */

const  CancelOrder = ({orderSummary, history, ordersPath, busyUpdating}) => {
    const onCancelOrder = () => {
        dispatch(cancelOrder(orderSummary.orderId, () => history.push(`${ordersPath}`)));
    };
    return <SpinnerButton padded busy={busyUpdating} fullWidth danger style={styles.ctaButton} onPress={onCancelOrder}><Text uppercase={false}>Cancel</Text></SpinnerButton> 
};

const  RejectDriver = ({orderSummary, history, ordersPath, busyUpdating, resources}) => {
    const onRejectDriver = () => {
        dispatch(rejectDriver(orderSummary.orderId, () => history.push(`${ordersPath}`)));
    };
    return <SpinnerButton padded busy={busyUpdating} fullWidth danger style={styles.ctaButton} onPress={onRejectDriver}><Text uppercase={false}>{resources.RejectButtonCaption}</Text></SpinnerButton>
};

const  CompleteOrder = ({orderSummary, history, ordersPath, busyUpdating, resources}) => {
    const onCompleteOrder = () => {
        dispatch(customerCompleteOrder(orderSummary.orderId, () => history.push(`${ordersPath}`)));
    };
    return <SpinnerButton padded busy={busyUpdating} fullWidth danger style={styles.ctaButton} onPress={onCompleteOrder}><Text uppercase={false}>Complete</Text></SpinnerButton>
};

class OrderStatusButtons extends Component {
    static propTypes = {
        me: PropTypes.object.isRequired,
        orderSummary: PropTypes.object.isRequired
    }
    constructor(props){
        super(props);
        ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
    }

    render(){
        const {ordersPath, orderSummary, dispatch, me, contentType} = this.props;
        
        const possibleStatuses = OrderStatuses.getPossibleStatuses({
            orderSummaryStatus: orderSummary.status,
            iAmTheCustomer: orderSummary.userId === me.userId,
            doubleComplete: contentType.doubleComplete
        });
        return <View column>
            {!!~possibleStatuses.indexOf(OrderStatuses.CANCELLED) ? <CancelOrder {...this.props}/> : null}
            {!!~possibleStatuses.indexOf(OrderStatuses.PLACED) ? <RejectDriver {...this.props}/> : null}
            {!!~possibleStatuses.indexOf(OrderStatuses.COMPLETEDBYCUSTOMER) ? <CompleteOrder {...this.props}/> : null}
        </View>
    }
}

const styles = {
    ctaButton: {
        marginTop: 10
    }
};
