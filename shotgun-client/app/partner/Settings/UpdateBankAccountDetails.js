import React, {Component} from 'react';
import {Text, Content, Header, Left, Body, Container, Button, Title, Grid, Row, Col, Item, Label, View} from 'native-base';
import yup from 'yup';
import {ValidatingInput, ValidatingButton, ErrorRegion, Icon} from 'common/components';
import {withExternalState} from 'custom-redux';
import {isAnyOperationPending, getDaoState, getOperationErrors} from 'common/dao';
import {setBankAccount} from 'partner/actions/PartnerActions';

class UpdateBankAccountDetails extends Component {
  onChangeText = async (field, value) => {
    const {unsavedBankAccount} = this.props;
    this.setState({unsavedBankAccount: {...unsavedBankAccount, [field]: value}});
  }

  onSetBankAccount = async () => {
    const {history, dispatch, parentPath, unsavedBankAccount} = this.props;
    dispatch(setBankAccount(unsavedBankAccount, () => {
      history.push(`${parentPath}/PartnerSettingsLanding`);
      this.setState({unsavedBankAccount: undefined});
    }));
  }
  
  render(){
    const {bankAccount = {}, history, busy, errors, unsavedBankAccount = {}} = this.props;

    return <Container>
      <Header withButton>
        <Left>
          <Button onPress={() => history.goBack()}>
            <Icon name='back-arrow'/>
          </Button>
        </Left>
        <Body><Title>Bank Account Details</Title></Body>
      </Header>
      <Content keyboardShouldPersistTaps="always" padded>
        <Grid>
          <Row>
            <Col>
              <Text>Current Bank Account</Text>
            </Col>
            <Col>
              <View>
                <Text bold>{bankAccount.bankName}</Text>
                <Text>Account ending {bankAccount.last4}</Text>
                <Text note>Sort Code {bankAccount.routingNumber}</Text>
              </View>
            </Col>
          </Row>
          <Row style={styles.bankAccountRow}>
            <Text>Update bank account details</Text>
          </Row>
          <Row>
            <Col>
              <Item stackedLabel>
                <Label>Account number</Label>
                <ValidatingInput bold placeholder="123456789" value={unsavedBankAccount.accountNumber}
                  validateOnMount={unsavedBankAccount.accountNumber !== undefined}
                  onChangeText={(value) => this.onChangeText('accountNumber', value)}
                  validationSchema={validationSchema.accountNumber} maxLength={8}/>
              </Item>
            </Col>
          </Row>
          <Row>
            <Col>
              <Item stackedLabel last>
                <Label>Sort code</Label>
                <ValidatingInput bold placeholder="12-34-56" value={unsavedBankAccount.sortCode}
                  validateOnMount={unsavedBankAccount.sortCode !== undefined}
                  onChangeText={(value) => this.onChangeText('sortCode', value)}
                  validationSchema={validationSchema.sortCode} maxLength={10}/>
              </Item>
            </Col>
          </Row>
        </Grid>
      </Content>
      <ErrorRegion errors={errors}/>
      <ValidatingButton paddedBottom fullWidth iconRight busy={busy} validateOnMount={true}
        onPress={this.onSetBankAccount}
        validationSchema={yup.object(validationSchema)} model={unsavedBankAccount}>
        <Text uppercase={false}>Set Bank Account</Text>
      </ValidatingButton>
    </Container>;
  }
}

const styles = {
  bankAccountRow: {
    marginTop: 25
  }
};

const validationSchema = {
  accountNumber: yup.string().required().matches(/^\d{8}$/),
  sortCode: yup.string().required().matches(/^\d{2}-?\d{2}-?\d{2}$/)
};

const mapStateToProps = (state, nextOwnProps) => ({
  ...nextOwnProps,
  busy: isAnyOperationPending(state, [{ paymentDao: 'getBankAccount'}, { paymentDao: 'setBankAccount'}]),
  errors: getOperationErrors(state, [{paymentDao: 'getBankAccount'}, {paymentDao: 'setBankAccount'}]),
  user: getDaoState(state, ['user'], 'userDao'),
  bankAccount: getDaoState(state, ['bankAccount'], 'paymentDao')
});

export default withExternalState(mapStateToProps)(UpdateBankAccountDetails);
