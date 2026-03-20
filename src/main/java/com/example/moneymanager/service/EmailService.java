package com.example.moneymanager.service;


import com.google.api.services.gmail.Gmail;

import com.google.api.services.gmail.model.Message;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Properties;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final Gmail gmailClient;

    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage mimeMessage = createMimeMessage(to, subject, body);
            Message message = encodeMessage(mimeMessage);
            gmailClient.users().messages().send("me", message).execute();
            log.info("Sent email via Gmail API to {}", to);
        } catch (MessagingException | IOException e) {
            log.error("Failed to send email to {}", to, e);
            throw new RuntimeException("Failed to send email to " + to, e);
        }
    }

    private MimeMessage createMimeMessage(String to, String subject, String body)
            throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress("me"));
        email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject, "UTF-8");
        email.setContent(body, "text/html; charset=UTF-8");
        return email;
    }

    private Message encodeMessage(MimeMessage mimeMessage)
            throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        mimeMessage.writeTo(buffer);
        String encodedEmail = Base64.getUrlEncoder().encodeToString(buffer.toByteArray());
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }
}
