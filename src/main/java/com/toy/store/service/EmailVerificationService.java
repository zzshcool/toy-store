package com.toy.store.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpSession;
import java.util.Random;

@Service
public class EmailVerificationService {

    private final Random random = new Random();
    private final JavaMailSender mailSender;

    public EmailVerificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * 生成 6 位數驗證碼並發送真實 Email，隨後存入 Session
     */
    public String generateAndSaveCode(String email, HttpSession session) {
        String code = String.format("%06d", random.nextInt(1000000));
        session.setAttribute("email_verify_code_" + email, code);
        session.setAttribute("email_verify_time_" + email, System.currentTimeMillis());

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("toysoul5566@gmail.com");
            message.setTo(email);
            message.setSubject("ToySoul 玩具靈魂 - 會員註冊驗證碼");
            message.setText("您好，感謝您註冊 ToySoul！\n\n您的註冊驗證碼為：[" + code + "]\n\n此驗證碼於 5 分鐘內有效，請儘速完成驗證。\n\n祝您購物愉快！");

            mailSender.send(message);
            System.out.println("SMTP: 已向 " + email + " 發送驗證碼 " + code);
        } catch (Exception e) {
            System.err.println("SMTP 發送失敗: " + e.getMessage());
            System.out.println("DEBUG (發送失敗): 驗證碼為 " + code);
        }

        return code;
    }

    /**
     * 校驗驗證碼是否正確且未過期 (5 分鐘內有效)
     */
    public boolean verifyCode(String email, String code, HttpSession session) {
        String savedCode = (String) session.getAttribute("email_verify_code_" + email);
        Long savedTime = (Long) session.getAttribute("email_verify_time_" + email);

        if (savedCode == null || savedTime == null) {
            return false;
        }

        // 5 分鐘過期
        if (System.currentTimeMillis() - savedTime > 5 * 60 * 1000) {
            session.removeAttribute("email_verify_code_" + email);
            session.removeAttribute("email_verify_time_" + email);
            return false;
        }

        boolean isValid = savedCode.equals(code);
        if (isValid) {
            session.setAttribute("email_verified_" + email, true);
        }
        return isValid;
    }

    /**
     * 檢查該 Email 是否已通過驗證
     */
    public boolean isEmailVerified(String email, HttpSession session) {
        Boolean verified = (Boolean) session.getAttribute("email_verified_" + email);
        return verified != null && verified;
    }
}
