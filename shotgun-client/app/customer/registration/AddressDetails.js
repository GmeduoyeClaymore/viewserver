import React from 'react';
import {Form, Text} from 'native-base';
import yup from 'yup';
import {merge} from 'lodash';
import ValidatingInput from '../../common/components/ValidatingInput';
import ValidatingButton from '../../common/components/ValidatingButton';

export default AddressDetails  = ({navigation, screenProps}) => {
  const {context} = screenProps;
  const {deliveryAddress = {}} = context.state;

  const onChangeText = async (field, value) => {
    context.setState({deliveryAddress: merge(deliveryAddress, {[field]: value})});
  };

  return <Form style={{display: 'flex', flex: 1}}>
    <ValidatingInput placeholder="Line 1" value={deliveryAddress.line1} onChangeText={(value) => onChangeText('line1', value)} validationSchema={AddressDetails.validationSchema.line1} maxLength={30}/>
    <ValidatingInput placeholder="Line 2" value={deliveryAddress.line2} onChangeText={(value) => onChangeText('line2', value)} validationSchema={AddressDetails.validationSchema.line2} maxLength={30}/>
    <ValidatingInput placeholder="City" value={deliveryAddress.city} onChangeText={(value) => onChangeText('city', value)} validationSchema={AddressDetails.validationSchema.city} maxLength={30}/>
    <ValidatingInput placeholder="Country" value={deliveryAddress.country} onChangeText={(value) => onChangeText('country', value)} validationSchema={AddressDetails.validationSchema.country} maxLength={30}/>
    <ValidatingInput placeholder="Postcode" value={deliveryAddress.postcode} onChangeText={(value) => onChangeText('postcode', value)} validationSchema={AddressDetails.validationSchema.postcode} maxLength={30}/>
    <ValidatingButton onPress={() => navigation.navigate('PaymentCardDetails')} validationSchema={yup.object(AddressDetails.validationSchema)} model={deliveryAddress}>
      <Text>Next</Text>
    </ValidatingButton>
  </Form>;
};

AddressDetails.validationSchema = {
  line1: yup.string().required().max(30),
  line2: yup.string().required().max(30),
  city: yup.string().required().max(30),
  country: yup.string().required().max(30),
  postcode: yup.string()
    .matches(/^([A-Z]{1,2}[0-9][0-9A-Z]?\s?[0-9][A-Z]{2})|(INCOMP)$/i)
    .required()
};

AddressDetails.navigationOptions = {title: 'Address Details'};
