import * as OrderStatuses from 'common/constants/OrderStatuses';
import PropTypes from 'prop-types';
import React, {Component} from 'react';
import { SpinnerButton} from 'common/components';
import Text from 'react-native';

import {View} from 'native-base';
import * as ContentTypes from 'common/constants/ContentTypes';

/*eslint-disable */
const resourceDictionary = new ContentTypes.ResourceDictionary().
  property('RejectButtonCaption', 'Reject').
    delivery('Reject Partner').
    personell('Reject Worker').
    rubbish('Reject Partner');
/*eslint-disable */

const  CancelOrder = ({orderSummary, history, ordersPath, busyUpdating}) => {
    const onCancelOrder = () => {
        dispatch(cancelOrder(orderSummary.orderId, () => history.push(`${ordersPath}`)));
    };
    return <SpinnerButton padded busy={busyUpdating} fullWidth danger style={styles.ctaButton} onPress={onCancelOrder}><Text uppercase={false}>Cancel</Text></SpinnerButton> 
};

const  RejectPartner = ({orderSummary, history, ordersPath, busyUpdating, resources}) => {
    const onRejectPartner = () => {
        dispatch(rejectPartner(orderSummary.orderId, () => history.push(`${ordersPath}`)));
    };
    return <SpinnerButton padded busy={busyUpdating} fullWidth danger style={styles.ctaButton} onPress={onRejectPartner}><Text uppercase={false}>{resources.RejectButtonCaption}</Text></SpinnerButton>
};

const  AcceptPartner = ({orderSummary, history, ordersPath, busyUpdating, resources}) => {
    const onAcceptPartner = () => {
        dispatch(rejectPartner(orderSummary.orderId, () => history.push(`${ordersPath}`)));
    };
    return <SpinnerButton padded busy={busyUpdating} fullWidth danger style={styles.ctaButton} onPress={onAcceptPartner}><Text uppercase={false}>{resources.RejectButtonCaption}</Text></SpinnerButton>
};

const  CompleteOrder = ({orderSummary, history, ordersPath, busyUpdating, resources}) => {
    const onCompleteOrder = () => {
        dispatch(customerCompleteOrder(orderSummary.orderId, () => history.push(`${ordersPath}`)));
    };
    return <SpinnerButton padded busy={busyUpdating} fullWidth danger style={styles.ctaButton} onPress={onCompleteOrder}><Text uppercase={false}>Complete</Text></SpinnerButton>
};

export default class OrderStatusButtons extends Component {
    static propTypes = {
        me: PropTypes.object.isRequired,
        order: PropTypes.object.isRequired,
        partnerResponses: PropTypes.object.isRequired
    }
    constructor(props){
        super(props);
        ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
    }

    render(){
        const {ordersPath, order, partnerResponses, dispatch, me, contentType} = this.props;
        return <View column>
            {!!~possibleStatuses.indexOf(OrderStatuses.PLACED) ? <RejectPartner {...this.props}/> : null}
            {!!~possibleStatuses.indexOf(OrderStatuses.COMPLETEDBYCUSTOMER) ? <CompleteOrder {...this.props}/> : null}
        </View>
    }
}

const styles = {
    ctaButton: {
        marginTop: 10
    }
};
