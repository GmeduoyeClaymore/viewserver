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

package io.viewserver.schema.column.memorymapped;

import io.viewserver.schema.column.ColumnBase;
import io.viewserver.schema.column.IWritableColumn;
import sun.nio.ch.DirectBuffer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/**
 * Created by bemm on 27/10/15.
 */
public abstract class MemoryMappedColumnBase extends ColumnBase implements IWritableColumn {
    private File file;
    private int maxRows;
    private final int dataSize;
    private final FileChannel fileChannel;
    private final ByteBuffer byteBuffer;
    protected final long address;
    private FileChannel prevFileChannel;
    private ByteBuffer prevByteBuffer;
    protected long prevAddress;

    protected MemoryMappedColumnBase(File file, int maxRows, int dataSize) throws IOException {
        super(file.getName());
        this.file = file;
        this.maxRows = maxRows;
        this.dataSize = dataSize;
        file.getParentFile().mkdirs();
        fileChannel = new RandomAccessFile(file, "rw").getChannel();
        byteBuffer = map(fileChannel);
        address = ((DirectBuffer) byteBuffer).address();
    }

    @Override
    public boolean supportsPreviousValues() {
        return true;
    }

    @Override
    public void storePreviousValues() {
        if (prevFileChannel == null) {
            try {
                prevFileChannel = new RandomAccessFile(file.getPath() + ".prev", "rw").getChannel();
                prevByteBuffer = map(prevFileChannel);
                prevAddress = ((DirectBuffer)prevByteBuffer).address();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void resetAll() {

    }

    public void close() throws IOException {
//        fileChannel.truncate()
        fileChannel.close();
    }

    private ByteBuffer map(FileChannel fileChannel) throws IOException {
        int size = maxRows * dataSize;
        return fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, size).order(ByteOrder.nativeOrder());
    }
}
