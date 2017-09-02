package io.awacs.agent;

import java.util.Map;

/**
 *
 * Created by pixyonly on 02/09/2017.
 */
public interface Plugin {

    void init(Map<String, String> properties);

    void rock();

    void over();
}
