package com.community.tools.service;

import com.community.tools.model.EmailBuild;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

  public final JavaMailSender emailSender;

  public EmailService(JavaMailSender emailSender) {
    this.emailSender = emailSender;
  }

  /**
   * This method send email.
   * @param builder email recipient/subject/text.
  */
  public void sendEmail(EmailBuild builder) {
    MimeMessage message = emailSender.createMimeMessage();
    MimeMessageHelper helper;
    try {
      helper = new MimeMessageHelper(message, true, "utf-8");
      helper.setTo(builder.getUserEmail());
      helper.setSubject(builder.getSubject());
      helper.setText(builder.getText(), true);
    } catch (MessagingException e) {
      log.error("Can't send Email. " + e.getMessage());
    }

    // Send Email!
    this.emailSender.send(message);
  }

}
