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

package io.viewserver.parser;

import io.viewserver.expression.function.FunctionRegistry;
import io.viewserver.expression.parser.ColumnAliasingVisitor;
import io.viewserver.expression.parser.ExpressionVisitorImpl;
import io.viewserver.expression.tree.*;
import io.viewserver.expressions.ExpressionLexer;
import io.viewserver.expressions.ExpressionParser;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.*;
import io.viewserver.schema.column.chunked.ChunkedColumnInt;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

/**
 * Created by bemm on 14/10/2014.
 */
public class ExpressionParserTest {
    private FunctionRegistry functionRegistry;
    private Schema schema;

    @Before
    public void before() {
        functionRegistry = new FunctionRegistry();
        schema = new Schema();
    }

    @Test
    public void testLiterals() {
        IExpression expression = getExpression("\"qwerty\"");
        Assert.assertTrue(expression.getType() == ColumnType.String);
        Assert.assertEquals("qwerty", ((IExpressionString)expression).getString(0));

        expression = getExpression("1.0f");
        Assert.assertTrue(expression.getType() == ColumnType.Float);
        Assert.assertEquals(1, ((IExpressionFloat)expression).getFloat(0), 0);

        expression = getExpression("1.0F");
        Assert.assertTrue(expression.getType() == ColumnType.Float);
        Assert.assertEquals(1, ((IExpressionFloat)expression).getFloat(0), 0);

        expression = getExpression("1.0d");
        Assert.assertTrue(expression.getType() == ColumnType.Double);
        Assert.assertEquals(1, ((IExpressionDouble)expression).getDouble(0), 0);

        expression = getExpression("1.0D");
        Assert.assertTrue(expression.getType() == ColumnType.Double);
        Assert.assertEquals(1, ((IExpressionDouble)expression).getDouble(0), 0);

        expression = getExpression("1.2");
        Assert.assertTrue(expression.getType() == ColumnType.Double);
        Assert.assertEquals(1.2, ((IExpressionDouble)expression).getDouble(0), 0);

        expression = getExpression("1b");
        Assert.assertTrue(expression.getType() == ColumnType.Byte);
        Assert.assertEquals(1, ((IExpressionByte)expression).getByte(0));

        expression = getExpression("1B");
        Assert.assertTrue(expression.getType() == ColumnType.Byte);
        Assert.assertEquals(1, ((IExpressionByte)expression).getByte(0));

        expression = getExpression("1h");
        Assert.assertTrue(expression.getType() == ColumnType.Short);
        Assert.assertEquals(1, ((IExpressionShort)expression).getShort(0));

        expression = getExpression("1H");
        Assert.assertTrue(expression.getType() == ColumnType.Short);
        Assert.assertEquals(1, ((IExpressionShort)expression).getShort(0));

        expression = getExpression("1l");
        Assert.assertTrue(expression.getType() == ColumnType.Long);
        Assert.assertEquals(1, ((IExpressionLong)expression).getLong(0));

        expression = getExpression("1L");
        Assert.assertTrue(expression.getType() == ColumnType.Long);
        Assert.assertEquals(1, ((IExpressionLong)expression).getLong(0));

        expression = getExpression("1i");
        Assert.assertTrue(expression.getType() == ColumnType.Int);
        Assert.assertEquals(1, ((IExpressionInt)expression).getInt(0));

        expression = getExpression("1I");
        Assert.assertTrue(expression.getType() == ColumnType.Int);
        Assert.assertEquals(1, ((IExpressionInt)expression).getInt(0));

        expression = getExpression("1");
        Assert.assertTrue(expression.getType() == ColumnType.Int);
        Assert.assertEquals(1, ((IExpressionInt)expression).getInt(0));

        expression = getExpression("true");
        Assert.assertTrue(expression.getType() == ColumnType.Bool);
        Assert.assertTrue(((IExpressionBool) expression).getBool(0));

        expression = getExpression("false");
        Assert.assertTrue(expression.getType() == ColumnType.Bool);
        Assert.assertFalse(((IExpressionBool) expression).getBool(0));

        expression = getExpression("null");
        Assert.assertNull(((IExpressionString) expression).getString(0));
    }

