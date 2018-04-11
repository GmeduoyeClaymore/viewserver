
import React, {Component} from 'react';
import {callDriver} from 'customer/actions/CustomerActions';

export default class CallButtons extends Component{
  constructor(props){
    super(props);
  }

  render(){
    const {errors, orderSummary, dispatch} = this.props;

    const onPressCallDriver = async () => {
      dispatch(callDriver(orderSummary.orderId));
    };
  
    <ErrorRegion errors={errors}>
      <Button fullWidth callButtonSml onPress={onPressCallDriver}>
        <Icon name="phone" paddedIcon/>
        <Text uppercase={false}>Call driver</Text>
      </Button>
    </ErrorRegion>;
  }
}
