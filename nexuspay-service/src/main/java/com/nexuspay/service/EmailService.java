package com.nexuspay.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@nexuspay.local}")
    private String from;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Async
    public void sendVerificationEmail(String to, String token) {
        String subject = "Verify your NexusPay account";
        String link = baseUrl + "/api/v1/auth/verify?token=" + token;
        String body = String.format("""
                Welcome to NexusPay!

                Please verify your email by clicking the link below:
                %s

                This link expires in 24 hours.

                If you did not create this account, please ignore this email.
                """, link);
        send(to, subject, body);
    }

    @Async
    public void sendPasswordResetEmail(String to, String token) {
        String subject = "Reset your NexusPay password";
        String link = baseUrl + "/api/v1/auth/password-reset/confirm?token=" + token;
        String body = String.format("""
                You requested a password reset for your NexusPay account.

                Click the link below to reset your password:
                %s

                This link expires in 1 hour.

                If you did not request this reset, please ignore this email.
                """, link);
        send(to, subject, body);
    }

    @Async
    public void sendInvoiceEmail(String to, String invoiceNumber, String amount, String currency) {
        String subject = "Invoice " + invoiceNumber + " from NexusPay";
        String body = String.format("""
                Your invoice %s has been generated.

                Amount: %s %s
                Status: Paid

                Thank you for your business!

                View your invoice in the NexusPay dashboard.
                """, invoiceNumber, amount.toUpperCase(), currency);
        send(to, subject, body);
    }

    @Async
    public void sendPaymentConfirmation(String to, String amount, String currency, String paymentId) {
        String subject = "Payment confirmed - " + paymentId;
        String body = String.format("""
                Your payment of %s %s has been confirmed.

                Payment ID: %s

                Thank you for your payment!
                """, amount, currency.toUpperCase(), paymentId);
        send(to, subject, body);
    }

    @Async
    public void sendMfaBackupCodes(String to, String codes) {
        String subject = "Your NexusPay MFA backup codes";
        String body = String.format("""
                Here are your MFA backup codes for NexusPay.

                %s

                Store these codes in a safe place. Each code can be used once.
                If you lose access to your authenticator app, use a backup code to sign in.
                """, codes);
        send(to, subject, body);
    }

    @Async
    public void sendWelcomeEmail(String to, String merchantName) {
        String subject = "Welcome to NexusPay, " + merchantName + "!";
        String body = String.format("""
                Welcome to NexusPay!

                Your merchant account "%s" has been created successfully.

                Get started:
                1. Configure your payment connectors
                2. Set up routing rules
                3. Create your first payment intent

                Visit your dashboard: %s

                If you have any questions, contact our support team.
                """, merchantName, baseUrl);
        send(to, subject, body);
    }

    private void send(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to {}: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}