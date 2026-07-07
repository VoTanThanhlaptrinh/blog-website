package com.blog.be.notification.application;

public interface MailService {
    void sendMail(String to, String subject, String body);

}
