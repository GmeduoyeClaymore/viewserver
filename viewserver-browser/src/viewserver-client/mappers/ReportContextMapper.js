import ProtoLoader from '../core/ProtoLoader';

export default class ReportContextMapper{
  static toDto(reportContext) {
    const reportContextDto = ProtoLoader.Dto.ReportContextDto.create(reportContext);
    reportContextDto.parameters = ReportContextMapper._mapParameters(reportContext.parameters, ReportContextMapper._buildParameter);
    return reportContextDto;
  }

  static _mapChildContexts(childContexts){
    const _self = ReportContextMapper;
    const childContextDtos = [];

    if (childContexts !== undefined) {
      childContexts.forEach(childContext => {
        childContextDtos.push(_self.toDto(childContext));
      });
    }
    return childContextDtos;
  }

  static _buildParameter(name, value) {
    return ProtoLoader.Dto.ReportContextDto.ParameterValue.create({
      name,
      value
    });
  }

  static _buildDimension(name, value) {
    return new ProtoLoader.Dto.ReportContextDto.Dimension({
      name,
      value
    });
  }

  static _mapParameters(parameters, builder) {
    const parametersDto = [];

    if (parameters !== undefined) {
      Object.entries(parameters).forEach(([key, value]) => {
        if (Object.prototype.toString.call(value) !== '[object Array]') {
          value = [value];
        }

        const newValue = [];
        value.forEach(val => {
          if (val !== undefined && val !== null){
            newValue.push(val);
          }
        });
        value = newValue;
        const valueDto = ProtoLoader.Dto.ReportContextDto.Value.create();
        let valuesListDto;
        let field;

        value.forEach((currentValue, index) => {
          //get the type for the values list based on the first type in the values list
          //TODO - maybe do this better, based on reportDefinition?
          //!!!!!! DO NOT CHANGE this to === type coercion is needed !!!!!!
          if (index == 0) {
            if (typeof currentValue === 'string') {
              field = 'string';
              valuesListDto = ProtoLoader.Dto.ReportContextDto.StringList.create();
            } else if (typeof currentValue === 'number') {
              field = currentValue % 1 === 0 ? 'int' : 'double';
              valuesListDto = currentValue % 1 === 0 ? ProtoLoader.Dto.ReportContextDto.IntList.create() : ProtoLoader.Dto.ReportContextDto.DoubleList.create();
            } else if (typeof currentValue === 'boolean') {
              field = 'boolean';
              valuesListDto = ProtoLoader.Dto.ReportContextDto.BooleanList.create();
            } else if (typeof currentValue === 'undefined') {
              field = 'null';
              valuesListDto = ProtoLoader.Dto.ReportContextDto.NullList.create();
            } else {
              field = 'string';
              valuesListDto = ProtoLoader.Dto.ReportContextDto.StringList.create();
            }
          }

          valuesListDto[field + 'Value'].push(currentValue);
        });
        if (field) {
          valueDto[field + 'List'] = valuesListDto;
        }

        const parameterValueDto = builder(key, valueDto);

        parametersDto.push(parameterValueDto);
      });
    }

    return parametersDto;
  }
}
