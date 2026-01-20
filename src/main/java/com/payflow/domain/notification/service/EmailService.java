package com.payflow.domain.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 간단한 텍스트 이메일 발송
     */
    public void sendSimpleEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
            log.info("📧 이메일 발송 성공: to={}, subject={}", to, subject);
        } catch (Exception e) {
            log.error("📧 이메일 발송 실패: to={}, error={}", to, e.getMessage());
            throw new RuntimeException("이메일 발송 실패", e);
        }
    }

    /**
     * HTML 이메일 발송
     */
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            
            mailSender.send(message);
            log.info("📧 HTML 이메일 발송 성공: to={}, subject={}", to, subject);
        } catch (MessagingException e) {
            log.error("📧 HTML 이메일 발송 실패: to={}, error={}", to, e.getMessage());
            throw new RuntimeException("이메일 발송 실패", e);
        }
    }

    /**
     * 결제 리마인더 이메일 HTML 템플릿
     */
    public String createPaymentReminderHtml(String subscriptionName, String dueDate, int daysAhead, int amount) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Arial', sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .highlight { background: #fff; padding: 20px; border-left: 4px solid #667eea; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 20px; color: #777; font-size: 12px; }
                    .button { display: inline-block; padding: 12px 30px; background: #667eea; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>💳 PayFlow</h1>
                        <p>결제 알림</p>
                    </div>
                    <div class="content">
                        <h2>안녕하세요!</h2>
                        <p><strong>%s</strong> 구독의 결제일이 다가오고 있습니다.</p>
                        <div class="highlight">
                            <p>📅 <strong>결제 예정일:</strong> %s</p>
                            <p>⏰ <strong>남은 기간:</strong> %d일</p>
                            <p>💰 <strong>결제 금액:</strong> ₩%,d원</p>
                        </div>
                        <p>결제 수단과 잔액을 미리 확인해주세요.</p>
                        <a href="http://localhost:5173/payments" class="button">결제 확인하기</a>
                        <div class="footer">
                            <p>이 메일은 PayFlow에서 자동으로 발송되었습니다.</p>
                            <p>알림 설정은 구독 관리 페이지에서 변경할 수 있습니다.</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """, subscriptionName, dueDate, daysAhead, amount);
    }

    /**
     * 연체 알림 이메일 HTML 템플릿
     */
    public String createOverdueNotificationHtml(String subscriptionName, String dueDate, int amount) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Arial', sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #ef4444 0%%, #dc2626 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .warning { background: #fef2f2; padding: 20px; border-left: 4px solid #ef4444; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 20px; color: #777; font-size: 12px; }
                    .button { display: inline-block; padding: 12px 30px; background: #ef4444; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>⚠️ PayFlow</h1>
                        <p>결제 연체 알림</p>
                    </div>
                    <div class="content">
                        <h2>결제일이 지났습니다</h2>
                        <p><strong>%s</strong> 구독의 결제가 완료되지 않았습니다.</p>
                        <div class="warning">
                            <p>📅 <strong>결제일:</strong> %s</p>
                            <p>💰 <strong>미납 금액:</strong> ₩%,d원</p>
                            <p>⚠️ 연체료가 발생하거나 서비스가 중단될 수 있습니다.</p>
                        </div>
                        <p>빠른 시일 내에 결제를 완료해주세요.</p>
                        <a href="http://localhost:5173/payments" class="button">결제 확인하기</a>
                        <div class="footer">
                            <p>이 메일은 PayFlow에서 자동으로 발송되었습니다.</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """, subscriptionName, dueDate, amount);
    }
}
