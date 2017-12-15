package io.awacs.component.mail;

import io.awacs.common.Configurable;
import io.awacs.common.Configuration;
import io.awacs.common.Releasable;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;

public class MailComponent implements Configurable, Releasable {

    private String username;

    private String password;

    private Session session;

    private Map<String, Group> interests;

    private MailQueue queue;

    private Timer timer;

    @Override
    public void init(Configuration configuration) {
        username = configuration.getString("username");
        password = configuration.getString("password");
        final Properties properties = new Properties();
        properties.putAll(configuration.getParameters());
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.starttls.enable", "true");
        properties.setProperty("mail.smtp.ssl.trust", configuration.getString("host"));
        properties.setProperty("mail.smtp.host", configuration.getString("host"));
        properties.setProperty("mail.smtp.port", configuration.getString("port"));
        session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
//        session.setDebug(true);
        interests = new HashMap<>();
        String[] namespaceses = configuration.getArray("interest_namespace");
        for (String namespace : namespaceses) {
            interests.put(namespace, new Group(configuration.getArray("interest_namespace." + namespace)));
        }
        queue = new MailQueue(50);
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                queue.clear();
            }
        }, 8 * 60 * 60 * 1000, 8 * 60 * 60 * 1000);
    }

    @Override
    public void release() {
        timer.cancel();
    }

    public void alert(String namespace, String content) {
        queue.checkOnFire(content, s -> {
            try {
                Group group = interests.get(namespace);
                MimeMessage mail = new MimeMessage(session);
                mail.setFrom(new InternetAddress(username));
                mail.setText(content);
                mail.setSentDate(new Date());
                mail.setSubject(String.format("Application exception of %s", namespace));
                mail.setRecipients(Message.RecipientType.TO, group.recipients);
                Transport.send(mail);
            } catch (Exception e) {
                //TODO
                e.printStackTrace();
            }
        });
    }

    private static class Group {

        private Address[] recipients;

        Group(String[] addr) {
            this.recipients = new Address[addr.length];
            for (int i = 0; i < addr.length; i++) {
                try {
                    this.recipients[i] = new InternetAddress(addr[i]);
                } catch (AddressException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
