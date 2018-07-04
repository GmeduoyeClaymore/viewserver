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

package io.viewserver.expression;

import io.viewserver.datasource.IDimensionMapper;
import io.viewserver.expression.function.HookingContext;
import io.viewserver.expression.tree.IExpression;
import io.viewserver.schema.Schema;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

import java.util.BitSet;
import java.util.Map;

/**
 * Created by bemm on 16/02/2015.
 */
public interface IExpressionParser {
    void setDimensionMapper(IDimensionMapper dimensionMapper);

    IDimensionMapper getDimensionMapper();

    IExpression parse(String expressionText, Schema schema, Map<String, String> columnAliases);

    IExpression parse(String expressionText, Schema schema, Map<String, String> columnAliases, BitSet columnsUsed, HookingContext hookingContext);

    <TResult> TResult parse(String expressionText, ParseTreeVisitor<TResult> parseTreeVisitor);

    <TResult> TResult parse(String expressionText, ParseTreeVisitor<TResult> parseTreeVisitor, BitSet columnsUsed);
}
