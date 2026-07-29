package com.blog.backend.identity.infrastructure.utils;

public class StringHelper {
    public static String mailBodyForActivingAccount() {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Xác thực tài khoản</title>\n" +
                "    <style>\n" +
                "        body {\n" +
                "            font-family: Arial, sans-serif;\n" +
                "            background-color: #f4f4f4;\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "        }\n" +
                "        .container {\n" +
                "            max-width: 600px;\n" +
                "            margin: 20px auto;\n" +
                "            background-color: #ffffff;\n" +
                "            border-radius: 8px;\n" +
                "            box-shadow: 0 4px 8px rgba(0,0,0,0.1);\n" +
                "            overflow: hidden;\n" +
                "        }\n" +
                "        .header {\n" +
                "            background-color: #007bff;\n" +
                "            color: #ffffff;\n" +
                "            text-align: center;\n" +
                "            padding: 20px;\n" +
                "        }\n" +
                "        .content {\n" +
                "            padding: 30px;\n" +
                "            color: #333333;\n" +
                "            line-height: 1.6;\n" +
                "        }\n" +
                "        .btn {\n" +
                "            display: inline-block;\n" +
                "            background-color: #28a745;\n" +
                "            color: #ffffff;\n" +
                "            text-decoration: none;\n" +
                "            padding: 12px 25px;\n" +
                "            border-radius: 5px;\n" +
                "            font-weight: bold;\n" +
                "            margin: 20px 0;\n" +
                "        }\n" +
                "        .footer {\n" +
                "            background-color: #f4f4f4;\n" +
                "            color: #777777;\n" +
                "            text-align: center;\n" +
                "            padding: 15px;\n" +
                "            font-size: 12px;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">\n" +
                "            <h2>Chào mừng bạn đến với Hệ thống!</h2>\n" +
                "        </div>\n" +
                "        <div class=\"content\">\n" +
                "            <p>Xin chào <strong>{{email}}</strong>,</p>\n" +
                "            <p>Cảm ơn bạn đã đăng ký tài khoản. Chúng tôi rất vui mừng được chào đón bạn tham gia cùng chúng tôi.</p>\n" +
                "            <p>Để hoàn tất quá trình đăng ký và bắt đầu sử dụng dịch vụ, vui lòng nhấn vào nút bên dưới để xác thực địa chỉ email của bạn:</p>\n" +
                "            \n" +
                "            <div style=\"text-align: center;\">\n" +
                "                <a href=\"http://localhost:8080/activeAccount?token={{token}}\" class=\"btn\" style=\"color: white;\">Kích hoạt tài khoản</a>\n" +
                "            </div>\n" +
                "            \n" +
                "            <p><em>*Lưu ý: Liên kết này sẽ hết hạn trong vòng 24 giờ.</em></p>\n" +
                "            <p>Nếu nút bấm không hoạt động, bạn có thể copy và dán đường dẫn sau vào trình duyệt:</p>\n" +
                "            <p style=\"word-break: break-all; color: #007bff;\">http://localhost:8080/activeAccount?token={{token}}</p>\n" +
                "            \n" +
                "            <p>Trân trọng,<br><strong>Đội ngũ phát triển</strong></p>\n" +
                "        </div>\n" +
                "        <div class=\"footer\">\n" +
                "            <p>Email này được gửi tự động, vui lòng không trả lời.</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    public static String mailBodyForForgotPassword(String email, String otp) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Khôi phục mật khẩu</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }\n" +
                "        .container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); overflow: hidden; }\n" +
                "        .header { background-color: #dc3545; color: #ffffff; text-align: center; padding: 20px; }\n" +
                "        .content { padding: 30px; color: #333333; line-height: 1.6; }\n" +
                "        .otp-box { display: inline-block; background-color: #f8f9fa; border: 2px dashed #dc3545; color: #dc3545; font-size: 24px; font-weight: bold; padding: 15px 30px; letter-spacing: 5px; margin: 20px 0; border-radius: 5px; }\n" +
                "        .footer { background-color: #f4f4f4; color: #777777; text-align: center; padding: 15px; font-size: 12px; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">\n" +
                "            <h2>Yêu cầu khôi phục mật khẩu</h2>\n" +
                "        </div>\n" +
                "        <div class=\"content\">\n" +
                "            <p>Xin chào <strong>" + email + "</strong>,</p>\n" +
                "            <p>Chúng tôi nhận được yêu cầu khôi phục mật khẩu cho tài khoản của bạn. Vui lòng sử dụng mã xác thực (OTP) dưới đây để tiếp tục:</p>\n" +
                "            <div style=\"text-align: center;\">\n" +
                "                <div class=\"otp-box\">" + otp + "</div>\n" +
                "            </div>\n" +
                "            <p><em>*Lưu ý: Mã xác thực này chỉ có hiệu lực trong vòng <strong>2 phút</strong>. Tuyệt đối không chia sẻ mã này cho bất kỳ ai.</em></p>\n" +
                "            <p>Nếu bạn không yêu cầu khôi phục mật khẩu, vui lòng bỏ qua email này. Tài khoản của bạn vẫn an toàn.</p>\n" +
                "            <p>Trân trọng,<br><strong>Đội ngũ phát triển</strong></p>\n" +
                "        </div>\n" +
                "        <div class=\"footer\">\n" +
                "            <p>Email này được gửi tự động, vui lòng không trả lời.</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
}
