package com.shotgun.viewserver.user;

import com.shotgun.viewserver.ControllerUtils;
import io.viewserver.expression.function.IUserDefinedFunction;
import io.viewserver.expression.tree.IExpression;
import io.viewserver.expression.tree.IExpressionDouble;
import io.viewserver.expression.tree.IExpressionString;
import io.viewserver.schema.column.ColumnType;
import org.apache.commons.beanutils.ConvertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class GetLatLongFromAddress implements IUserDefinedFunction, IExpressionDouble {
    private static final Logger log = LoggerFactory.getLogger(GetPartnerResponseField.class);
    private IExpressionString addressLatLongFieldExpression;
    private IExpressionString addressExpression;
    private IExpressionDouble overrideExpression;

    @Override
    public void setParameters(IExpression... parameters) {
        if (parameters.length != 3) {
            throw new IllegalArgumentException("Syntax: getLatLongFromAddress(<latORLongField (string)>,<addressExpression (json-string)>,<overrideExpression (double)>");
        }
        addressLatLongFieldExpression = (IExpressionString) parameters[0];
        addressExpression = (IExpressionString) parameters[1];
        overrideExpression = (IExpressionDouble) parameters[2];
    }

    @Override
    public ColumnType getType() {
        return ColumnType.Double;
    }

    @Override
    public double getDouble(int row) {
        Double overrideValue = overrideExpression.getDouble(row);
        if(overrideValue != null && overrideValue.floatValue() != 0f && overrideValue.floatValue() != -1f ){
            return overrideValue;
        }

        String addressLatLongField = addressLatLongFieldExpression.getString(row);
        if(addressLatLongField == null || "".equals(addressLatLongField)){
            return -1;
        }
        String addressJson = addressExpression.getString(row);
        if(addressJson == null){
            return -1;
        }
        HashMap address = ControllerUtils.mapDefault(addressJson);
        if(address == null){
            return -1;
        }
        try {
            return (double) ConvertUtils.convert(address.get(addressLatLongField),double.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
