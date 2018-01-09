package com.shotgun.viewserver;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import io.viewserver.command.ControllerContext;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.IOutput;
import io.viewserver.operators.table.ITable;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;

import java.util.UUID;

public class ControllerUtils{
    public static ITable getTable(String tableName){
        IOperator table = ControllerContext.Current().getPeerSession().getSystemCatalog().getOperator(tableName);
        if (!(table instanceof ITable)) {
            throw new RuntimeException("Operator '" + tableName + "' is not a table");
        }

        return (ITable)table;
    }

    public static KeyedTable getKeyedTable(String tableName){
        IOperator table = ControllerContext.Current().getPeerSession().getSystemCatalog().getOperator(tableName);
        if (!(table instanceof KeyedTable)) {
            throw new RuntimeException("Operator '" + tableName + "' is not a keyed table");
        }

        return (KeyedTable)table;
    }

    public static Object getColumnValue(ITable table, String column, int row){
        IOutput output = table.getOutput();
        Schema schema = output.getSchema();
        ColumnHolder col = schema.getColumnHolder(column);
        return ColumnHolderUtils.getValue(col, row);
    }

    public static String generateGuid(){
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public static String encryptPassword(String plainText){
        // Create instance
        Argon2 argon2 = Argon2Factory.create();
        char[] charStr = plainText.toCharArray();

        try {
            int N = 65536;
            int r = 2;
            int p = 1;
            // Hash password
            return argon2.hash(r, N, p, charStr);
        } finally {
            argon2.wipeArray(charStr);
        }
    }

    public static boolean validatePassword(String plainText, String hashedText) {
        // Create instance
        Argon2 argon2 = Argon2Factory.create();
        char[] charStr = plainText.toCharArray();

        try {
            int N = 65536;
            int r = 2;
            int p = 1;

            return argon2.verify(hashedText, charStr);
        } finally {
            argon2.wipeArray(charStr);
        }
    }
}
