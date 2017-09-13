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

package io.viewserver.expression.parser;

import io.viewserver.expressions.ExpressionBaseVisitor;
import io.viewserver.expressions.ExpressionParser;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Created by nickc on 31/10/2014.
 */
public class ColumnAliasingVisitor extends ExpressionBaseVisitor<String> {
    private final Map<String, String> columnAliases;

    public ColumnAliasingVisitor(Map<String, String> columnAliases) {
        this.columnAliases = columnAliases;
    }

    @Override
    public String visitByteExpression(@NotNull ExpressionParser.ByteExpressionContext ctx) {
        return ctx.getText();
    }

    @Override
    public String visitParenthesisExpression(@NotNull ExpressionParser.ParenthesisExpressionContext ctx) {
        return "(" + this.visit(ctx.expression()) + ")";
    }

    @Override
    public String visitDivideExpression(@NotNull ExpressionParser.DivideExpressionContext ctx) {
        return this.visit(ctx.expression(0)) + "/" + this.visit(ctx.expression(1));
    }

    @Override
    public String visitModulusExpression(@NotNull ExpressionParser.ModulusExpressionContext ctx) {
        return this.visit(ctx.expression(0)) + "%" + this.visit(ctx.expression(1));
    }

    @Override
    public String visitExpressionList(@NotNull ExpressionParser.ExpressionListContext ctx) {
        String result = "[";
        for (int i = 0; i < ctx.expression().size(); i++) {
            if (i > 0) {
                result += ",";
            }
            result += this.visit(ctx.expression(i));
        }
        result += "]";
        return result;
    }

    @Override
    public String visitAddExpression(@NotNull ExpressionParser.AddExpressionContext ctx) {
        return this.visit(ctx.expression(0)) + "+" + this.visit(ctx.expression(1));
    }

    @Override
    public String visitDoubleExpression(@NotNull ExpressionParser.DoubleExpressionContext ctx) {
        return ctx.getText();
    }

    @Override
    public String visitOrExpression(@NotNull ExpressionParser.OrExpressionContext ctx) {
        return this.visit(ctx.expression(0)) + "||" + this.visit(ctx.expression(1));
    }

    @Override
    public String visitPowerExpression(@NotNull ExpressionParser.PowerExpressionContext ctx) {
        return this.visit(ctx.expression(0)) + "^" + this.visit(ctx.expression(1));
    }

    @Override
    public String visitNotEqualsExpression(@NotNull ExpressionParser.NotEqualsExpressionContext ctx) {
        return this.visit(ctx.expression(0)) + "!=" + this.visit(ctx.expression(1));
    }

    @Override
    public String visitShortExpression(@NotNull ExpressionParser.ShortExpressionContext ctx) {
        return ctx.getText();
    }

    @Override
    public String visitAndExpression(@NotNull ExpressionParser.AndExpressionContext ctx) {
        return this.visit(ctx.expression(0)) + "&&" + this.visit(ctx.expression(1));
    }

    @Override
    public String visitBoolExpression(@NotNull ExpressionParser.BoolExpressionContext ctx) {
        return ctx.getText();
    }

    @Override
    public String visitGreaterThanExpression(@NotNull ExpressionParser.GreaterThanExpressionContext ctx) {
        return this.visit(ctx.expression(0)) + ">" + this.visit(ctx.expression(1));
    }

    @Override
    public String visitEqualsExpression(@NotNull ExpressionParser.EqualsExpressionContext ctx) {
        return this.visit(ctx.expression(0)) + "=" + this.visit(ctx.expression(1));
    }

    @Override
    public String visitNullExpression(@NotNull ExpressionParser.NullExpressionContext ctx) {
        return ctx.getText();
    }

    @Override
    public String visitLessThanExpression(@NotNull ExpressionParser.LessThanExpressionContext ctx) {
        return this.visit(ctx.expression(0)) + "<" + this.visit(ctx.expression(1));
    }

    @Override
    public String visitIntExpression(@NotNull ExpressionParser.IntExpressionContext ctx) {
        return ctx.getText();
    }

    @Override
    public String visitLessThanOrEqualsExpression(@NotNull ExpressionParser.LessThanOrEqualsExpressionContext ctx) {
        return this.visit(ctx.expression(0)) + "<=" + this.visit(ctx.expression(1));
    }

    @Override
    public String visitStringExpression(@NotNull ExpressionParser.StringExpressionContext ctx) {
        return ctx.getText();
    }

    @Override
    public String visitLongExpression(@NotNull ExpressionParser.LongExpressionContext ctx) {
        return ctx.getText();
    }

    @Override
    public String visitNotExpression(@NotNull ExpressionParser.NotExpressionContext ctx) {
        return "!" + this.visit(ctx.expression());
    }

    @Override
    public String visitSubtractExpression(@NotNull ExpressionParser.SubtractExpressionContext ctx) {
        return this.visit(ctx.expression(0)) + "-" + this.visit(ctx.expression(1));
    }

    @Override
    public String visitFloatExpression(@NotNull ExpressionParser.FloatExpressionContext ctx) {
        return ctx.getText();
    }

    @Override
    public String visitMultiplyExpression(@NotNull ExpressionParser.MultiplyExpressionContext ctx) {
        return this.visit(ctx.expression(0)) + "*" + this.visit(ctx.expression(1));
    }

    @Override
    public String visitInExpression(@NotNull ExpressionParser.InExpressionContext ctx) {
        String result = this.visit(ctx.expression(0)) + " in [";
        ExpressionParser.ExpressionListContext expressionList = ((ExpressionParser.ListExpressionContext) ctx.expression(1)).list().expressionList();
        for (int i = 0; i < expressionList.expression().size(); i++) {
            if (i > 0) {
                result += ",";
            }
            result += this.visit(expressionList.expression(i));
        }
        result += "]";
        return result;
    }

    @Override
    public String visitGreaterThanOrEqualsExpression(@NotNull ExpressionParser.GreaterThanOrEqualsExpressionContext ctx) {
        return this.visit(ctx.expression(0)) + ">=" + this.visit(ctx.expression(1));
    }

    @Override
    public String visitUnaryMinusExpression(@NotNull ExpressionParser.UnaryMinusExpressionContext ctx) {
        return "-" + this.visit(ctx.expression());
    }

    @Override
    public String visitFunctionCallExpression(@NotNull ExpressionParser.FunctionCallExpressionContext ctx) {
        ExpressionParser.FunctionCallContext functionCall = ctx.functionCall();
        String result = functionCall.Identifier().getText() + "(";
        List<? extends ExpressionParser.ExpressionContext> parameters = functionCall.expressionList().expression();
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) {
                result += ",";
            }
            result += this.visit(parameters.get(i));
        }
        result += ")";
        return result;
    }

    @Override
    public String visitColumnExpression(@NotNull ExpressionParser.ColumnExpressionContext ctx) {
        String columnName = ctx.getText();
        if (columnAliases != null) {
            String actualName = columnAliases.get(columnName);
            if (actualName != null) {
                columnName = actualName;
            }
        }
        return columnName;
    }

    @Override
    public String visitColFunctionCallExpression(@NotNull ExpressionParser.ColFunctionCallExpressionContext ctx) {
        return ctx.getText();
    }
}
