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

package io.awacs.repository;

import java.util.Collections;
import java.util.List;

/**
 * Created by pixyonly on 16/10/12.
 */
public final class MailForm {

    private List<String> to = Collections.emptyList();

    private List<String> cc = Collections.emptyList();

    private String subject;

    private String text;

    public List<String> getTo() {
        return to;
    }

    public MailForm setTo(List<String> to) {
        this.to = to;
        return this;
    }

    public List<String> getCc() {
        return cc;
    }

    public MailForm setCc(List<String> cc) {
        this.cc = cc;
        return this;
    }

    public String getSubject() {
        return subject;
    }

    public MailForm setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public String getText() {
        return text;
    }

    public MailForm setText(String text) {
        this.text = text;
        return this;
    }
}
