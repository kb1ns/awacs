package io.awacs.agent.net;

import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by pixyonly on 04/09/2017.
 */
class Connection {

    private final static Logger log = Logger.getLogger("AWACS");

    Remote remote;

    AsynchronousSocketChannel channel;

    int timeout;

    Connection(Remote remote, int timeout) {
        this.remote = remote;
        this.timeout = timeout;
        ready();
    }

    boolean ready() {
        try {
            if (channel == null) {
                channel = AsynchronousSocketChannel.open();
            }
            Future<Void> future = channel.connect(remote.getAddress());
            if (future.get(timeout, TimeUnit.MILLISECONDS) != null) {
                log.log(Level.WARNING, "Cannot connect to remote {0}", remote);
                return false;
            }
            channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
            log.log(Level.INFO, "Connected to server {0}", remote);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    void flush(ByteBuffer batch, final Callback cb) {
        batch.flip();
        channel.write(batch, batch, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                if (!attachment.hasRemaining()) {
                    log.finest("Batch flushed.");
                    cb.onComplete();
                } else {
                    channel.write(attachment, attachment, this);
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                exc.printStackTrace();
                cb.onException(exc);
            }
        });
    }
}
