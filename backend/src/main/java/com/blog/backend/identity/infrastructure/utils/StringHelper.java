package com.blog.backend.identity.infrastructure.utils;

/**
 * Plain-text email bodies. mail-service only sends text/plain, so these must not contain HTML.
 */
public class StringHelper {

    public static String mailBodyForActivingAccount(String email, String token) {
        String link = "http://localhost:8080/activeAccount?token=" + token;
        return "Xin chào " + email + ",\n\n"
                + "Cảm ơn bạn đã đăng ký tài khoản. Chúng tôi rất vui mừng được chào đón bạn tham gia cùng chúng tôi.\n\n"
                + "Để hoàn tất đăng ký, vui lòng mở đường dẫn sau trong trình duyệt để xác thực địa chỉ email của bạn:\n"
                + link + "\n\n"
                + "*Lưu ý: Liên kết này sẽ hết hạn trong vòng 24 giờ.\n\n"
                + "Trân trọng,\n"
                + "Đội ngũ phát triển\n\n"
                + "--\n"
                + "Email này được gửi tự động, vui lòng không trả lời.";
    }

    public static String mailBodyForForgotPassword(String email, String otp) {
        return "Xin chào " + email + ",\n\n"
                + "Chúng tôi nhận được yêu cầu khôi phục mật khẩu cho tài khoản của bạn. "
                + "Vui lòng sử dụng mã xác thực (OTP) dưới đây để tiếp tục:\n\n"
                + "    " + otp + "\n\n"
                + "*Lưu ý: Mã xác thực này chỉ có hiệu lực trong vòng 2 phút. Tuyệt đối không chia sẻ mã này cho bất kỳ ai.\n\n"
                + "Nếu bạn không yêu cầu khôi phục mật khẩu, vui lòng bỏ qua email này. Tài khoản của bạn vẫn an toàn.\n\n"
                + "Trân trọng,\n"
                + "Đội ngũ phát triển\n\n"
                + "--\n"
                + "Email này được gửi tự động, vui lòng không trả lời.";
    }
}
