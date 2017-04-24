/**
 * Copyright 2016 AWACS Project.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.awacs.agent;

import io.awacs.core.NoSuchKeyTypeException;
import io.awacs.core.transport.Key;
import io.awacs.protocol.binary.BinaryMessage;
import io.awacs.protocol.binary.ByteKey;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by pixyonly on 4/21/17.
 */
public class NettyClientTest {

    @Test
    public void testWithNoBatch() {
        List<InetSocketAddress> addresses = new LinkedList<>();
        addresses.add(new InetSocketAddress("127.0.0.1", 7100));
        NettyClient nettyClient = new NettyClient.Builder().setAddresses(addresses).build();
        Key<?> key = null;
        try {
            key = ByteKey.getKey("io.awacs.protocol.binary.ByteKey", "1");
        } catch (NoSuchKeyTypeException e) {
            e.printStackTrace();
        }
        nettyClient.send(new BinaryMessage.BinaryMessageBuilder()
                .setKey(key)
                .setVersion(BinaryMessage.C_VERSION)
                .setBody("{\"hello\":\"world.\"}".getBytes())
                .build().serialize());
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testWithBatch() {
        List<InetSocketAddress> addresses = new LinkedList<>();
        addresses.add(new InetSocketAddress("127.0.0.1", 7100));
        NettyClient nettyClient = new NettyClient.Builder().setAddresses(addresses).build();
        Key<?> key = null;
        try {
            key = ByteKey.getKey("io.awacs.protocol.binary.ByteKey", "1");
        } catch (NoSuchKeyTypeException e) {
            e.printStackTrace();
        }
        MessageHub.instance.register(nettyClient);
        int count = 99;
        final CountDownLatch countDownLatch = new CountDownLatch(count);
        long t = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            final Key<?> finalKey = key;
            new Thread(new Runnable() {
                public void run() {
                    MessageHub.instance.publish(new BinaryMessage.BinaryMessageBuilder()
                            .setKey(finalKey)
                            .setVersion(BinaryMessage.C_VERSION)
                            .setBody("{\"hello\":\"world.\"}".getBytes())
                            .build());
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
            System.out.println(System.currentTimeMillis() - t);
            for (int i = 0; i < count; i++) {
                final Key<?> finalKey = key;
                new Thread(new Runnable() {
                    public void run() {
                        MessageHub.instance.publish(new BinaryMessage.BinaryMessageBuilder()
                                .setKey(finalKey)
                                .setVersion(BinaryMessage.C_VERSION)
                                .setBody("{\"hello\":\"world.\"}".getBytes())
                                .build());
                        countDownLatch.countDown();
                    }
                }).start();
            }
            System.out.println(System.currentTimeMillis() - t);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
