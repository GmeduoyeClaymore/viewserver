import React from 'react';
import {Text, Content, Header, Left, Body, Container, Button, Icon, Title, Grid, Row, Col, Item, Label} from 'native-base';
import yup from 'yup';
import ValidatingInput from 'common/components/ValidatingInput';
import ValidatingButton from 'common/components/ValidatingButton';
import {merge} from 'lodash';
import {connect} from 'react-redux';
import {withRouter} from 'react-router';

const BankAccountDetails = ({context, history}) => {
  const {bankAccount} = context.state;

  const onChangeText = async (field, value) => {
    context.setState({bankAccount: merge(bankAccount, {[field]: value})});
  };

  return <Container>
    <Header withButton>
      <Left>
        <Button>
          <Icon name='arrow-back' onPress={() => history.goBack()}/>
        </Button>
      </Left>
      <Body><Title>Payment Details</Title></Body>
    </Header>
    <Content keyboardShouldPersistTaps="always">
      <Grid>
        <Row>
          <Col>
            <Item stackedLabel first>
              <Label>Account number</Label>
              <ValidatingInput bold value={bankAccount.accountNumber} validateOnMount={bankAccount.accountNumber !== undefined} onChangeText={(value) => onChangeText('accountNumber', value)} validationSchema={validationSchema.accountNumber} maxLength={8}/>
            </Item>
          </Col>
        </Row>
        <Row>
          <Col>
            <Item stackedLabel last>
              <Label>Sort code</Label>
              <ValidatingInput bold value={bankAccount.sortCode} validateOnMount={bankAccount.sortCode !== undefined} onChangeText={(value) => onChangeText('sortCode', value)} validationSchema={validationSchema.sortCode} maxLength={10}/>
            </Item>
          </Col>
        </Row>
      </Grid>
    </Content>
    <ValidatingButton paddedBottom fullWidth iconRight validateOnMount={true} onPress={() => history.push('/Driver/Registration/VehicleDetails')} validationSchema={yup.object(validationSchema)} model={bankAccount}>
      <Text uppercase={false}>Continue</Text>
      <Icon name='arrow-forward'/>
    </ValidatingButton>
  </Container>;
};

const validationSchema = {
  accountNumber: yup.string().required().matches(/^\d{8}$/),
  sortCode: yup.string().required().matches(/^\d{2}-?\d{2}-?\d{2}$/)
};

export default withRouter(connect()(BankAccountDetails));
