import React, {Component} from 'react';
import {Grid, Row, Col, Text, Content, Header, Body, Container, Title, Item, Label, Left, Button} from 'native-base';
import yup from 'yup';
import {ErrorRegion, ValidatingInput, ValidatingButton, Icon} from 'common/components';
import {connect} from 'custom-redux';
import {loginUserByUsernameAndPassword} from 'common/actions/CommonActions';
import {isAnyOperationPending, getOperationError} from 'common/dao';

class CustomerLogin extends Component {
  constructor(props) {
    super(props);

    this.state = {
      email: undefined,
      password: undefined
    };
  }

  render() {
    const {dispatch, history, errors, busy} = this.props;
    const {email, password} = this.state;

    const onChangeText = async (field, value) => {
      this.setState({[field]: value});
    };

    const login = async() => {
      dispatch(loginUserByUsernameAndPassword({email, password}, () => history.replace('/Root')));
    };

    return <Container>
      <Header withButton>
        <Left>
          <Button onPress={() => history.goBack()}>
            <Icon name='back-arrow'/>
          </Button>
        </Left>
        <Body><Title>Login</Title></Body>
      </Header>
      <Content keyboardShouldPersistTaps="always">
        <Grid>
          <Row>
            <Col>
              <Item stackedLabel>
                <Label>Email</Label>
                <ValidatingInput bold value={email}
                  placeholder="email@email.com"
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
                  placeholder="*****"
                  onChangeText={(value) => onChangeText('password', value)}
                  validationSchema={validationSchema.password}/>
              </Item>
            </Col>
          </Row>
        </Grid>
      </Content>
      <ErrorRegion errors={errors}/>
      <ValidatingButton paddedBottomLeftRight fullWidth iconRight
        onPress={login} validationSchema={yup.object(validationSchema)} busy={busy} model={this.state}>
        <Text uppercase={false}>Sign In</Text>
        <Icon next name='forward-arrow'/>
      </ValidatingButton>
    </Container>;
  }
}

const validationSchema = {
  email: yup.string().email().required(),
  password: yup.string().required()
};

const mapStateToProps = (state, initialProps) => ({
  ...initialProps,
  errors: getOperationError(state, 'loginDao', 'loginUserByUsernameAndPassword'),
  busy: isAnyOperationPending(state, [{ loginDao: 'loginUserByUsernameAndPassword'}])
});

export default connect(mapStateToProps)(CustomerLogin);
