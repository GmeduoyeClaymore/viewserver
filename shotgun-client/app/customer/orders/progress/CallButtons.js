
import React, {Component} from 'react';
import {callPartner} from 'customer/actions/CustomerActions';

export default class CallButtons extends Component{
  constructor(props){
    super(props);
  }

  render(){
    const {errors, order, dispatch} = this.props;

    const onPressCallPartner = async () => {
      dispatch(callPartner(order.orderId));
    };
  
    <ErrorRegion errors={errors}>
      <Button fullWidth callButtonSml onPress={onPressCallPartner}>
        <Icon name="phone" paddedIcon/>
        <Text uppercase={false}>Call partner</Text>
      </Button>
    </ErrorRegion>;
  }
}
