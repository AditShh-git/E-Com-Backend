package com.one.vm.utils;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailUtil {

	private final JavaMailSender mailSender;

    public void sendEmail(String toEmail, String subject, String message) {

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(toEmail);
        mail.setSubject(subject);
        mail.setText(message);

        mailSender.send(mail);
    }
}
