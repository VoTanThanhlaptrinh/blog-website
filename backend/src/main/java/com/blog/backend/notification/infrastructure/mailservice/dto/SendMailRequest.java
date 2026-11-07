package com.blog.backend.notification.infrastructure.mailservice.dto;

/** Body of POST /mails/send: to must be a valid email, subject 1-200 chars, body 1-20000 chars (plain text). */
public record SendMailRequest(String to, String subject, String body) {
}
