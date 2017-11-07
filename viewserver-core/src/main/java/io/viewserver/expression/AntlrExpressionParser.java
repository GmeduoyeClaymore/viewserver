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

import io.viewserver.datasource.DimensionMapper;
import io.viewserver.datasource.IDimensionMapper;
import io.viewserver.expression.function.FunctionRegistry;
import io.viewserver.expression.function.HookingContext;
import io.viewserver.expression.parser.ExpressionVisitorImpl;
import io.viewserver.expression.parser.IColumnTrackingExpressionVisitor;
import io.viewserver.expression.tree.IExpression;
import io.viewserver.expressions.ExpressionLexer;
import io.viewserver.expressions.ExpressionParser;
import io.viewserver.schema.Schema;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

import java.util.BitSet;
import java.util.Map;

/**
 * Created by nick on 16/02/2015.
 */
public class AntlrExpressionParser implements IExpressionParser {
    private FunctionRegistry functionRegistry;
    private IDimensionMapper dimensionMapper;

    public AntlrExpressionParser() {
    }

    public AntlrExpressionParser(FunctionRegistry functionRegistry) {
        this.functionRegistry = functionRegistry;
    }

    public AntlrExpressionParser(FunctionRegistry functionRegistry, DimensionMapper dimensionMapper) {
        this.functionRegistry = functionRegistry;
        this.dimensionMapper = dimensionMapper;
    }

    @Override
    public void setDimensionMapper(IDimensionMapper dimensionMapper) {
        this.dimensionMapper = dimensionMapper;
    }

    @Override
    public IDimensionMapper getDimensionMapper(){
        return this.dimensionMapper;
    }

    @Override
    public IExpression parse(String expressionText, Schema schema, Map<String, String> columnAliases) {
        return parse(expressionText, schema, columnAliases, null, null);
    }

    @Override
    public IExpression parse(String expressionText, Schema schema, Map<String, String> columnAliases, BitSet columnsUsed, HookingContext hookingContext) {
        return parse(expressionText, new ExpressionVisitorImpl(schema, functionRegistry, columnAliases, dimensionMapper, hookingContext), columnsUsed);
    }

    @Override
    public <TResult> TResult parse(String expressionText, ParseTreeVisitor<TResult> parseTreeVisitor) {
        return parse(expressionText, parseTreeVisitor, null);
    }

    @Override
    public <TResult> TResult parse(String expressionText, ParseTreeVisitor<TResult> parseTreeVisitor, BitSet columnsUsed) {
        ExpressionLexer lexer = new ExpressionLexer(new ANTLRInputStream(expressionText));
        ExpressionParser parser = new ExpressionParser(new CommonTokenStream(lexer));
        ExpressionParser.ParseContext parse = parser.parse();
        TResult result = parseTreeVisitor.visit(parse);
        if (columnsUsed != null && parseTreeVisitor instanceof IColumnTrackingExpressionVisitor) {
            columnsUsed.or(((IColumnTrackingExpressionVisitor)parseTreeVisitor).getColumnsUsed());
        }
        return result;
    }
}
