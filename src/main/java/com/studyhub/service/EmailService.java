package com.studyhub.service;

import com.studyhub.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    public void sendVerificationEmail(User user) {
        String link = baseUrl + "/verify?token=" + user.getVerificationToken();
        String body = "<p>Hello " + user.getFullName() + ",</p>"
                + "<p>Please verify your email by clicking the link below:</p>"
                + "<p><a href=\"" + link + "\">Verify my account</a></p>"
                + "<p>This link does not expire.</p>";
        send(user.getEmail(), "Verify your StudyHub account", body);
    }

    public void sendPasswordResetEmail(User user) {
        String link = baseUrl + "/reset-password?token=" + user.getResetPasswordToken();
        String body = "<p>Hello " + user.getFullName() + ",</p>"
                + "<p>Click the link below to reset your password. It expires in 30 minutes.</p>"
                + "<p><a href=\"" + link + "\">Reset my password</a></p>"
                + "<p>If you did not request this, ignore this email.</p>";
        send(user.getEmail(), "Reset your StudyHub password", body);
    }

    public void sendNewAccountEmail(User user, String rawPassword) {
        String body = "<p>Hello " + user.getFullName() + ",</p>"
                + "<p>An account has been created for you on StudyHub.</p>"
                + "<p>Username: <strong>" + user.getUsername() + "</strong></p>"
                + "<p>Password: <strong>" + rawPassword + "</strong></p>"
                + "<p>Please log in and change your password as soon as possible.</p>"
                + "<p><a href=\"" + baseUrl + "/login\">Log in to StudyHub</a></p>";
        send(user.getEmail(), "Your new StudyHub account", body);
    }

    private void send(String to, String subject, String htmlBody) {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email to " + to, e);
        }
    }
}
