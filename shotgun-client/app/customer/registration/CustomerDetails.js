import React from 'react';
import { Form, Text} from 'native-base';
import yup from 'yup';
import ValidatingInput from 'common/components/ValidatingInput';
import ValidatingButton from 'common/components/ValidatingButton';
import {merge} from 'lodash';

export default CustomerDetails  = ({navigation, screenProps}) => {
  const {context} = screenProps;
  const {customer = {}} = context.state;

  const onChangeText = async (field, value) => {
    context.setState({customer: merge(customer, {[field]: value})});
  };

  return <Form style={{display: 'flex', flex: 1}}>
    <ValidatingInput placeholder="First Name" value={customer.firstName} onChangeText={(value) => onChangeText('firstName', value)} validationSchema={CustomerDetails.validationSchema.firstName} maxLength={30}/>
    <ValidatingInput placeholder="Last Name" value={customer.lastName}  onChangeText={(value) => onChangeText('lastName', value)} validationSchema={CustomerDetails.validationSchema.lastName} maxLength={30}/>
    <ValidatingInput placeholder="Email" keyboardType='email-address' value={customer.email} onChangeText={(value) => onChangeText('email', value)} validationSchema={CustomerDetails.validationSchema.email} maxLength={30}/>
    <ValidatingInput placeholder="Phone Number" keyboardType='phone-pad' value={customer.contactNo} onChangeText={(value) => onChangeText('contactNo', value)} validationSchema={CustomerDetails.validationSchema.contactNo}/>
    <ValidatingButton onPress={() => navigation.navigate('AddressDetails')} validationSchema={yup.object(CustomerDetails.validationSchema)} model={customer}>
      <Text>Next</Text>
    </ValidatingButton>
  </Form>;
};

CustomerDetails.validationSchema = {
  firstName: yup.string().required().max(30),
  lastName: yup.string().required().max(30),
  email: yup.string().required().email().max(100),
  contactNo: yup.string().required().matches(/^[0-9,\+,\-,\s,\(,\)]*$/).max(35),
};

CustomerDetails.navigationOptions = {title: 'Personal Details'};
