package com.devonoff.util;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailProvider {

  private final JavaMailSender javaMailSender;

  private final String SUBJECT = "[DevOnOff] 회원가입 인증 메일입니다.";

  /**
   * 인증메일 전송
   *
   * @param email
   * @param certificationNumber
   * @return boolean
   */
  public boolean sendCertificationMail(String email, String certificationNumber) {
    try {
      MimeMessage message = javaMailSender.createMimeMessage();
      MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);

      String htmlContent = getCertificationMessage(certificationNumber);

      messageHelper.setTo(email);
      messageHelper.setSubject(SUBJECT);
      messageHelper.setText(htmlContent, true);

      javaMailSender.send(message);
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  /**
   * 인증메일 본문 내용 작성
   *
   * @param certificationNumber
   * @return String
   */
  private String getCertificationMessage(String certificationNumber) {
    return "<h1>" +
        "[DevOnOff] 회원가입 인증 메일" +
        "</h1>" +
        "<h3>" +
        "인증코드 : <strong style='font-size: 32px; letter-spacing: 8px;'>" +
        certificationNumber + "</strong>" +
        "</h3>";
  }
}
