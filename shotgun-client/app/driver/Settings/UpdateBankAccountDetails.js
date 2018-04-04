import React, {Component} from 'react';
import {Text, Content, Header, Left, Body, Container, Button, Title, Grid, Row, Col, Item, Label, View} from 'native-base';
import yup from 'yup';
import {ValidatingInput, ValidatingButton, ErrorRegion, Icon} from 'common/components';
import {connect} from 'custom-redux';
import {isAnyOperationPending, getDaoState, getOperationErrors} from 'common/dao';
import {setBankAccount} from 'driver/actions/DriverActions';

class UpdateBankAccountDetails extends Component {
  constructor(props) {
    super(props);

    this.state = {
      bankAccount: {
        accountNumber: undefined,
        sortCode: undefined
      }
    };
  }

  render(){
    const {currentBankAccount = {}, history, dispatch, busy, errors, parentPath} = this.props;
    const {bankAccount} = this.state;

    const onChangeText = async (field, value) => {
      this.setState({bankAccount: {...this.state.bankAccount, [field]: value}});
    };

    const onSetBankAccount = async () => {
      dispatch(setBankAccount(bankAccount, () => history.push(`${parentPath}/DriverSettingsLanding`)));
    };

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
                <Text bold>{currentBankAccount.bankName}</Text>
                <Text>Account ending {currentBankAccount.last4}</Text>
                <Text note>Sort Code {currentBankAccount.routingNumber}</Text>
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
                <ValidatingInput bold placeholder="123456789" value={bankAccount.accountNumber}
                  validateOnMount={bankAccount.accountNumber !== undefined}
                  onChangeText={(value) => onChangeText('accountNumber', value)}
                  validationSchema={validationSchema.accountNumber} maxLength={8}/>
              </Item>
            </Col>
          </Row>
          <Row>
            <Col>
              <Item stackedLabel last>
                <Label>Sort code</Label>
                <ValidatingInput bold placeholder="12-34-56" value={bankAccount.sortCode}
                  validateOnMount={bankAccount.sortCode !== undefined}
                  onChangeText={(value) => onChangeText('sortCode', value)}
                  validationSchema={validationSchema.sortCode} maxLength={10}/>
              </Item>
            </Col>
          </Row>
        </Grid>
      </Content>
      <ErrorRegion errors={errors}>
        <ValidatingButton paddedBottom fullWidth iconRight busy={busy} validateOnMount={true}
          onPress={onSetBankAccount}
          validationSchema={yup.object(validationSchema)} model={bankAccount}>
          <Text uppercase={false}>Set Bank Account</Text>
        </ValidatingButton>
      </ErrorRegion>
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
  currentBankAccount: getDaoState(state, ['bankAccount'], 'paymentDao')
});


export default connect(mapStateToProps, true, false)(UpdateBankAccountDetails);
