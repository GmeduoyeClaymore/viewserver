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

package io.viewserver.messages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 * Created by nick on 02/12/15.
 */
public abstract class PoolableMessage<T> implements IPoolableMessage<T> {
    private static final Logger log = LoggerFactory.getLogger(PoolableMessage.class);
    private IMessagePool owner;
    private int retainCount;
    private Class clazz;

    protected PoolableMessage(Class clazz) {
        this.clazz = clazz;
    }

    public void setOwner(IMessagePool owner) {
        this.owner = owner;
    }

    @Override
    public T retain() {
        retainCount++;
        return (T) this;
    }

    @Override
    public final T release() {
        if (log.isTraceEnabled()) {
            final StringBuilder builder = new StringBuilder();
            final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
            final ThreadInfo threadInfo = threadMXBean.getThreadInfo(Thread.currentThread().getId(), Integer.MAX_VALUE);
            final StackTraceElement[] stackTraceElements = threadInfo.getStackTrace();
            int stackTraceSize = stackTraceElements.length;
            for (int j = 0; j < stackTraceSize; j++) {
                builder.append("        at ").append(stackTraceElements[j]).append(System.lineSeparator());
            }
            //log.trace("Releasing poolable message of type {}\n{}", getClass().getName(), builder.toString());
        }
        if (retainCount == 0) {
            throw new RuntimeException(String.format("Poolable message of type %s over-released!", getClass().getName()));
        }
        if (--retainCount == 0) {
            doRelease();
            owner.release(clazz, this);
        }
        return (T)this;
    }

    protected abstract void doRelease();
}
