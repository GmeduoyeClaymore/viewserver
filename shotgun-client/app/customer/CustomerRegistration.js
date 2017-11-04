import React, {Component, PropTypes} from 'react';
import { View, Text, TextInput, TouchableOpacity } from 'react-native';
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

const renderTextInputControl = context => (fieldname, title, maxLength, keyboardType = 'default') => {
    const {state} = context;
    const validationErrorsForField = state.validationErrors[fieldname];
    return (<View>
        {validationErrorsForField ? <Text style={{color: 'red'}}>{validationErrorsForField}</Text> : null}
        <TextInput placeholder={title} keyboardType={keyboardType} onChangeText ={(text)=> onFieldItemChanged(context)(fieldname, text)} value = {state[fieldname]} maxLength={maxLength}  />
    </View>);
};


class PersonalInformation {
    constructor(context){
        this.context = context;
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
    render = () => (<View style={{display: 'flex', flex: 1}}>
            {renderTextInputControl(this.context)('title', 'Title', 10)}
            {renderTextInputControl(this.context)('forename', 'Forename', 10)}
            {renderTextInputControl(this.context)('surname', 'Surname', 10)}
            {renderTextInputControl(this.context)('phone', 'Phone', 12, 'phone-pad')}
            {renderTextInputControl(this.context)('email', 'Email', 12, 'email-address')}
        </View>)
}

class AddressDetails  {
    constructor(context){
        this.context = context;
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
    render = () => (<View style={{display: 'flex', flex: 1}}>
            {renderTextInputControl(this.context)('postcode', 'Postcode', 10)}
            {renderTextInputControl(this.context)('line1', 'Address Line 1', 20)}
            {renderTextInputControl(this.context)('line2', 'Address Line 2', 20)}
            {renderTextInputControl(this.context)('line3', 'Address Line 3', 20)}
            {renderTextInputControl(this.context)('city', 'City', 12)}
            {renderTextInputControl(this.context)('county', 'County', 12)}
        </View>)
}

class CardDetails{
    constructor(context){
        this.context = context;
    }

    get name(){
        return 'Card Details';
    }

    async validate(){
        return validateRequiredFields(this.context)([
            'nameOncard',
            'cardnumber',
            'expirationDateMonth',
            'expirationDateYear',
            'securitycode'
        ]);
    }
    render = () => (<View  style={{display: 'flex', flex: 1}}>
            {renderTextInputControl(this.context)('nameOncard', 'Name', 10)}
            {renderTextInputControl(this.context)('cardnumber', 'Card No.', 10, 'phone-pad')}
            {renderTextInputControl(this.context)('expirationDateMonth', 'Exp Month', 10, 'phone-pad')}
            {renderTextInputControl(this.context)('expirationDateYear', 'Exp Year', 10, 'phone-pad')}
            {renderTextInputControl(this.context)('securitycode', 'Security Code', 10, 'phone-pad')}
        </View>)
}

export default class CustomerRegistration extends Component {
    constructor(props){
        super(props);
        this.client = this.props.screenProps.client;
        this.customerDao = this.props.screenProps.customerDao || new CustomerDao(this.client, CustomerDao.generateCustomerId());
        this.proceeedToNextItem = this.proceeedToNextItem.bind(this);
        this.submitRegistration = this.submitRegistration.bind(this);
        this.nextStep = this.nextStep.bind(this);
        const context = this;
        this.state = {
            stepCounter: 0,
            validationErrors: {},
            errors: []
        };
        this.registrationSteps = [
            new PersonalInformation(context),
            new AddressDetails(context),
            new CardDetails(context)
        ];
    }

    async submitRegistration(){
        const {navigation} = this.props;
        const {customerDao} = this;
        try {
            const {
                line1,
                line2,
                line3,
                city,
                county,
            } = this.state;

            const addressInformation = {
                line1,
                line2,
                line3,
                city,
                county
            };

            const {
                nameOncard,
                cardnumber,
                expirationDateMonth,
                expirationDateYear,
                securitycode
            } = this.state;

            const paymentInformation = {
                nameOncard,
                cardnumber,
                expirationDateMonth,
                expirationDateYear,
                securitycode
            };

            const {
                title,
                forename,
                surname,
                phone,
                email,
            } = this.state;

            const customerInformation = {
                title,
                forename,
                surname,
                phone,
                email,
                deliveryAddress: JSON.stringify(addressInformation),
                paymentDetails: JSON.stringify(paymentInformation)
            };

            await customerDao.addOrUpdateCustomer(customerInformation);
            navigation.navigate('Home', {});
        } catch (errors){
            this.setState({errors: [errors]});
        }
    }

    async proceeedToNextItem(action){
        const {stepCounter} = this.state;
        const RegistrationStep  = this.registrationSteps[stepCounter];
        try {
            await RegistrationStep.validate();
            await action();
        } catch (error){
            Logger.info(`Issue with validation ${error}`);
            this.setState({errors: [error]});
        }
    }

    nextStep(){
        const {stepCounter} = this.state;
        this.setState({stepCounter: stepCounter + 1, errors: []});
    }

    goToStep(index){
        this.setState({stepCounter: index});
    }

    render(){
        const {stepCounter, errors} = this.state;
        const registrationStep  = this.registrationSteps[stepCounter];
        const finalStep = stepCounter === this.registrationSteps.length - 1;
        const buttonText = finalStep ? 'Complete' : 'Next';
        const buttonAction = finalStep ? () => this.proceeedToNextItem(this.submitRegistration) : () => this.proceeedToNextItem(this.nextStep);
        return (<View>
            {errors ? errors.map((c, idx)=> <Text key={idx} style={{color: 'red'}}>{c.message}</Text> ) : null}
            {this.registrationSteps.map((c, idx) => <TouchableOpacity  key={idx} onPress={idx < stepCounter ? () => this.goToStep(idx) : null}><Text>{c.name + (idx == stepCounter ? '*' : '') }</Text></TouchableOpacity>  )}
            <View style={{display: 'flex', flexDirection: 'column', height: 600}}>
            {registrationStep.render()}
            <ActionButton icon={nextIcon} action={buttonAction}  buttonText={buttonText}/>
            </View>
        </View>);
    }
}
