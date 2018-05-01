package io.viewserver.expression.function;

import io.viewserver.controller.ControllerUtils;
import io.viewserver.expression.tree.IExpression;
import io.viewserver.expression.tree.IExpressionDouble;
import io.viewserver.expression.tree.IExpressionString;
import io.viewserver.schema.column.ColumnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class DistanceJson implements IUserDefinedFunction, IExpressionDouble {
    private static final Logger log = LoggerFactory.getLogger(Serialize.class);
    private IExpressionDouble latExp, lngExp;
    private IExpressionString unit;
    private IExpressionString locationJson;

    @Override
    public void setParameters(IExpression... parameters) {
        if (parameters.length != 4) {
            throw new IllegalArgumentException("Syntax: distance(<location (json)>, <latitude2 (double)>, <longitude2 (double)>, <unit (string) M|K|N>)");
        }

        locationJson = (IExpressionString) parameters[0];
        latExp = (IExpressionDouble) parameters[1];
        lngExp = (IExpressionDouble) parameters[2];
        unit = (IExpressionString) parameters[3];
    }

    @Override
    public ColumnType getType() {
        return ColumnType.Double;
    }

    @Override
    public double getDouble(int row) {
        String string = locationJson.getString(row);
        if(string == null){
            return -1;
        }
        HashMap<String,Object> location = ControllerUtils.mapDefault(string);
        Double lat = (Double) location.get("lat");
        Double lng = (Double) location.get("lng");
        return Distance.distance(lat == null ? 0 : lat, lng == null ? 0 : lng, latExp.getDouble(row), lngExp.getDouble(row), unit.getString(row));
    }

}
