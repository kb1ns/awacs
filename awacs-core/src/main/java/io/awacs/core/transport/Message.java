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

package io.awacs.core.transport;

import io.awacs.core.util.RuntimeHelper;

/**
 * Created by pixyonly on 8/23/16.
 */
public interface Message {

    byte C_VERSION = 0x01;

    byte getVersion();

    int getPid();

    Key<?> getKey();

    int getTimestamp();

    byte[] body();

    byte[] serialize();

    int size();

    abstract class Builder<M extends Message> {

        protected Key<?> key;

        protected byte[] body;

        protected int timestamp = (int) (System.currentTimeMillis() / 1000);

        protected byte version = C_VERSION;

        protected int pid = RuntimeHelper.instance.getPid();

        public Builder<M> setKey(Key<?> key) {
            this.key = key;
            return this;
        }

        public Builder<M> setBody(byte[] body) {
            this.body = body;
            return this;
        }

        public Builder<M> setTimestamp(int timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder<M> setVersion(byte version) {
            this.version = version;
            return this;
        }

        public Builder<M> setPid(int pid) {
            this.pid = pid;
            return this;
        }

        public abstract M build();
    }
}
