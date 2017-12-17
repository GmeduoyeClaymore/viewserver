import React from 'react';
import { Form, Text, Content, Header, Body, Container, Title} from 'native-base';
import yup from 'yup';
import ValidatingInput from 'common/components/ValidatingInput';
import ValidatingButton from 'common/components/ValidatingButton';
import {merge} from 'lodash';

export default UserDetails  = ({context, history, match, next}) => {
  const {user = {}} = context.state;

  const onChangeText = async (field, value) => {
    context.setState({user: merge(user, {[field]: value})});
  };

  return <Container>
    <Header>
      <Body><Title>Personal Details</Title></Body>
    </Header>
    <Content>
      <Form style={{display: 'flex', flex: 1}}>
        <ValidatingInput placeholder="First Name" value={user.firstName} onChangeText={(value) => onChangeText('firstName', value)} validationSchema={UserDetails.validationSchema.firstName} maxLength={30}/>
        <ValidatingInput placeholder="Last Name" value={user.lastName} onChangeText={(value) => onChangeText('lastName', value)} validationSchema={UserDetails.validationSchema.lastName} maxLength={30}/>
        <ValidatingInput placeholder="Password" secureTextEntry={true} value={user.password} onChangeText={(value) => onChangeText('password', value)} validationSchema={UserDetails.validationSchema.password} maxLength={30}/>
        <ValidatingInput placeholder="Email" keyboardType='email-address' value={user.email} onChangeText={(value) => onChangeText('email', value)} validationSchema={UserDetails.validationSchema.email} maxLength={30}/>
        <ValidatingInput placeholder="Phone Number" keyboardType='phone-pad' value={user.contactNo} onChangeText={(value) => onChangeText('contactNo', value)} validationSchema={UserDetails.validationSchema.contactNo}/>
        <ValidatingButton onPress={() => history.push(`${match.path}/${next}`)} validationSchema={yup.object(UserDetails.validationSchema)} model={user}>
          <Text>Next</Text>
        </ValidatingButton>
      </Form>
    </Content>
  </Container>;
};

UserDetails.validationSchema = {
  firstName: yup.string().required().max(30),
  lastName: yup.string().required().max(30),
  password: yup.string().required().max(30),
  email: yup.string().required().email().max(100),
  contactNo: yup.string().required().matches(/^[0-9,\+,\-,\s,\(,\)]*$/).max(35),
};
