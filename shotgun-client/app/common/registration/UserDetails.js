import React from 'react';
import {Grid, Row, Col, Text, Content, Header, Body, Container, Title, Item, Label, Left, Button} from 'native-base';
import yup from 'yup';
import {ValidatingInput, ValidatingButton, Icon} from 'common/components';
import {merge} from 'lodash';

export default UserDetails  = ({context, history, next}) => {
  const {user} = context.state;

  const onChangeText = async (field, value) => {
    context.setState({user: merge(user, {[field]: value})});
  };

  return <Container>
    <Header withButton>
      <Left>
        <Button>
          <Icon name='arrow-back' onPress={() => history.goBack()} />
        </Button>
      </Left>
      <Body><Title>Your Details</Title></Body>
    </Header>
    <Content keyboardShouldPersistTaps="always">
      <Grid>
        <Row>
          <Col>
            <Item stackedLabel first>
              <Label>First name</Label>
              <ValidatingInput bold value={user.firstName} placeholder="John" validateOnMount={user.firstName !== undefined} onChangeText={(value) => onChangeText('firstName', value)} validationSchema={validationSchema.firstName} maxLength={30}/>
            </Item>
          </Col>
        </Row>
        <Row>
          <Col>
            <Item stackedLabel>
              <Label>Last name</Label>
              <ValidatingInput bold value={user.lastName} placeholder="Smith" validateOnMount={user.lastName !== undefined} onChangeText={(value) => onChangeText('lastName', value)} validationSchema={validationSchema.lastName} maxLength={30}/>
            </Item>
          </Col>
        </Row>
        <Row>
          <Col>
            <Item stackedLabel>
              <Label>Phone number</Label>
              <ValidatingInput bold keyboardType='phone-pad' placeholder="01234 56678" validateOnMount={user.contactNo !== undefined} value={user.contactNo} onChangeText={(value) => onChangeText('contactNo', value)} validationSchema={validationSchema.contactNo}/>
            </Item>
          </Col>
        </Row>
        <Row>
          <Col>
            <Item stackedLabel>
              <Label>Email</Label>
              <ValidatingInput bold keyboardType='email-address' placeholder="email@email.com" validateOnMount={user.email !== undefined} value={user.email} onChangeText={(value) => onChangeText('email', value)} validationSchema={validationSchema.email} maxLength={30}/>
            </Item>
          </Col>
        </Row>
        <Row>
          <Col>
            <Item stackedLabel last>
              <Label>Create an account password</Label>
              <ValidatingInput bold secureTextEntry={true} placeholder="****" value={user.password} validateOnMount={user.password !== undefined} onChangeText={(value) => onChangeText('password', value)} validationSchema={validationSchema.password} maxLength={30}/>
            </Item>
          </Col>
        </Row>
      </Grid>
    </Content>
    <ValidatingButton paddedBottom fullWidth iconRight validateOnMount={true} style={styles.continueButton} onPress={() => history.push(next)} validationSchema={yup.object(validationSchema)} model={user}>
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
  contactNo: yup.string().required().matches(/^(((\+44\s?\d{4}|\(?0\d{4}\)?)\s?\d{3}\s?\d{3})|((\+44\s?\d{3}|0\d{3})\s?\d{3}\s?\d{4})|((\+44\s?\d{2}|0\d{2})\s?\d{4}\s?\d{4}))?$/).max(35),
};
