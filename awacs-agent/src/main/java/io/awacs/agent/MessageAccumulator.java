/**
 * Copyright 2016 AWACS Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.awacs.agent;

import com.google.common.collect.Lists;
import io.awacs.core.transport.Message;
import io.awacs.core.transport.ResponseHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Enable batch send
 * Created by pixyonly on 4/21/17.
 */
final class MessageAccumulator {

    private AtomicBoolean batchInProgress = new AtomicBoolean(false);

    private List<Message> batch;

    private HashedWheelTimer timer;

    private AtomicLong chunkBytes = new AtomicLong(0l);

    private ConcurrentMap<String, ResultTracker> trackers;

    private int maxBatchSize;

    private long maxChunkBytes;

    private long lingerMs;

    private long timeout;

    private NettyClient client;

    private ExecutorService executorService;
    
    private Timeout batchStandingBy;

    MessageAccumulator(NettyClient client,
                       int maxBatchSize,
                       long maxChunkBytes,
                       long lingerms,
                       long timeout) {
        this.client = client;
        this.batch = new ArrayList<>(maxBatchSize);
        this.maxBatchSize = maxBatchSize;
        this.maxChunkBytes = maxChunkBytes;
        this.lingerMs = lingerms;
        this.timeout = timeout;
        this.timer = new HashedWheelTimer();
        executorService = Executors.newSingleThreadExecutor();
    }

    void flush(final Collection<Message> ready, final byte[] buf) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                int offset = 0;
                for (Message m : ready) {
                    System.arraycopy(m.serialize(), 0, buf, offset, m.size());
                    offset += m.size();
                }
                client.send(buf);
            }
        });

    }

    synchronized void append(Message message, final ResponseHandler<? extends Message> callback) {
        if (!batchInProgress.get()) {
            batchInProgress.set(true);
            batchStandingBy = timer.newTimeout(new TimerTask() {
                @Override
                public void run(Timeout timeout) throws Exception {
                    synchronized(MessageAccumulator.this) {
                        final List<Message> ready = Lists.newArrayList();
                        ready.addAll(batch);
                        final byte[] buf = new byte[chunkBytes.intValue()];
                        flush(ready, buf);
                        batch.clear();
                        chunkBytes.set(0);
                        batchInProgress.set(false);
                    }
                }
            }, lingerMs, TimeUnit.MILLISECONDS);
        }
        batch.add(message);
        chunkBytes.addAndGet(message.size());
        if (batch.size() >= maxBatchSize || chunkBytes.get() >= maxChunkBytes) {
            final List<Message> ready = Lists.newArrayList();
            ready.addAll(batch);
            final byte[] buf = new byte[chunkBytes.intValue()];
            flush(ready, buf);
            batch.clear();
            chunkBytes.set(0);
            batchInProgress.set(false);
            batchStandingBy.cancel();
        }
    }

    //TODO
    void confirm(String rid) {
    }

    synchronized void close() {
        try {
            final List<Message> ready = Lists.newArrayList();
            ready.addAll(batch);
            final byte[] buf = new byte[chunkBytes.intValue()];
            flush(ready, buf);
            executorService.shutdown();
            executorService.awaitTermination(lingerMs + 1000, TimeUnit.MILLISECONDS);
            timer.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class ResultTracker {

    }

}
