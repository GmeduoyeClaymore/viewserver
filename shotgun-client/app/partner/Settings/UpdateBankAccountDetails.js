import React, {Component} from 'react';
import {Text, Content, Header, Left, Body, Container, Button, Title, Grid, Row, Col, Item, Label, View} from 'native-base';
import yup from 'yup';
import {ValidatingInput, ValidatingButton, ErrorRegion, Icon} from 'common/components';
import {withExternalState} from 'custom-redux';
import {isAnyOperationPending, getDaoState, getOperationErrors, getNavigationProps} from 'common/dao';
import {setBankAccount} from 'partner/actions/PartnerActions';
import shotgun from 'native-base-theme/variables/shotgun';
import PaymentInfo from 'common/settings/PaymentInfo';

class UpdateBankAccountDetails extends Component {
  onChangeText = async (field, value) => {
    const {unsavedBankAccount} = this.props;
    this.setState({unsavedBankAccount: {...unsavedBankAccount, [field]: value}});
  }

  onSetBankAccount = async () => {
    const {history, dispatch, unsavedBankAccount, address, next} = this.props;
    dispatch(setBankAccount(unsavedBankAccount, address, () => {
      history.replace(next);
      this.setState({unsavedBankAccount: undefined});
    }));
  }
  
  render(){
    const {user, history, busy, errors, unsavedBankAccount = {}} = this.props;
    const {bankAccount} = user;

    return <Container>
      <Header withButton>
        <Left>
          <Button onPress={() => history.goBack()}>
            <Icon name='back-arrow'/>
          </Button>
        </Left>
        <Body><Title>Bank Account Details</Title></Body>
      </Header>
      <Content keyboardShouldPersistTaps="always">
        <Grid>
          {bankAccount ? <Row style={styles.currentAccountRow}>
            <Col>
              <Text>Current Bank Account</Text>
            </Col>
            <Col>
              <View>
                <Text bold>{bankAccount.bankName}</Text>
                <Text>Account ending {bankAccount.last4}</Text>
                <Text note>Sort Code {bankAccount.sortCode}</Text>
              </View>
            </Col>
          </Row> :
            <Row style={styles.currentAccountRow}>
              <Col>
                <Text>Enter your bank account details so we can pay you once the job is completed</Text>
              </Col>
            </Row>}
          {bankAccount ? <Row style={styles.updateAccountRow}>
            <Text>Update bank account details</Text>
          </Row> : null}
          <Row>
            <Col>
              <Item stackedLabel>
                <Label>Account number</Label>
                <ValidatingInput  keyboardType='phone-pad' bold placeholder="123456789" value={unsavedBankAccount.accountNumber}
                  validateOnMount={unsavedBankAccount.accountNumber !== undefined}
                  onChangeText={(value) => this.onChangeText('accountNumber', value)}
                  validationSchema={validationSchema.accountNumber} maxLength={8}/>
              </Item>
            </Col>
          </Row>
          <Row>
            <Col>
              <Item stackedLabel>
                <Label>Sort code</Label>
                <ValidatingInput  keyboardType='phone-pad' bold placeholder="12-34-56" value={unsavedBankAccount.sortCode}
                  validateOnMount={unsavedBankAccount.sortCode !== undefined}
                  onChangeText={(value) => this.onChangeText('sortCode', value)}
                  validationSchema={validationSchema.sortCode} maxLength={10}/>
              </Item>
            </Col>
          </Row>
          {!bankAccount ?  <Row>
            <PaymentInfo chargePercentage={user.chargePercentage}></PaymentInfo>
          </Row> :
            null}
        </Grid>
      </Content>
      <ErrorRegion errors={errors}/>
      <ValidatingButton paddedBottomLeftRight fullWidth iconRight busy={busy} validateOnMount={true}
        onPress={this.onSetBankAccount}
        validationSchema={yup.object(validationSchema)} model={{...unsavedBankAccount, dob: unsavedBankAccount}}>
        <Text uppercase={false}>Set Bank Account</Text>
      </ValidatingButton>
    </Container>;
  }
}

const styles = {
  currentAccountRow: {
    margin: shotgun.contentPadding,
    marginTop: 0
  },
  updateAccountRow: {
    margin: shotgun.contentPadding
  }
};

const validationSchema = {
  accountNumber: yup.string().required().matches(/^\d{8}$/),
  sortCode: yup.string().required().matches(/^\d{2}-?\d{2}-?\d{2}$/)
};

const mapStateToProps = (state, initialProps) => {
  const next = getNavigationProps(initialProps).next || initialProps.next;

  return {
    ...initialProps,
    next,
    busy: isAnyOperationPending(state, [{ paymentDao: 'setBankAccount'}]),
    errors: getOperationErrors(state, [{paymentDao: 'setBankAccount'}]),
    user: getDaoState(state, ['user'], 'userDao'),
    address: getDaoState(state, ['customer', 'homeAddress'], 'deliveryAddressDao')
  };
};

export default withExternalState(mapStateToProps)(UpdateBankAccountDetails);
