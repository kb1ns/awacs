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

package io.awacs.plugin.springmvc;

import com.alibaba.fastjson.JSON;

/**
 * Created by pixyonly on 7/25/16.
 */
class SpringmvcReport {

    private boolean successful;

    private String service;

    private String exception;

    private StackTraceElement[] stack;

    private String message;

    private long timestamp;

    private int elapsedTime;

    public boolean isSuccessful() {
        return successful;
    }

    public SpringmvcReport setSuccessful(boolean successful) {
        this.successful = successful;
        return this;
    }

    public String getService() {
        return service;
    }

    public SpringmvcReport setService(String service) {
        this.service = service;
        return this;
    }

    public String getException() {
        return exception;
    }

    public SpringmvcReport setException(String exception) {
        this.exception = exception;
        return this;
    }

    public StackTraceElement[] getStack() {
        return stack;
    }

    public SpringmvcReport setStack(StackTraceElement[] stack) {
        this.stack = stack;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public SpringmvcReport setMessage(String message) {
        this.message = message;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public SpringmvcReport setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public int getElapsedTime() {
        return elapsedTime;
    }

    public SpringmvcReport setElapsedTime(int elapsedTime) {
        this.elapsedTime = elapsedTime;
        return this;
    }

    @Override public String toString() {
        return JSON.toJSONString(this);
    }
}
