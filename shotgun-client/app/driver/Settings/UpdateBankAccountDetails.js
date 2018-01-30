import React, {Component} from 'react';
import {Text, Content, Header, Left, Body, Container, Button, Icon, Title, Grid, Row, Col, Item, Label, View} from 'native-base';
import yup from 'yup';
import ValidatingInput from 'common/components/ValidatingInput';
import ValidatingButton from 'common/components/ValidatingButton';
import {merge} from 'lodash';
import {connect} from 'react-redux';
import {withRouter} from 'react-router';
import {isAnyOperationPending, getDaoState, getOperationErrors} from 'common/dao';
import {setBankAccount} from 'driver/actions/DriverActions';
import ErrorRegion from 'common/components/ErrorRegion';

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
    const {currentBankAccount, history, user, dispatch, busy, errors} = this.props;
    const {bankAccount} = this.state;

    const onChangeText = async (field, value) => {
      this.setState({bankAccount: merge(this.state.bankAccount, {[field]: value})});
    };

    const onSetBankAccount = async () => {
      dispatch(setBankAccount(user.stripeAccountId, bankAccount, () => history.push('/Driver/Settings')));
    };

    return <Container>
      <Header withButton>
        <Left>
          <Button>
            <Icon name='arrow-back' onPress={() => history.goBack()}/>
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
              <Item stackedLabel first>
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


export default withRouter(connect(mapStateToProps)(UpdateBankAccountDetails));
