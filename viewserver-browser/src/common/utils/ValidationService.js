export  class ValidationService {
    static async validate(model, validationSchema){
      let mappedValidation = {};
      try {
        await validationSchema.validate(model, {abortEarly: false, stripUnknown: true});
        mappedValidation = undefined;
      } catch (err) {
        if(err.inner){
            err.inner.forEach(error => {
                mappedValidation[error.path] = error.message;
            })
        }else{
            mappedValidation = {error: err.message};
        }
      }
      return mappedValidation;
    }
  }
  
  export default  ValidationService;