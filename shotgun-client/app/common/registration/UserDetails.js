import React from 'react';
import {Grid, Row, Col, Text, Content, Header, Body, Container, Title, Icon, Item, Label} from 'native-base';
import yup from 'yup';
import ValidatingInput from 'common/components/ValidatingInput';
import ValidatingButton from 'common/components/ValidatingButton';
import {merge} from 'lodash';

export default UserDetails  = ({context, history, match, next}) => {
  const {user} = context.state;

  const onChangeText = async (field, value) => {
    context.setState({user: merge(user, {[field]: value})});
  };

  return <Container>
    <Header>
      <Body><Title>Your Details</Title></Body>
    </Header>
    <Content>
      <Grid>
        <Row>
          <Col>
            <Item stackedLabel first>
              <Label>First name</Label>
              <ValidatingInput bold value={user.firstName} validateOnMount={user.firstName !== undefined} onChangeText={(value) => onChangeText('firstName', value)} validationSchema={validationSchema.firstName} maxLength={30}/>
            </Item>
          </Col>
        </Row>
        <Row>
          <Col>
            <Item stackedLabel>
              <Label>Last name</Label>
              <ValidatingInput bold value={user.lastName} validateOnMount={user.lastName !== undefined} onChangeText={(value) => onChangeText('lastName', value)} validationSchema={validationSchema.lastName} maxLength={30}/>
            </Item>
          </Col>
        </Row>
        <Row>
          <Col>
            <Item stackedLabel>
              <Label>Phone number</Label>
              <ValidatingInput bold keyboardType='phone-pad' validateOnMount={user.contactNo !== undefined} value={user.contactNo} onChangeText={(value) => onChangeText('contactNo', value)} validationSchema={validationSchema.contactNo}/>
            </Item>
          </Col>
        </Row>
        <Row>
          <Col>
            <Item stackedLabel>
              <Label>Email</Label>
              <ValidatingInput bold keyboardType='email-address' validateOnMount={user.email !== undefined} value={user.email} onChangeText={(value) => onChangeText('email', value)} validationSchema={validationSchema.email} maxLength={30}/>
            </Item>
          </Col>
        </Row>
        <Row>
          <Col>
            <Item stackedLabel last>
              <Label>Create an account password</Label>
              <ValidatingInput bold secureTextEntry={true} value={user.password} validateOnMount={user.password !== undefined} onChangeText={(value) => onChangeText('password', value)} validationSchema={validationSchema.password} maxLength={30}/>
            </Item>
          </Col>
        </Row>
      </Grid>
    </Content>
    <ValidatingButton paddedBottom fullWidth iconRight validateOnMount={true} style={styles.continueButton} onPress={() => history.push(`${match.path}/${next}`)} validationSchema={yup.object(validationSchema)} model={user}>
      <Text uppercase={false}>Continue</Text>
      <Icon name='arrow-forward'/>
    </ValidatingButton>
  </Container>;
};

const styles = {
  continueButton: {
    marginTop: 50
  }
};

const validationSchema = {
  firstName: yup.string().required().max(30),
  lastName: yup.string().required().max(30),
  password: yup.string().required().max(30),
  email: yup.string().required().email().max(100),
  contactNo: yup.string().required().matches(/^[0-9,\+,\-,\s,\(,\)]*$/).max(35),
};
