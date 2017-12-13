package io.awacs.component.mail;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ConcurrentLinkedDeque;

public class MailQueueTest {

    private MailQueue queue;

    private ConcurrentLinkedDeque<String> record;

    @Before
    public void init() {
        queue = new MailQueue(5);
        record = new ConcurrentLinkedDeque<>();
    }

    @Test
    public void r() {
        queue.checkOnFire("hello", s -> record.add(s));
        queue.checkOnFire("hello", s -> record.add(s));
        queue.checkOnFire("hello", s -> record.add(s));
        queue.checkOnFire("hello", s -> record.add(s));
        Assert.assertEquals(1, record.size());
        Assert.assertEquals(1, queue.size());
        queue.checkOnFire("hello, world", s -> record.add(s));
        Assert.assertEquals(2, record.size());
        Assert.assertEquals(2, queue.size());
        queue.checkOnFire("hello, world", s -> record.add(s));
        Assert.assertEquals(2, record.size());
        Assert.assertEquals(2, queue.size());
        queue.checkOnFire("hello, world, world", s -> record.add(s));
        Assert.assertEquals(3, record.size());
        Assert.assertEquals(3, queue.size());
        queue.checkOnFire("hello, world, world, world", s -> record.add(s));
        Assert.assertEquals(4, record.size());
        Assert.assertEquals(4, queue.size());
        queue.checkOnFire("hello, world, world, world, world", s -> record.add(s));
        Assert.assertEquals(5, record.size());
        Assert.assertEquals(5, queue.size());
        queue.checkOnFire("h", s -> record.add(s));
        Assert.assertEquals(6, record.size());
        Assert.assertEquals(5, queue.size());
        queue.checkOnFire("hello", s -> record.add(s));
        Assert.assertEquals(7, record.size());
        Assert.assertEquals(5, queue.size());
        queue.expire();
        Assert.assertEquals(4, queue.size());
        queue.expire();
        Assert.assertEquals(3, queue.size());
        queue.expire();
        Assert.assertEquals(2, queue.size());
        queue.expire();
        Assert.assertEquals(1, queue.size());
        queue.expire();
        Assert.assertEquals(0, queue.size());
        queue.expire();
        Assert.assertEquals(0, queue.size());
    }
}
