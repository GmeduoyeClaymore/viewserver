import React from 'react';
import {UIManager} from 'react-native';
import {Container, Text} from 'native-base';
import {Provider} from 'react-redux';
import configureStore from './redux/ConfigureStore';
import Client from './viewserver-client/Client';
import Logger from 'common/Logger';
import ProtoLoader from './viewserver-client/core/ProtoLoader';
import PrincipalService from './common/services/PrincipalService';
import LandingCommon from './LandingCommon';
import RegistrationCommon from './RegistrationCommon';
import CustomerRegistration from './customer/registration/CustomerRegistration';
import DriverRegistration from './driver/registration/DriverRegistration';
import CustomerLanding from './customer/CustomerLanding';
import DriverLanding from './driver/DriverLanding';
import {NativeRouter, Route, Redirect, Switch, AndroidBackButton} from 'react-router-native';
import LoadingScreen from 'common/components/LoadingScreen';
import GenericJSONCommandPromise from 'common/promises/GenericJSONCommandPromise';
const store = configureStore();
UIManager.setLayoutAnimationEnabledExperimental(true);

export default class App extends React.Component {
  static INITIAL_ROOT_NAME = 'LandingCommon';

  constructor() {
    super();
    this.state = {
      isReady: false,
      isConnected: false
    };
    this.client = new Client('ws://localhost:6060/');
    this.makePayment = this.makePayment.bind(this);
    this.dispatch = store.dispatch;
    this.success = this.success.bind(this);
    this.successReject = this.successReject.bind(this);
    this.noController = this.noController.bind(this);
    this.invalidArgs = this.invalidArgs.bind(this);
    this.errorWithinExecution = this.errorWithinExecution.bind(this);
  }

  async componentDidMount() {
    let isConnected = false;
    try {
      Logger.debug('Mounting App Component');
      this.setState({ error : undefined});
      await ProtoLoader.loadAll();
      this.setInitialRoot();
      Logger.debug('Mounting App Component');
      await this.client.connect();
      isConnected = true;
      await this.setUserId()
    } catch (error){
      Logger.debug('Connection error - ' + error);
      this.setState({ error});
    }
    Logger.debug('App Component Mounted');
    this.setState({ isReady: true, isConnected });
  }

  async setUserId(){
    this.userId = await PrincipalService.getUserIdFromDevice();
  }

  setInitialRoot(){
    if (this.userId == undefined){
      App.INITIAL_ROOT_NAME = '/RegistrationCommon';
    } else {
      App.INITIAL_ROOT_NAME = '/LandingCommon';
      Logger.info(`Loading with customer id ${this.userId}`);
    }
  }

  async makePayment(makeCall){
    const commandExecutedPromise = new GenericJSONCommandPromise();
    try{
      this.setState({isReady: false})
      makeCall(commandExecutedPromise);
      const result =  await commandExecutedPromise.promise.timeoutWithError(2000,"Unable to make payment after 2 seconds");
      this.setState({paymentResult: result,isReady: true})
    }catch(error){
      this.setState({paymentResult: error,isReady: true})
    }
  }

  success(commandExecutedPromise){
    this.client.invokeJSONCommand(`paymentController`, "process", {name: "fooname", address: "fooAddress"}, commandExecutedPromise);
  }

  successReject(commandExecutedPromise){
    this.client.invokeJSONCommand(`paymentController`, "reject", {name: "fooname", address: "fooAddress"}, commandExecutedPromise);
  }

  successVoid(commandExecutedPromise){
    this.client.invokeJSONCommand(`paymentController`, "void", {name: "fooname", address: "fooAddress"}, commandExecutedPromise);
  }

  successNoParams(commandExecutedPromise){
    this.client.invokeJSONCommand(`paymentController`, "noParams", undefined, commandExecutedPromise);
  }

  noController(commandExecutedPromise){
    this.client.invokeJSONCommand(`paymentControllerx`, "process", {name: "fooname", address: "fooAddress"}, commandExecutedPromise);
  }

  invalidArgs(commandExecutedPromise){
    this.client.invokeJSONCommand(`paymentController`, "process", {name: "fooname", addressx: "fooAddress"}, commandExecutedPromise);
  }

  errorWithinExecution(commandExecutedPromise){
    this.client.invokeJSONCommand(`paymentController`, "process", {name: "error", address: "fooAddress"}, commandExecutedPromise);
  }

  render(){
    const {isReady,isConnected,error,paymentResult} = this.state;
    if (!isReady) {
      return <LoadingScreen text="...Loading"/>;
    } else if (!isConnected){
      return  <Container style={{flexDirection: 'column', flex: 1}}>
        <Text>{JSON.stringify(error)}</Text>
      </Container>;
    }
    return  <Container style={{flexDirection: 'column', flex: 1}}>
      <Text>{ "Result of executing command is " + JSON.stringify(paymentResult)}</Text>
    <Button  onPress={() => this.makePayment(this.success)}><Text>Success Process Payment</Text></Button>
    <Button  onPress={() => this.makePayment(this.successReject)}><Text>Success ASYNC Reject Payment</Text></Button>
    <Button  onPress={() => this.makePayment(this.successVoid)}><Text>Success Void Payment</Text></Button>
    <Button  onPress={() => this.makePayment(this.successNoParams)}><Text>Success No PArams Payment</Text></Button>
    <Button  onPress={() => this.makePayment(this.noController)}><Text>No Controller Payment</Text></Button>
    <Button  onPress={() => this.makePayment(this.invalidArgs)}><Text>Invalid Serialized Args</Text></Button>
    <Button  onPress={() => this.makePayment(this.errorWithinExecution)}><Text>Error Within execution</Text></Button>
  </Container>;
  }

  renderx() {
    const {isReady,isConnected,error} = this.state;
    if (!isReady) {
      return <LoadingScreen text="Connecting"/>;
    } else if (!isConnected){
      return  <Container style={{flexDirection: 'column', flex: 1}}>
        <Text>{error}</Text>
      </Container>;
    }
    const globalProps = {client: this.client, userId: this.userId, dispatch: this.dispatch};

    return <Provider store={store}>
      <NativeRouter>
        <AndroidBackButton>
          <Switch>
            <Route path="/Root" component={App}/>
            <Route path="/RegistrationCommon" exact render={(props) => <RegistrationCommon {...globalProps} {...props}/>}/>
            <Route path="/LandingCommon" exact render={(props) => <LandingCommon {...globalProps} {...props}/>}/>
            <Route path="/Customer/Registration" render={(props) => <CustomerRegistration {...globalProps} {...props}/>}/>
            <Route path="/Driver/Registration" render={(props) => <DriverRegistration {...globalProps} {...props}/>}/>
            <Route path="/Customer" render={(props) => <CustomerLanding {...globalProps} {...props}/>}/>
            <Route path="/Driver" render={(props) => <DriverLanding {...globalProps} {...props}/>}/>
            <Redirect to={App.INITIAL_ROOT_NAME}/>
          </Switch>
        </AndroidBackButton>
      </NativeRouter>
    </Provider>;
  }
}
