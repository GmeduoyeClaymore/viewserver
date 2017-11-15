export default class ValidationService {
  static async validate(model, validationSchema){
    let mappedValidation;
    try {
      await validationSchema.validate(model, {abortEarly: false, stripUnknown: true});
      mappedValidation = {error: ''};
    } catch (err) {
      mappedValidation = {error: err.message};
    }
    return mappedValidation;
  }
}
