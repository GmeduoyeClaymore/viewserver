import ProtoLoader from '../core/ProtoLoader';
import {$} from '../core/JQueryish';

export default class ReportContextMapper{
  static toDto(reportContext) {
    const reportContextCopy = $.extend(true, {}, reportContext);
    ReportContextMapper._enrichParameters(reportContextCopy.parameters);

    const reportContextDto = ProtoLoader.Dto.ReportContextDto.create({
      reportId: reportContextCopy.reportId,
      multiContextMode: reportContextCopy.multiContextMode,
      output: reportContextCopy.output,
      parameters: ReportContextMapper._mapParameters(reportContextCopy.parameters, ReportContextMapper._buildParameter),
      dimensions: ReportContextMapper._mapParameters(reportContextCopy.dimensions, ReportContextMapper._buildDimension),
      excludedFilters: ReportContextMapper._mapParameters(reportContextCopy.excludedFilters, ReportContextMapper._buildDimension),
      childContexts: ReportContextMapper._mapChildContexts(reportContextCopy.childContexts)
    });

    console.log('REPCONTEXT');
    console.log(reportContextDto);

    return reportContextDto;
  }
        
  static _mapChildContexts(childContexts){
    const _self = ReportContextMapper;
    const childContextDtos = [];

    if (childContexts !== undefined) {
      childContexts.forEach((childContext) => {
        childContextDtos.push(_self.toDto(childContext));
      });
    }
    return childContextDtos;
  }
        
  static _enrichParameters(parameters){
    if (parameters.aggregators && parameters.aggregators.length > 1){
      ReportContextMapper._addSubTotalParameters(parameters);
    }
  }
        
  static _addSubTotalParameters(parameters){
    let subTotalString = '';
    parameters.subtotals = [];

    parameters.aggregators.forEach((aggregator) => {
      subTotalString += aggregator + '|';
      parameters.subtotals.push(subTotalString + 'bucket');
    });
  }
        
  static _buildParameter(name, value) {
    return ProtoLoader.Dto.ReportContextDto.ParameterValue.create({
      name,
      value
    });
  }
        
  static _buildDimension(name, value) {
    return ProtoLoader.Dto.ReportContextDto.Dimension.create({
      name,
      value
    });
  }
        
  static _mapParameters(parameters, builder) {
    const parametersDto = [];

    if (parameters !== undefined) {
      $.each(parameters, (key, value) => {
        if (Object.prototype.toString.call(value) !== '[object Array]') {
          value = [value];
        }

        const newValue = [];
        value.forEach((val) => {
          if (val !== undefined && val !== null){
            newValue.push(val);
          }
        });
        value = newValue;
        let valueDto = ProtoLoader.Dto.ReportContextDto.Value.create();
        let valuesListDto;
        let field;

        value.forEach((currentValue, index) => {
          //get the type for the values list based on the first type in the values list
          //TODO - maybe do this better, based on reportDefinition?
          if (index === 0) {
            if (typeof currentValue === 'string') {
              field = 'string';
              console.log('creating string');
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
          const val = {[field + 'List']: valuesListDto};
          valueDto = ProtoLoader.Dto.ReportContextDto.Value.create(val);
        }

        const parameterValueDto = builder(key, valueDto);

        parametersDto.push(parameterValueDto);
      });
    }

    return parametersDto;
  }
}
