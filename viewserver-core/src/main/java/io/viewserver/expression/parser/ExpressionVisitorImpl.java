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

import io.viewserver.core.NullableBool;
import io.viewserver.datasource.Cardinality;
import io.viewserver.datasource.Dimension;
import io.viewserver.datasource.IDataSource;
import io.viewserver.datasource.IDimensionMapper;
import io.viewserver.expression.Cast;
import io.viewserver.expression.function.*;
import io.viewserver.expression.tree.*;
import io.viewserver.expression.tree.divide.DivideExpression;
import io.viewserver.expression.tree.equals.EqualsExpression;
import io.viewserver.expression.tree.gt.GTExpression;
import io.viewserver.expression.tree.gtequals.GTEqualsExpression;
import io.viewserver.expression.tree.like.LikeExpression;
import io.viewserver.expression.tree.literal.*;
import io.viewserver.expression.tree.lt.LTExpression;
import io.viewserver.expression.tree.ltequals.LTEqualsExpression;
import io.viewserver.expression.tree.minus.MinusExpression;
import io.viewserver.expression.tree.modulus.ModExpression;
import io.viewserver.expression.tree.multiply.MultiplyExpression;
import io.viewserver.expression.tree.notequals.NotEqualsExpression;
import io.viewserver.expression.tree.plus.PlusExpression;
import io.viewserver.expression.tree.power.PowerExpression;
import io.viewserver.expressions.ExpressionBaseVisitor;
import io.viewserver.expressions.ExpressionParser;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.*;
import javolution.text.TypeFormat;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

/**
 * Created by bemm on 14/10/2014.
 */
public class ExpressionVisitorImpl extends ExpressionBaseVisitor<IExpression> implements IColumnTrackingExpressionVisitor {
    private Schema schema;
    private FunctionRegistry functionRegistry;
    private Map<String, String> columnAliases;
    private IDimensionMapper dimensionMapper;
    private HookingContext hookingContext;
    private final BitSet columnsUsed = new BitSet();

    public ExpressionVisitorImpl(Schema schema, FunctionRegistry functionRegistry, Map<String, String> columnAliases,
                                 IDimensionMapper dimensionMapper, HookingContext hookingContext) {
        this.schema = schema;
        this.functionRegistry = functionRegistry;
        this.columnAliases = columnAliases;
        this.dimensionMapper = dimensionMapper;
        this.hookingContext = hookingContext;
    }

    @Override
    public BitSet getColumnsUsed() {
        return columnsUsed;
    }

    @Override
    public IExpression visitParenthesisExpression(@NotNull ExpressionParser.ParenthesisExpressionContext ctx) {
        return this.visit(ctx.expression());
    }

    @Override
    public IExpression visitCastExpression(@NotNull ExpressionParser.CastExpressionContext ctx) {
        String typeName = ctx.cast().CastType().getText();
        ColumnType columnType = ColumnType.fromString(typeName);
        IExpression expression = this.visit(ctx.expression());
        return new Cast.CastExpressionWrapper(expression, columnType);
    }

    @Override
    public IExpression visitNullExpression(@NotNull ExpressionParser.NullExpressionContext ctx) {
        return new LiteralNull();
    }

    @Override
    public IExpression visitIntExpression(@NotNull ExpressionParser.IntExpressionContext ctx) {
        String text = ctx.getText();
        String stripped = "Ii".contains(text.substring(text.length() - 1)) ? text.substring(0, text.length() - 1) : text;
        return new LiteralInt(TypeFormat.parseInt(stripped));
    }

    @Override
    public IExpression visitBoolExpression(@NotNull ExpressionParser.BoolExpressionContext ctx) {
        return new LiteralBool(TypeFormat.parseBoolean(ctx.getText()));
    }

    @Override
    public IExpression visitStringExpression(@NotNull ExpressionParser.StringExpressionContext ctx) {
        String text = ctx.getText();
        String stripped = text.substring(1, text.length() - 1);
        return new LiteralString(stripped);
    }

