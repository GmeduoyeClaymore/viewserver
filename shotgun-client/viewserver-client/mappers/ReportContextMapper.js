import ProtoLoader from '../core/ProtoLoader';
import {$} from '../core/JQueryish';

export default class ReportContextMapper{
    
    static toDto(reportContext) {
        let reportContextCopy = $.extend(true, {}, reportContext);
        ReportContextMapper._enrichParameters(reportContextCopy.parameters);

        let reportContextDto = new ProtoLoader.Dto.ReportContextDto();
        reportContextDto.setParameters(ReportContextMapper._mapParameters(reportContextCopy.parameters, ReportContextMapper._buildParameter));
        reportContextDto.setDimensions(ReportContextMapper._mapParameters(reportContextCopy.dimensions, ReportContextMapper._buildDimension));
        reportContextDto.setExcludedFilters(ReportContextMapper._mapParameters(reportContextCopy.excludedFilters, ReportContextMapper._buildDimension));
        reportContextDto.setChildContexts(ReportContextMapper._mapChildContexts(reportContextCopy.childContexts));

        if(reportContextCopy.reportId !== undefined) {
            reportContextDto.setReportId(reportContextCopy.reportId);
        }

        if(reportContextCopy.multiContextMode !== undefined) {
            reportContextDto.setMultiContextMode(reportContextCopy.multiContextMode);
        }

        if(reportContextCopy.output !== undefined) {
            reportContextDto.setOutput(reportContextCopy.output);
        }

        return reportContextDto;
    }
        
    static _mapChildContexts(childContexts){
        var _self = ReportContextMapper;
        var childContextDtos = [];

        if(childContexts !== undefined) {
            $.each(childContexts, function (index, childContext) {
                childContextDtos.push(_self.toDto(childContext));
            });
        }
        return childContextDtos;
    }
        
    static _enrichParameters(parameters){
        if(parameters.aggregators && parameters.aggregators.length > 1){
            ReportContextMapper._addSubTotalParameters(parameters);
        }
    }
        
    static _addSubTotalParameters(parameters){
        var subTotalString = '';
        parameters.subtotals = [];

        $.each(parameters.aggregators, function(i, aggregator){
            subTotalString += aggregator + '|';
            parameters.subtotals.push(subTotalString + 'bucket');
        });
    }
        
    static _buildParameter(name, value) {
        return new ProtoLoader.Dto.ReportContextDto.ParameterValue({
            name: name,
            value: value
        });
    }
        
    static _buildDimension(name, value) {
        return new ProtoLoader.Dto.ReportContextDto.Dimension({
            name: name,
            value: value
        });
    }
        
    static _mapParameters(parameters, builder) {
        let parametersDto = [];

        if(parameters !== undefined) {
            $.each(parameters, function (key, value) {

                if (Object.prototype.toString.call(value) !== '[object Array]') {
                    value = [value];
                }

                let newValue = [];
                $.each(value,function(index,val){
                    if(val !== undefined && val !== null){
                        newValue.push(val);
                    }
                });
                value = newValue;
                let valueDto = new ProtoLoader.Dto.ReportContextDto.Value();
                let valuesListDto;
                let field;

                $.each(value, function (index, currentValue) {

                    //get the type for the values list based on the first type in the values list
                    //TODO - maybe do this better, based on reportDefinition?
                    if (index === 0) {
                        if (typeof currentValue === 'string') {
                            field = 'string';
                            valuesListDto = new ProtoLoader.Dto.ReportContextDto.StringList();
                        } else if (typeof currentValue === 'number') {
                            field = currentValue % 1 === 0 ? 'int' : 'double';
                            valuesListDto = currentValue % 1 === 0 ? new ProtoLoader.Dto.ReportContextDto.IntList() : new ProtoLoader.Dto.ReportContextDto.DoubleList();
                        } else if (typeof currentValue === 'boolean') {
                            field = 'boolean';
                            valuesListDto = new ProtoLoader.Dto.ReportContextDto.BooleanList();
                        } else if (typeof currentValue === 'undefined') {
                            field = 'null';
                            valuesListDto = new ProtoLoader.Dto.ReportContextDto.NullList();
                        } else {
                            field = 'string';
                            valuesListDto = new ProtoLoader.Dto.ReportContextDto.StringList();
                        }
                    }

                    valuesListDto[field + 'Value'].push(currentValue);

                });
                if (field) {
                    valueDto.set(field + 'List', valuesListDto);
                }

                let parameterValueDto = builder(key, valueDto);

                parametersDto.push(parameterValueDto);
            });
        }

        return parametersDto;
    }

}