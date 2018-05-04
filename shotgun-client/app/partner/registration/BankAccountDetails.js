import React from 'react';
import {Text, Content, Header, Left, Body, Container, Button, Title, Grid, Row, Col, Item, Label} from 'native-base';
import yup from 'yup';
import {ValidatingButton, ValidatingInput, Icon} from 'common/components';
import {withExternalState} from 'custom-redux';

const BankAccountDetails = ({bankAccount, history, setState, dispatch}) => {
  const onChangeText = async (field, value) => {
    setState({bankAccount: {...bankAccount, [field]: value}}, undefined, dispatch);
  };

  return <Container>
    <Header withButton>
      <Left>
        <Button onPress={() => history.goBack()}>
          <Icon name='back-arrow'/>
        </Button>
      </Left>
      <Body><Title>Payment Details</Title></Body>
    </Header>
    <Content keyboardShouldPersistTaps="always">
      <Grid>
        <Row>
          <Col>
            <Item stackedLabel>
              <Label>Account number</Label>
              <ValidatingInput bold placeholder="123456789" value={bankAccount.accountNumber} validateOnMount={bankAccount.accountNumber !== undefined} onChangeText={(value) => onChangeText('accountNumber', value)} validationSchema={validationSchema.accountNumber} maxLength={8}/>
            </Item>
          </Col>
        </Row>
        <Row>
          <Col>
            <Item stackedLabel last>
              <Label>Sort code</Label>
              <ValidatingInput bold placeholder="12-34-56" value={bankAccount.sortCode} validateOnMount={bankAccount.sortCode !== undefined} onChangeText={(value) => onChangeText('sortCode', value)} validationSchema={validationSchema.sortCode} maxLength={10}/>
            </Item>
          </Col>
        </Row>
      </Grid>
    </Content>
    <ValidatingButton paddedBottom fullWidth iconRight validateOnMount={true} onPress={() => history.push('/Partner/Registration/PartnerAccountType')} validationSchema={yup.object(validationSchema)} model={bankAccount}>
      <Text uppercase={false}>Continue</Text>
      <Icon next name='forward-arrow'/>
    </ValidatingButton>
  </Container>;
};

const validationSchema = {
  accountNumber: yup.string().required(),  // BREAKS IN IOS .matches(/^\d{8}$/),
  sortCode: yup.string().required() // BREAKS IN IOS .matches(/^\d{2}-?\d{2}-?\d{2}$/)
};

export default withExternalState()(BankAccountDetails);