    @Override
    public IExpression visitFloatExpression(@NotNull ExpressionParser.FloatExpressionContext ctx) {
        String text = ctx.getText();
        String stripped = text.substring(0, text.length() - 1);
        return new LiteralFloat(TypeFormat.parseFloat(stripped));
    }

    @Override
    public IExpression visitDoubleExpression(@NotNull ExpressionParser.DoubleExpressionContext ctx) {
        String text = ctx.getText();
        String stripped = "Dd".contains(text.substring(text.length() - 1)) ? text.substring(0, text.length() - 1) : text;
        return new LiteralDouble(TypeFormat.parseDouble(stripped));
    }

    @Override
    public IExpression visitByteExpression(@NotNull ExpressionParser.ByteExpressionContext ctx) {
        String text = ctx.getText();
        String stripped = text.substring(0, text.length() - 1);
        return new LiteralByte(TypeFormat.parseByte(stripped));
    }

    @Override
    public IExpression visitShortExpression(@NotNull ExpressionParser.ShortExpressionContext ctx) {
        String text = ctx.getText();
        String stripped = text.substring(0, text.length() - 1);
        return new LiteralShort(TypeFormat.parseShort(stripped));
    }

    @Override
    public IExpression visitLongExpression(@NotNull ExpressionParser.LongExpressionContext ctx) {
        String text = ctx.getText();
        String stripped = text.substring(0, text.length() - 1);
        return new LiteralLong(TypeFormat.parseLong(stripped));
    }

    @Override
    public IExpression visitAddExpression(@NotNull ExpressionParser.AddExpressionContext ctx) {
        return createBinaryExpression(ExpressionParser.Plus, this.visit(ctx.expression(0)), this.visit(ctx.expression(1)));
    }

    @Override
    public IExpression visitSubtractExpression(@NotNull ExpressionParser.SubtractExpressionContext ctx) {
        return createBinaryExpression(ExpressionParser.Minus, this.visit(ctx.expression(0)), this.visit(ctx.expression(1)));
    }

    @Override
    public IExpression visitMultiplyExpression(@NotNull ExpressionParser.MultiplyExpressionContext ctx) {
        return createBinaryExpression(ExpressionParser.Mult, this.visit(ctx.expression(0)), this.visit(ctx.expression(1)));
    }

    @Override
    public IExpression visitDivideExpression(@NotNull ExpressionParser.DivideExpressionContext ctx) {
        return createBinaryExpression(ExpressionParser.Div, this.visit(ctx.expression(0)), this.visit(ctx.expression(1)));
    }

    @Override
    public IExpression visitModulusExpression(@NotNull ExpressionParser.ModulusExpressionContext ctx) {
        return createBinaryExpression(ExpressionParser.Mod, this.visit(ctx.expression(0)), this.visit(ctx.expression(1)));
    }

    @Override
    public IExpression visitPowerExpression(@NotNull ExpressionParser.PowerExpressionContext ctx) {
        return createBinaryExpression(ExpressionParser.Pow, this.visit(ctx.expression(0)), this.visit(ctx.expression(1)));
    }

    @Override
    public IExpression visitGreaterThanOrEqualsExpression(@NotNull ExpressionParser.GreaterThanOrEqualsExpressionContext ctx) {
        return createBinaryExpression(ExpressionParser.GTEquals, this.visit(ctx.expression(0)), this.visit(ctx.expression(1)));
    }

    @Override
    public IExpression visitLessThanOrEqualsExpression(@NotNull ExpressionParser.LessThanOrEqualsExpressionContext ctx) {
        return createBinaryExpression(ExpressionParser.LTEquals, this.visit(ctx.expression(0)), this.visit(ctx.expression(1)));
    }

    @Override
    public IExpression visitGreaterThanExpression(@NotNull ExpressionParser.GreaterThanExpressionContext ctx) {
        return createBinaryExpression(ExpressionParser.GT, this.visit(ctx.expression(0)), this.visit(ctx.expression(1)));
    }

    @Override
    public IExpression visitLessThanExpression(@NotNull ExpressionParser.LessThanExpressionContext ctx) {
        return createBinaryExpression(ExpressionParser.LT, this.visit(ctx.expression(0)), this.visit(ctx.expression(1)));
    }

