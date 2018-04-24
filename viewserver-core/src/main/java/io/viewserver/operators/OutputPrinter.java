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

package io.viewserver.operators;

import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;

/**
 * Created by bemm on 27/10/2014.
 */
public class OutputPrinter {
    public static void printOutput(IOutput output) {
        printOutput(output, false);
    }

    public static void printOutput(IOutput output, boolean usePreviousValues) {
        System.out.println(getOutput(output, usePreviousValues));
    }

    public static void printRow(IOutput output, int rowId) {
        System.out.println(getRow(output, rowId));
    }

    //TODO -refactor to merge with code for printing output
    private static String getRow(IOutput output, int rowId){
        Schema schema = output.getSchema();
        int schemaWidth = schema.getWidth();
        schemaWidth++;
        int[] columnWidth = new int[schemaWidth];

        String[] header = buildHeader(schema, schemaWidth, columnWidth);
        String[][] data = new String[1][];

        data[0] = new String[schemaWidth];
        data[0][0] = Integer.toString(rowId);
        columnWidth[0] = Math.max(columnWidth[0], data[0][0].length() + 1);
        for (int j = 0, col = 1; col < schemaWidth; j++, col++) {
            ColumnHolder columnHolder = schema.getColumnHolder(j);
            if (columnHolder == null) {
                continue;
            }

            data[0][col] = ColumnHolderUtils.getValue(columnHolder, rowId).toString();
            if(columnHolder.getName().equals("client")){
                System.out.println("client: " + data[0][col]);
            }

            columnWidth[col] = Math.max(columnWidth[col], data[0][col].length() + 1);
        }

        return buildString(schemaWidth, columnWidth, header, data);
    }

    public static String getOutput(IOutput output, boolean previous) {
        Schema schema = output.getSchema();
        int schemaWidth = schema.getWidth();
        schemaWidth++;
        int[] columnWidth = new int[schemaWidth];

        String[] header = buildHeader(schema, schemaWidth, columnWidth);

        IRowSequence allRows = output.getAllRows();
        String[][] data = new String[output.getRowCount()][];
        int i = 0;
        while (allRows.moveNext()) {
            int currentRowId = allRows.getRowId();

            data[i] = new String[schemaWidth];
            data[i][0] = Integer.toString(currentRowId);
            columnWidth[0] = Math.max(columnWidth[0], data[i][0].length() + 1);
            for (int j = 0, col = 1; col < schemaWidth; j++, col++) {
                ColumnHolder columnHolder = schema.getColumnHolder(j);
                if (columnHolder == null) {
                    continue;
                }
                Object value = (previous && columnHolder.supportsPreviousValues()) ? ColumnHolderUtils.getPreviousValue(columnHolder, currentRowId) : ColumnHolderUtils.getValue(columnHolder, currentRowId);
                data[i][col] = value != null ? value.toString() : null;


                columnWidth[col] = Math.max(columnWidth[col], data[i][col] != null ? data[i][col].length() + 1 : 1);
            }
            i++;
        }

        return buildString(schemaWidth, columnWidth, header, data);
    }

    private static String buildString(int schemaWidth, int[] columnWidth, String[] header, String[][] data) {
        int i;
        StringBuilder lineBuilder = new StringBuilder();
        for (i = 0; i < schemaWidth; i++) {
            lineBuilder.append('+');
            for (int j = 0; j <= columnWidth[i] + 1; j++) {
                lineBuilder.append('-');
            }
        }
        lineBuilder.append("+\n");

        StringBuilder builder = new StringBuilder();

        builder.append(lineBuilder);

        for (i = 0; i < schemaWidth; i++) {
            builder.append("| ").append(String.format("%-" + (columnWidth[i] + 1) + "s", header[i]));
        }
        builder.append("|\n");

        builder.append(lineBuilder);

        for (i = 0; i < data.length; i++) {
            for (int j = 0; j < schemaWidth; j++) {
                try {
                    builder.append("| ").append(String.format("%-" + (columnWidth[j] + 1) + "s", data[i][j]));
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            builder.append("|\n");
        }

        builder.append(lineBuilder);

        return builder.toString();
    }

    private static String[] buildHeader(Schema schema, int schemaWidth, int[] columnWidth) {
        String[] header = new String[schemaWidth];
        header[0] = "Row";
        for (int i = 0, col = 1; col < schemaWidth; i++, col++) {
            ColumnHolder columnHolder = schema.getColumnHolder(i);
            if (columnHolder == null) {
                continue;
            }
            String name = columnHolder.getName();
            header[col] = name;
            columnWidth[col] = name.length();
        }
        return header;
    }
}
