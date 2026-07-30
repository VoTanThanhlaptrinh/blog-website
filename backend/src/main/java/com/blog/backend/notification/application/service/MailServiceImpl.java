package com.blog.backend.notification.application.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {
    private final JavaMailSender mailSender;
    @Override
    public void sendMail(String to, String subject, String body) {
        try {
            // 1. Tạo MimeMessage để hỗ trợ định dạng phong phú (HTML, đính kèm file)
            MimeMessage message = mailSender.createMimeMessage();

            // 2. Sử dụng Helper để dễ dàng cấu hình MimeMessage (true = hỗ trợ multipart, "UTF-8" = chống lỗi font tiếng Việt)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);

            // 3. Truyền body và set cờ 'html = true' để Spring biên dịch mã HTML thay vì in ra text thô
            helper.setText(body, true);

            // 4. Thực hiện gửi mail
            mailSender.send(message);

            System.out.println("Đã gửi email HTML thành công tới: " + to);

        } catch (MessagingException e) {
            // 5. Bắt lỗi cấu hình hoặc mạng để luồng không bị sập (có thể dùng Logger ở đây)
            System.err.println("Lỗi khi gửi email tới " + to + ": " + e.getMessage());
            // Bạn có thể throw một Custom Exception ở đây nếu muốn hệ thống biết mail gửi thất bại
            throw new RuntimeException("Không thể gửi email xác thực.", e);
        }
    }
}