    @Override
    public IExpression visitEqualsExpression(@NotNull ExpressionParser.EqualsExpressionContext ctx) {
        return createBinaryExpression(ExpressionParser.Equals, this.visit(ctx.expression(0)), this.visit(ctx.expression(1)));
    }

    @Override
    public IExpression visitLikeExpression(@NotNull ExpressionParser.LikeExpressionContext ctx) {
        IExpression lhs = this.visit(ctx.expression(0));
        IExpression rhs = this.visit(ctx.expression(1));
        if (!(lhs instanceof IExpressionString) || !(rhs instanceof IExpressionString)) {
            throw new IllegalArgumentException("The 'like' function requires both operands to be strings");
        }
        return new LikeExpression((IExpressionString)lhs, (IExpressionString) rhs);
    }

    @Override
    public IExpression visitNotEqualsExpression(@NotNull ExpressionParser.NotEqualsExpressionContext ctx) {
        return createBinaryExpression(ExpressionParser.NotEquals, this.visit(ctx.expression(0)), this.visit(ctx.expression(1)));
    }

    @Override
    public IExpression visitAndExpression(@NotNull ExpressionParser.AndExpressionContext ctx) {
        return createBinaryExpression(ExpressionParser.And, this.visit(ctx.expression(0)), this.visit(ctx.expression(1)));
    }

    @Override
    public IExpression visitOrExpression(@NotNull ExpressionParser.OrExpressionContext ctx) {
        return createBinaryExpression(ExpressionParser.Or, this.visit(ctx.expression(0)), this.visit(ctx.expression(1)));
    }

    @Override
    public IExpression visitInExpression(@NotNull ExpressionParser.InExpressionContext ctx) {
        IExpression lhs = this.visit(ctx.expression(0));

        ExpressionParser.ExpressionListContext expressionList = ((ExpressionParser.ListExpressionContext) ctx.expression(1)).list().expressionList();
        int count = expressionList.expression().size();
        IExpression[] expressions = new IExpression[count];
        int i = 0;
        for (ExpressionParser.ExpressionContext expression : expressionList.expression()) {
            expressions[i++] = this.visit(expression);
        }

        switch(expressions[0].getType()) {
            case Int: {
                return new InExpression.Int((IExpressionInt)lhs, Arrays.copyOf(expressions, count, IExpressionInt[].class));
            }
            default: {
                throw new IllegalArgumentException("Cannot use 'in' operator for type " + expressions[0].getType());
            }
        }
    }

    @Override
    public IExpression visitUnaryMinusExpression(@NotNull ExpressionParser.UnaryMinusExpressionContext ctx) {
        return createUnaryExpression(ExpressionParser.Minus, this.visit(ctx.expression()));
    }

    @Override
    public IExpression visitNotExpression(@NotNull ExpressionParser.NotExpressionContext ctx) {
        IExpression operand = this.visit(ctx.expression());
        switch (operand.getType()) {
            case Bool: {
                return new NegateExpression.Bool((IExpressionBool)operand);
            }
            case NullableBool: {
                return new NegateExpression.NullableBool((IExpressionNullableBool)operand);
            }
            default: {
                throw new IllegalArgumentException("Negate cannot take an operand of type " + operand.getType());
            }
        }
    }

    @Override
    public IExpression visitFunctionCallExpression(@NotNull ExpressionParser.FunctionCallExpressionContext ctx) {
        ExpressionParser.FunctionCallContext functionCall = ctx.functionCall();
        String functionName = functionCall.Identifier().getText();
        IUserDefinedFunction function = functionRegistry.create(functionName);
        if (function == null) {
            throw new IllegalArgumentException("No function '" + functionName + "' exists");
        }

        List<? extends ExpressionParser.ExpressionContext> expressions = functionCall.expressionList().expression();
        IExpression[] parameters = new IExpression[expressions.size()];
        int i = 0;
        for (ExpressionParser.ExpressionContext expression : expressions) {
            parameters[i++] = this.visit(expression);
        }

        function.setParameters(parameters);

        if(function instanceof IHookingFunction){
            ((IHookingFunction) function).hook(hookingContext);
        }

        if (function instanceof IRetypeableUserDefinedFunction) {
            return ((IRetypeableUserDefinedFunction)function).retype();
        }

        return function;
    }

