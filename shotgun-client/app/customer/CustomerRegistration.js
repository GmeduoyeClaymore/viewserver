import React, {Component, PropTypes} from 'react';
import { View, Text, Image, StyleSheet } from 'react-native';
import ActionButton from '../common/components/ActionButton.js';
import nextIcon from  '../common/assets/chevron-double-right.png';
import Logger from '../viewserver-client/Logger';
import CustomerDao from './data/CustomerDao';
const validateRequiredFields = context => (fieldNames) => {
    const state = context.state;
    return new Promise(
        (resolve, reject) => {
                const result = {};
                fieldNames.forEach(fieldname => {
                    if (!state[fieldname]){
                        result[fieldname] = `${fieldname} is a required field`;
                    }
                });
                context.setState({validationErrors: result},
                    () => {
                        if (Object.keys(result).length){
                            reject('Validation errors found');
                        } else {
                            resolve();
                        }
                    });
            }
        );
};

const onFieldItemChanged =  context => (fieldname, fieldValue) => {
    context.setState({[fieldname]: fieldValue});
};

const renderTextInputControl = context => (fieldname, title, maxLength, keyboardType = 'text') => {
    const {state} = context;
    const validationErrorsForField = state.validationErrors[fieldname];
    return <View>
        {validationErrorsForField ? <Text style={{color: 'red'}}>${validationErrorsForField}</Text> : null}
        <TextInput placeholder={title} keyboardType={keyboardType} onChangeText ={(text)=> onFieldItemChanged(context)(fieldname, text)} value = {state[fieldname]} maxLength={maxLength}  />;
    </View>;
};


class PersonalInformation extends Component {
    constructor(props){
        super(props);
        this.context = props.context;
    }

    get name(){
        return 'Personal Information';
    }

    async validate(){
        return validateRequiredFields(this.context)([
            'title',
            'forename',
            'surname',
            'phone',
            'email',
        ]);
    }
    render(){
        return <View>
            {renderTextInputControl(this.context)('title', 'Title', 10)}
            {renderTextInputControl(this.context)('forename', 'Forename', 10)}
            {renderTextInputControl(this.context)('surname', 'Surname', 10)}
            {renderTextInputControl(this.context)('phone', 'Phone', 12)}
            {renderTextInputControl(this.context)('email', 'Email', 12)}
        </View>;
    }
}

class AddressDetails extends Component {
    constructor(props){
        super(props);
        this.context = props.context;
    }

    get name(){
        return 'Address Details';
    }

    async validate(){
        return validateRequiredFields(this.context)([
            'line1',
            'city',
            'postcode',
        ]);
    }
    render(){
        return <View>
            {renderTextInputControl(this.context)('postcode', 'Postcode', 10)}
            {renderTextInputControl(this.context)('line1', 'Address Line 1', 20)}
            {renderTextInputControl(this.context)('line2', 'Address Line 2', 20)}
            {renderTextInputControl(this.context)('line3', 'Address Line 3', 20)}
            {renderTextInputControl(this.context)('city', 'City', 12)}
            {renderTextInputControl(this.context)('county', 'County', 12)}
        </View>;
    }
}

class CardDetails extends Component {
    constructor(props){
        super(props);
        this.context = props.context;
    }

    get name(){
        return 'Card Details';
    }

    async validate(){
        return this.validateRequiredFields(this.context)([
            'nameOncard',
            'cardnumber',
            'expirationDateMonth',
            'expirationDateYear',
            'securityCode'
        ]);
    }
    render(){
        return <View>
            {renderTextInputControl(this.context)('nameOncard', 'Name', 10)}
            {renderTextInputControl(this.context)('cardnumber', 'Card No.', 10)}
            {renderTextInputControl(this.context)('expirationDateMonth', 'Exp Month', 10)}
            {renderTextInputControl(this.context)('expirationDateYEar', 'Exp Year', 10)}
            {renderTextInputControl(this.context)('securitycode', 'Security Code', 10)}
        </View>;
    }
}

export default class CustomerRegistration extends Component {
    constructor(props){
        super(props);
        this.client = this.props.screenProps.client;
        this.customerDao = this.props.screenProps.customerDao || new CustomerDao(this.client, CustomerDao.generateCustomerId());
        const context = this;
        this.state = {
            stepCounter: 0
        };
        this.registrationSteps = [
            new PersonalInformation({context}),
            new AddressDetails({context}),
            new CardDetails({context})
        ];
    }

    async submitRegistration(){
        const {navigation} = this.props;
        const {customerDao} = this;
        try {
            await customerDao.addOrUpdateCustomer(this.state);
            navigation.navigate('Home', {product});
        } catch (errors){
            this.setState({errors});
        }
    }

    async proceeedToNextItem(){
        const {stepCounter} = this.state;
        const RegistrationStep  = this.registrationSteps[stepCounter];
        try {
            await RegistrationStep.validate();
            this.setState({stepCounter: stepCounter + 1});
        } catch (error){
            Logger.info(`Issue with validation ${error}`);
        }
    }

    goToStep(index){
        this.setState({stepCounter: index});
    }

    render(){
        const {stepCounter, error} = this.state;
        const RegistrationStep  = this.registrationSteps[stepCounter];
        const finalStep = stepCounter == this.registrationSteps.length;
        const buttonText = finalStep ? 'Complete' : 'Next';
        const buttonAction = finalStep ? this.proceeedToNextItem : this.submitRegistration;
        return <View>
            {error ? <Text style={{color: 'red'}}>{error}</Text> : null}
            {this.registrationSteps.map((c, idx) => <Text onClick={stepCounter === idx ? null : this.goToStep(idx)}>{c.name}</Text> )}
            <RegistrationStep/>
            <ActionButton icon={nextIcon} action={buttonAction}  buttonText={buttonText}/>
        </View>;
    }
}