    @Test
    public void testCast() {
        IExpression expression = getExpression("(byte)0");
        Assert.assertTrue(expression.getType() == ColumnType.Byte);
        Assert.assertEquals(0, ((IExpressionByte)expression).getByte(0));

        expression = getExpression("(Byte)0");
        Assert.assertTrue(expression.getType() == ColumnType.Byte);
        Assert.assertEquals(0, ((IExpressionByte)expression).getByte(0));

        expression = getExpression("(short)0");
        Assert.assertTrue(expression.getType() == ColumnType.Short);
        Assert.assertEquals(0, ((IExpressionShort)expression).getShort(0));

        expression = getExpression("(Short)0");
        Assert.assertTrue(expression.getType() == ColumnType.Short);
        Assert.assertEquals(0, ((IExpressionShort)expression).getShort(0));

        expression = getExpression("(int)0");
        Assert.assertTrue(expression.getType() == ColumnType.Int);
        Assert.assertEquals(0, ((IExpressionInt)expression).getInt(0));

        expression = getExpression("(Int)0");
        Assert.assertTrue(expression.getType() == ColumnType.Int);
        Assert.assertEquals(0, ((IExpressionInt)expression).getInt(0));

        expression = getExpression("(long)0");
        Assert.assertTrue(expression.getType() == ColumnType.Long);
        Assert.assertEquals(0, ((IExpressionLong)expression).getLong(0));

        expression = getExpression("(Long)0");
        Assert.assertTrue(expression.getType() == ColumnType.Long);
        Assert.assertEquals(0, ((IExpressionLong)expression).getLong(0));

        expression = getExpression("(float)0");
        Assert.assertTrue(expression.getType() == ColumnType.Float);
        Assert.assertEquals(0, ((IExpressionFloat)expression).getFloat(0), 0);

        expression = getExpression("(Float)0");
        Assert.assertTrue(expression.getType() == ColumnType.Float);
        Assert.assertEquals(0, ((IExpressionFloat)expression).getFloat(0), 0);

        expression = getExpression("(double)0");
        Assert.assertTrue(expression.getType() == ColumnType.Double);
        Assert.assertEquals(0, ((IExpressionDouble)expression).getDouble(0), 0);

        expression = getExpression("(Double)0");
        Assert.assertTrue(expression.getType() == ColumnType.Double);
        Assert.assertEquals(0, ((IExpressionDouble)expression).getDouble(0), 0);
    }

    @Test
    public void testColumn() {
        ColumnHolder columnHolder = ColumnHolderUtils.createColumnHolder("test", ColumnType.Int);
        columnHolder.setColumn(new ChunkedColumnInt(columnHolder, new IColumnWatcher() {
            @Override
            public void markDirty(int rowId, int columnId) {

            }

            @Override
            public boolean isDirty(int rowId, int columnId) {
                return false;
            }

            @Override
            public void markColumnDirty(int columnId) {

            }
        }, null, 1024, 1024));
        schema.addColumn(columnHolder);

        ((IWritableColumnInt)columnHolder.getColumn()).setInt(0, 123);

        IExpressionInt expression = (IExpressionInt) getExpression("test");
        Assert.assertEquals(123, expression.getInt(0));
    }

    @Test
    public void testColumnFunction() {
        ColumnHolder columnHolder = ColumnHolderUtils.createColumnHolder("test", ColumnType.Int);
        columnHolder.setColumn(new ChunkedColumnInt(columnHolder, new IColumnWatcher() {
            @Override
            public void markDirty(int rowId, int columnId) {

            }

            @Override
            public boolean isDirty(int rowId, int columnId) {
                return false;
            }

            @Override
            public void markColumnDirty(int columnId) {

            }
        }, null, 1024, 1024));
        schema.addColumn(columnHolder);

        ((IWritableColumnInt)columnHolder.getColumn()).setInt(0, 123);

        IExpressionInt expression = (IExpressionInt) getExpression("col(\"te\" + \"st\")");
        Assert.assertEquals(123, expression.getInt(0));
    }

    @Test
    public void testUnaryMinus() {
        IExpressionInt expression = (IExpressionInt) getExpression("-1");
        Assert.assertEquals(-1, expression.getInt(0));
    }

    @Test
    public void testNegate() {
        IExpressionBool expression = (IExpressionBool) getExpression("!false");
        Assert.assertTrue(expression.getBool(0));
        expression = (IExpressionBool) getExpression("!true");
        Assert.assertFalse(expression.getBool(0));
    }

    @Test
    public void testAdd() {
        IExpressionInt expr = (IExpressionInt) getExpression("1+2");
        Assert.assertEquals(3, expr.getInt(0));
    }

    @Test
    public void testSubtract() {
        IExpressionInt expr = (IExpressionInt) getExpression("1-2");
        Assert.assertEquals(-1, expr.getInt(0));
    }

    @Test
    public void testMultiply() {
        IExpressionInt expr = (IExpressionInt) getExpression("3*4");
        Assert.assertEquals(12, expr.getInt(0));
    }

    @Test
    public void testDivide() {
        IExpressionInt expr = (IExpressionInt) getExpression("8/2");
        Assert.assertEquals(4, expr.getInt(0));
    }

    @Test
    public void testModulus() {
        IExpressionInt expr = (IExpressionInt) getExpression("17%3");
        Assert.assertEquals(2, expr.getInt(0));
    }

    @Test
    public void testPower() {
        IExpressionInt expr = (IExpressionInt) getExpression("2^4");
        Assert.assertEquals(16, expr.getInt(0));
    }

    @Test
    public void testGreaterThanOrEquals() {
        IExpressionBool expr = (IExpressionBool) getExpression("1>=2");
        Assert.assertEquals(false, expr.getBool(0));
        expr = (IExpressionBool) getExpression("2>=2");
        Assert.assertEquals(true, expr.getBool(0));
        expr = (IExpressionBool) getExpression("3>=2");
        Assert.assertEquals(true, expr.getBool(0));
    }