    @Override
    public IExpression visitColFunctionCallExpression(@NotNull ExpressionParser.ColFunctionCallExpressionContext ctx) {
        IExpression columnExpression = this.visit(ctx.colFunctionCall().expression());
        if (columnExpression.getType() != ColumnType.String) {
            throw new IllegalArgumentException("col() requires a string argument");
        }
        String columnName = ((IExpressionString) columnExpression).getString(-1);
        return getColumnExpression(columnName);
    }

    @Override
    public IExpression visitColumnExpression(@NotNull ExpressionParser.ColumnExpressionContext ctx) {
        String columnName = ctx.Identifier().getText();
        return getColumnExpression(columnName);
    }

    protected IExpression getColumnExpression(String columnName) {
        if (columnAliases != null) {
            String realName = columnAliases.get(columnName);
            if (realName != null) {
                columnName = realName;
            }
        }
        ColumnHolder columnHolder = schema.getColumnHolder(columnName);
        if (columnHolder == null) {
            throw new IllegalArgumentException("No such column '" + columnName + "'");
        }
        columnsUsed.set(columnHolder.getColumnId());

//        if (dimensionMapper != null) {
            ColumnMetadata metadata = columnHolder.getMetadata();
            if (metadata != null && metadata.isFlagged(ColumnFlags.DIMENSION)) {
                return new UnEnumExpression(columnHolder, metadata.getDimensionNameSpace(), metadata.getDimensionName(), metadata.getCardinality());
            }
//        }

        return (IExpression)columnHolder;
    }

    private IExpression createUnaryExpression(int operation, IExpression operand) {
        switch (operation) {
            case ExpressionParser.Minus: {
                switch (operand.getType()) {
                    case Byte: {
                        return new UnaryMinusExpression.Byte((IExpressionByte)operand);
                    }
                    case Short: {
                        return new UnaryMinusExpression.Short((IExpressionShort)operand);
                    }
                    case Int: {
                        return new UnaryMinusExpression.Int((IExpressionInt)operand);
                    }
                    case Long: {
                        return new UnaryMinusExpression.Long((IExpressionLong)operand);
                    }
                    case Float: {
                        return new UnaryMinusExpression.Float((IExpressionFloat)operand);
                    }
                    case Double: {
                        return new UnaryMinusExpression.Double((IExpressionDouble)operand);
                    }
                }
            }
            default: {
                throw new IllegalArgumentException("Unknown operation " + operation);
            }
        }
    }

    private IExpression createBinaryExpression(int operation, IExpression lhs, IExpression rhs) {
        ColumnType type = Cast.pickCommonType(lhs, rhs);
        lhs = Cast.wrapIfNecessary(lhs, type);
        rhs = Cast.wrapIfNecessary(rhs, type);
        switch (operation) {
            case ExpressionParser.Plus: {
                return PlusExpression.createPlus(type, lhs, rhs);
            }
            case ExpressionParser.Minus: {
                return MinusExpression.createMinus(type, lhs, rhs);
            }
            case ExpressionParser.Mult: {
                return MultiplyExpression.createMultiply(type, lhs, rhs);
            }
            case ExpressionParser.Div: {
                return DivideExpression.createDivide(type, lhs, rhs);
            }
            case ExpressionParser.Mod: {
                return ModExpression.createModulus(type, lhs, rhs);
            }
            case ExpressionParser.Pow: {
                return PowerExpression.createPower(type, lhs, rhs);
            }
            case ExpressionParser.GTEquals: {
                return GTEqualsExpression.createGTEquals(type, lhs, rhs);
            }
            case ExpressionParser.LTEquals: {
                return LTEqualsExpression.createLTEquals(type, lhs, rhs);
            }
            case ExpressionParser.GT: {
                return GTExpression.createGT(type, lhs, rhs);
            }
            case ExpressionParser.LT: {
                return LTExpression.createLT(type, lhs, rhs);
            }
            case ExpressionParser.Equals: {
                return EqualsExpression.createEquals(type, lhs, rhs);
            }
            case ExpressionParser.NotEquals: {
                return NotEqualsExpression.createNotEquals(type, lhs, rhs);
            }
            case ExpressionParser.And: {
                switch (lhs.getType()) {
                    case Bool: {
                        return new AndExpression.Bool((IExpressionBool)lhs, (IExpressionBool)rhs);
                    }
                    case NullableBool: {
                        return new AndExpression.NullableBool((IExpressionNullableBool)lhs, (IExpressionNullableBool)rhs);
                    }
                }
            }
            case ExpressionParser.Or: {
                switch (lhs.getType()) {
                    case Bool: {
                        return new OrExpression.Bool((IExpressionBool)lhs, (IExpressionBool)rhs);
                    }
                    case NullableBool: {
                        return new OrExpression.NullableBool((IExpressionNullableBool)lhs, (IExpressionNullableBool)rhs);
                    }
                }
            }
            default: {
                throw new IllegalArgumentException("Unknown operation " + operation);
            }
        }
    }

