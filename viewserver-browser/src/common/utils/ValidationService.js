export  class ValidationService {
    static async validate(model, validationSchema){
      let mappedValidation = {};
      try {
        await validationSchema.validate(model, {abortEarly: false, stripUnknown: true});
        mappedValidation = {error : undefined};
      } catch (err) {
        if(err.inner){
            err.inner.forEach(error => {
                mappedValidation[error.path] = error.message;
            })
            mappedValidation.error = err.inner.map(c => c.message).join("\n");
        }else{
            mappedValidation = {error: err.message};
        }
      }
      return mappedValidation;
    }
  }
  
  export default  ValidationService;