package sw.capstone.rabbitMQ.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import sw.capstone.converter.EmailConverter;
import sw.capstone.rabbitMQ.dto.EmailRabbitMQDto;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;


@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RabbitMqService {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.auth-code-expiration-millis}")
    private Long authCodeExpirationMillis;

    private String setContext(String randomNum) {
        Context context = new Context();
        context.setVariable("code", randomNum);
        return templateEngine.process("email",context);
    }

    @RabbitListener(queues = "${spring.rabbitmq.queue.email}")
    public void receiveMessage(EmailRabbitMQDto rabbitMQDto) {

        if (rabbitMQDto == null){
            log.error("정보가 안담겨옴");
        }
        else {
            log.info(rabbitMQDto.getTargetEmail(), rabbitMQDto.getRandomNum());

            //처리할 로직 구현
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            try {
                MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
                mimeMessageHelper.setTo(rabbitMQDto.getTargetEmail());
                mimeMessageHelper.setSubject("sw-capstone 인증 이메일 테스트");
                mimeMessageHelper.setText(setContext(rabbitMQDto.getRandomNum()), true);
                javaMailSender.send(mimeMessage);

                log.info(EmailConverter.toEmailResultDto(rabbitMQDto.getTargetEmail(), rabbitMQDto.getRandomNum(), "success").toString());
            } catch (MessagingException e) {
                log.info(EmailConverter.toEmailResultDto(rabbitMQDto.getTargetEmail(), rabbitMQDto.getRandomNum(), "fail").toString());
            }
        }
    }
}