    @Test
    public void testLessThanOrEquals() {
        IExpressionBool expr = (IExpressionBool) getExpression("1<=2");
        Assert.assertEquals(true, expr.getBool(0));
        expr = (IExpressionBool) getExpression("2<=2");
        Assert.assertEquals(true, expr.getBool(0));
        expr = (IExpressionBool) getExpression("3<=2");
        Assert.assertEquals(false, expr.getBool(0));
    }

    @Test
    public void testGreaterThan() {
        IExpressionBool expr = (IExpressionBool) getExpression("1>2");
        Assert.assertEquals(false, expr.getBool(0));
        expr = (IExpressionBool) getExpression("2>2");
        Assert.assertEquals(false, expr.getBool(0));
        expr = (IExpressionBool) getExpression("3>2");
        Assert.assertEquals(true, expr.getBool(0));
    }

    @Test
    public void testLessThan() {
        IExpressionBool expr = (IExpressionBool) getExpression("1<2");
        Assert.assertEquals(true, expr.getBool(0));
        expr = (IExpressionBool) getExpression("2<2");
        Assert.assertEquals(false, expr.getBool(0));
        expr = (IExpressionBool) getExpression("3<2");
        Assert.assertEquals(false, expr.getBool(0));
    }

    @Test
    public void testEquals() {
        IExpressionBool expr = (IExpressionBool) getExpression("1=2");
        Assert.assertEquals(false, expr.getBool(0));
        expr = (IExpressionBool) getExpression("2=2");
        Assert.assertEquals(true, expr.getBool(0));
        expr = (IExpressionBool) getExpression("1==2");
        Assert.assertEquals(false, expr.getBool(0));
        expr = (IExpressionBool) getExpression("2==2");
        Assert.assertEquals(true, expr.getBool(0));
    }

    @Test
    public void testNotEquals() {
        IExpressionBool expr = (IExpressionBool) getExpression("1!=2");
        Assert.assertEquals(true, expr.getBool(0));
        expr = (IExpressionBool) getExpression("2!=2");
        Assert.assertEquals(false, expr.getBool(0));
    }

    @Test
    public void testAnd() {
        IExpressionBool expr = (IExpressionBool) getExpression("false&&false");
        Assert.assertEquals(false, expr.getBool(0));
        expr = (IExpressionBool) getExpression("true&&false");
        Assert.assertEquals(false, expr.getBool(0));
        expr = (IExpressionBool) getExpression("false&&true");
        Assert.assertEquals(false, expr.getBool(0));
        expr = (IExpressionBool) getExpression("true&&true");
        Assert.assertEquals(true, expr.getBool(0));
        expr = (IExpressionBool) getExpression("true&&1>0");
        Assert.assertEquals(true, expr.getBool(0));
    }

    @Test
    public void testOr() {
        IExpressionBool expr = (IExpressionBool) getExpression("false||false");
        Assert.assertEquals(false, expr.getBool(0));
        expr = (IExpressionBool) getExpression("true||false");
        Assert.assertEquals(true, expr.getBool(0));
        expr = (IExpressionBool) getExpression("false||true");
        Assert.assertEquals(true, expr.getBool(0));
        expr = (IExpressionBool) getExpression("true||true");
        Assert.assertEquals(true, expr.getBool(0));
    }

    @Test
    public void testIn() {
        IExpressionBool expr = (IExpressionBool) getExpression("0 in [1,2,3]");
        Assert.assertFalse(expr.getBool(0));
        expr = (IExpressionBool) getExpression("2 in [1,2,3]");
        Assert.assertTrue(expr.getBool(0));
    }

    @Test
    public void testNull() {
        IExpressionBool expr = (IExpressionBool) getExpression("null == null");
        Assert.assertTrue(expr.getBool(0));
    }

    @Test
    public void testIf() {
        IExpressionInt expr = (IExpressionInt) getExpression("if(true,1,2)");
        Assert.assertEquals(1, expr.getInt(0));
        expr = (IExpressionInt) getExpression("if(false,1,2)");
        Assert.assertEquals(2, expr.getInt(0));
    }


    private IExpression getExpression(String text) {
        ExpressionLexer lexer = new ExpressionLexer(new ANTLRInputStream(text));
        ExpressionParser parser = new ExpressionParser(new CommonTokenStream(lexer));
        ExpressionParser.ParseContext parse = parser.parse();
        ExpressionVisitorImpl visitor = new ExpressionVisitorImpl(schema, functionRegistry, null, null, null);
        return visitor.visit(parse);
    }

    @Test
    public void testColumnAliasingVisitor() throws Exception {
        HashMap<String, String> aliases = new HashMap<String, String>();
        aliases.put("alias", "name");

        ExpressionLexer lexer = new ExpressionLexer(new ANTLRInputStream("1+alias"));
        ExpressionParser parser = new ExpressionParser(new CommonTokenStream(lexer));
        ExpressionParser.ParseContext parse = parser.parse();
        ColumnAliasingVisitor visitor = new ColumnAliasingVisitor(aliases);
        Assert.assertEquals("1+name", visitor.visit(parse));
    }
}
