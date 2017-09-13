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

import io.viewserver.expression.tree.IExpression;
import io.viewserver.expression.tree.IExpressionDouble;
import io.viewserver.schema.column.ColumnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Paul on 19/11/2015.
 */
public class Distance implements IUserDefinedFunction, IExpressionDouble {
    private static final Logger log = LoggerFactory.getLogger(Serialize.class);
    private IExpressionDouble lat1, lat2, lng1, lng2;
    public static final double R = 6372.8; // Radius of the world in Km

    @Override
    public void setParameters(IExpression... parameters) {
        if (parameters.length != 4) {
            throw new IllegalArgumentException("Syntax: distance(<latitude1 (double)>, <longitude2 (double)>, <latitude2 (double)>, <longitude2 (double)>)");
        }

        lat1 = (IExpressionDouble) parameters[0];
        lng1 = (IExpressionDouble) parameters[1];
        lat2 = (IExpressionDouble) parameters[2];
        lng2 = (IExpressionDouble) parameters[3];
    }

    @Override
    public ColumnType getType() {
        return ColumnType.Double;
    }

    @Override
    public double getDouble(int row) {
        return haversine(lat1.getDouble(row), lng1.getDouble(row), lat2.getDouble(row), lng2.getDouble(row));
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return R * c;
    }

}