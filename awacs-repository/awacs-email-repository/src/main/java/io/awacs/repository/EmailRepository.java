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

package io.awacs.repository;

import io.awacs.core.Configurable;
import io.awacs.core.Configuration;
import io.awacs.core.InitializationException;
import io.awacs.core.util.LoggerPlus;
import io.awacs.core.util.LoggerPlusFactory;
import io.awacs.core.util.ThreadPoolHelper;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;

/**
 * Created by pixyonly on 16/9/27.
 */
public class EmailRepository implements Configurable {

    private static final LoggerPlus logger = LoggerPlusFactory.getLogger(EmailRepository.class);

    private String username;

    private String password;

    private Session session;

    public void send(final MailForm mail) {
        ThreadPoolHelper.instance.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    MimeMessage msg = new MimeMessage(session);
                    msg.setFrom(new InternetAddress(username));
                    msg.setSentDate(new Date());
                    for (String to : mail.getTo())
                        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
                    for (String cc : mail.getCc())
                        msg.addRecipient(Message.RecipientType.CC, new InternetAddress(cc));
                    msg.setText(mail.getText());
                    Transport.send(msg);
                    logger.info("Email sent.");
                } catch (Exception e) {
                    logger.warn("Email send failed.", e);
                }
            }
        });
    }

    @Override
    public void init(Configuration configuration) throws InitializationException {
        username = configuration.getString("username");
        password = configuration.getString("password");
        final Properties properties = new Properties();
        properties.putAll(configuration.getParameters());
        properties.setProperty("mail.smtp.auth", "true");
        session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

}
