import ProtoLoader from '../core/ProtoLoader';
import {$} from '../core/JQueryish';

export default class ReportContextMapper{
    
    toDto(reportContext) {
        let reportContextCopy = $.extend(true, {}, reportContext);
        this._enrichParameters(reportContextCopy.parameters);

        let reportContextDto = new ProtoLoader.Dto.ReportContextDto();
        reportContextDto.setParameters(this._mapParameters(reportContextCopy.parameters, this._buildParameter));
        reportContextDto.setDimensions(this._mapParameters(reportContextCopy.dimensions, this._buildDimension));
        reportContextDto.setExcludedFilters(this._mapParameters(reportContextCopy.excludedFilters, this._buildDimension));
        reportContextDto.setChildContexts(this._mapChildContexts(reportContextCopy.childContexts));

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
        
    _mapChildContexts(childContexts){
        var _self = this;
        var childContextDtos = [];

        if(childContexts !== undefined) {
            $.each(childContexts, function (index, childContext) {
                childContextDtos.push(_self.toDto(childContext));
            });
        }
        return childContextDtos;
    }
        
    _enrichParameters(parameters){
        if(parameters.aggregators && parameters.aggregators.length > 1){
            this._addSubTotalParameters(parameters);
        }
    }
        
    _addSubTotalParameters(parameters){
        var subTotalString = '';
        parameters.subtotals = [];

        $.each(parameters.aggregators, function(i, aggregator){
            subTotalString += aggregator + '|';
            parameters.subtotals.push(subTotalString + 'bucket');
        });
    }
        
    _buildParameter(name, value) {
        return new ProtoLoader.Dto.ReportContextDto.ParameterValue({
            name: name,
            value: value
        });
    }
        
    _buildDimension(name, value) {
        return new ProtoLoader.Dto.ReportContextDto.Dimension({
            name: name,
            value: value
        });
    }
        
    _mapParameters(parameters, builder) {
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