/*
 * Copyright 2016 Claymore Minds Limited and Niche Solutions (UK) Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.viewserver.expression.function;

import io.viewserver.controller.ControllerUtils;
import io.viewserver.expression.tree.IExpression;
import io.viewserver.expression.tree.IExpressionDouble;
import io.viewserver.expression.tree.IExpressionString;
import io.viewserver.schema.column.ColumnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * Created by Paul on 19/11/2015.
 */
public class Distance implements IUserDefinedFunction, IExpressionDouble {
    private static final Logger log = LoggerFactory.getLogger(Serialize.class);
    private IExpressionDouble lat1, lat2, lng1, lng2;
    private IExpressionString unit;

    @Override
    public void setParameters(IExpression... parameters) {
        if (parameters.length != 5) {
            throw new IllegalArgumentException("Syntax: distance(<latitude1 (double)>, <longitude2 (double)>, <latitude2 (double)>, <longitude2 (double)>, <unit (string) M|K|N>)");
        }

        lat1 = (IExpressionDouble) parameters[0];
        lng1 = (IExpressionDouble) parameters[1];
        lat2 = (IExpressionDouble) parameters[2];
        lng2 = (IExpressionDouble) parameters[3];
        unit = (IExpressionString) parameters[4];
    }

    @Override
    public ColumnType getType() {
        return ColumnType.Double;
    }

    @Override
    public double getDouble(int row) {
        return distance(lat1.getDouble(row), lng1.getDouble(row), lat2.getDouble(row), lng2.getDouble(row), unit.getString(row));
    }

    static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == "K") {
            dist = dist * 1.609344;
        } else if (unit == "N") {
            dist = dist * 0.8684;
        }

        return (dist);
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }
}


