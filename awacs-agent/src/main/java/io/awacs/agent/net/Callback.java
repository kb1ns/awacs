package io.awacs.agent.net;

/**
 * Created by pixyonly on 02/09/2017.
 */
public interface Callback {

    void onComplete();

    void onException(Throwable t);
}
