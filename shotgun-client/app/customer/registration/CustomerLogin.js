import React, {Component} from 'react';
import {Grid, Row, Col, Text, Content, Header, Body, Container, Title, Icon, Item, Label, Left, Button} from 'native-base';
import yup from 'yup';
import ValidatingInput from 'common/components/ValidatingInput';
import ValidatingButton from 'common/components/ValidatingButton';
import {connect} from 'react-redux';
import {loginCustomer} from 'customer/actions/CustomerActions';
import {isAnyOperationPending, getOperationError} from 'common/dao';
import ErrorRegion from 'common/components/ErrorRegion';

class CustomerLogin extends Component {
  constructor(props) {
    super(props);

    this.state = {
      email: undefined,
      password: undefined
    };
  }

  render() {
    const {dispatch, history, errors} = this.props;
    const {email, password} = this.state;

    const onChangeText = async (field, value) => {
      this.setState({[field]: value});
    };

    const login = async() => {
      dispatch(loginCustomer(email, password, () => history.push('/Root')));
    };

    return <Container>
      <Header withButton>
        <Left>
          <Button>
            <Icon name='arrow-back' onPress={() => history.goBack()}/>
          </Button>
        </Left>
        <Body><Title>Login</Title></Body>
      </Header>
      <Content keyboardShouldPersistTaps="always">
        <Grid>
          <Row>
            <Col>
              <Item stackedLabel first>
                <Label>Email</Label>
                <ValidatingInput bold value={email}
                  onChangeText={(value) => onChangeText('email', value)}
                  validationSchema={validationSchema.email}/>
              </Item>
            </Col>
          </Row>
          <Row>
            <Col>
              <Item stackedLabel last>
                <Label>Password</Label>
                <ValidatingInput bold secureTextEntry={true} value={password}
                  onChangeText={(value) => onChangeText('password', value)}
                  validationSchema={validationSchema.password}/>
              </Item>
            </Col>
          </Row>
        </Grid>
      </Content>
      <ErrorRegion errors={errors}>
        <ValidatingButton paddedBottom fullWidth iconRight
          onPress={login} validationSchema={yup.object(validationSchema)} model={this.state}>
          <Text uppercase={false}>Sign In</Text>
          <Icon name='arrow-forward'/>
        </ValidatingButton>
      </ErrorRegion>
    </Container>;
  }
}

const validationSchema = {
  email: yup.string().required(),
  password: yup.string().required()
};

const mapStateToProps = (state, initialProps) => ({
  ...initialProps,
  errors: getOperationError(state, 'customerDao', 'loginCustomer'),
  busy: isAnyOperationPending(state, { customerDao: 'loginCustomer'})
});

export default connect(mapStateToProps)(CustomerLogin);
