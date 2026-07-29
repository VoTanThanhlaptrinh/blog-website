package com.blog.backend.notification.application.service;

public interface MailService {
    void sendMail(String to, String subject, String body);

}
