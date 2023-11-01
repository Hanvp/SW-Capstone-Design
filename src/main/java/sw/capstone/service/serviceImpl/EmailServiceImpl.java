package sw.capstone.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import sw.capstone.converter.EmailConverter;
import sw.capstone.service.EmailService;
import sw.capstone.web.dto.CheckRecordPer1000;
import sw.capstone.web.dto.requestDto.EmailRequestDto;
import sw.capstone.web.dto.responseDto.EmailResponseDto;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.auth-code-expiration-millis}")
    private Long authCodeExpirationMillis;

    @Override
    public EmailResponseDto.EmailSendResultDto sendEmail(EmailRequestDto.EmailSendInfo request) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessageHelper.setTo(request.getTargetEmail());
            mimeMessageHelper.setSubject("sw-capstone 인증 이메일 테스트");
            mimeMessageHelper.setText(setContext(request.getRandomNum()), true);
            javaMailSender.send(mimeMessage);

            return EmailConverter.toEmailResultDto(request.getTargetEmail(), request.getRandomNum(), "success");
        } catch (MessagingException e) {
            return EmailConverter.toEmailResultDto(request.getTargetEmail(), request.getRandomNum(),"fail");
        }
    }

    private String setContext(String randomNum) {
        Context context = new Context();
        context.setVariable("code", randomNum);
        return templateEngine.process("email",context);
    }

}