    private class UnEnumExpression implements IExpressionColumn, IExpressionBool, IExpressionNullableBool, IExpressionByte, IExpressionShort, IExpressionInt, IExpressionLong, IExpressionString {
        private final ColumnType type;
        private Cardinality cardinality;
        private ColumnHolder columnHolder;
        private String dimensionNamespace;
        private String dimensionName;

        public UnEnumExpression(ColumnHolder columnHolder, String dimensionNamespace, String  dimensionName, Cardinality cardinality) {
            this.columnHolder = columnHolder;
            this.dimensionNamespace = dimensionNamespace;
            this.dimensionName = dimensionName;
            this.type = columnHolder.getType();
            this.cardinality = cardinality;
        }

        @Override
        public String getName(){
            return this.columnHolder.getName();
        }

        @Override
        public boolean getBool(int row) {
            return dimensionMapper.lookupBool(dimensionNamespace, dimensionName, getLookupId(row));
        }

        @Override
        public byte getByte(int row) {
            return dimensionMapper.lookupByte(dimensionNamespace, dimensionName, getLookupId(row));
        }

        @Override
        public int getInt(int row) {
            return dimensionMapper.lookupInt(dimensionNamespace, dimensionName, getLookupId(row));
        }

        @Override
        public long getLong(int row) {
            return dimensionMapper.lookupLong(dimensionNamespace, dimensionName, getLookupId(row));
        }

        @Override
        public NullableBool getNullableBool(int row) {
            return dimensionMapper.lookupNullableBool(dimensionNamespace, dimensionName, getLookupId(row));
        }

        @Override
        public short getShort(int row) {
            return dimensionMapper.lookupShort(dimensionNamespace, dimensionName, getLookupId(row));
        }

        @Override
        public String getString(int row) {
            return dimensionMapper.lookupString(dimensionNamespace, dimensionName, getLookupId(row));
        }

        @Override
        public ColumnType getType() {
            return type;
        }

        private int getLookupId(int row) {
            int id = -1;
            ColumnMetadata metadata = columnHolder.getMetadata();
            switch (this.cardinality) {
                case Boolean: {
                    id = NullableBool.fromBoolean(((IColumnBool)columnHolder).getBool(row)).getNumericValue();
                    break;
                }
                case Byte: {
                    id = ((IColumnByte)columnHolder).getByte(row);
                    if (metadata != null) {
                        if (id == ((ColumnMetadataByte)metadata).getNullValue()) {
                            id = -1;
                        }
                    }
                    break;
                }
                case Short: {
                    id = ((IColumnShort)columnHolder).getShort(row);
                    if (metadata != null) {
                        if (id == ((ColumnMetadataShort)metadata).getNullValue()) {
                            id = -1;
                        }
                    }
                    break;
                }
                case Int: {
                    id = ((IColumnInt)columnHolder).getInt(row);
                    if (metadata != null) {
                        if (id == ((ColumnMetadataInt)metadata).getNullValue()) {
                            id = -1;
                        }
                    }
                    break;
                }
            }
            return id;
        }    }
}
