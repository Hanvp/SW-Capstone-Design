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
import sw.capstone.rabbitMQ.dto.CheckRecordPer1000;
import sw.capstone.rabbitMQ.dto.EmailRabbitMQDto;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;


@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RabbitMqService {

//    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.auth-code-expiration-millis}")
    private Long authCodeExpirationMillis;

    Boolean start = true;
    AtomicLong count = new AtomicLong(1);
    List<CheckRecordPer1000> recordDto = new ArrayList<>();


    private String setContext(String randomNum) {
        Context context = new Context();
        context.setVariable("code", randomNum);
        return templateEngine.process("email",context);
    }

    @RabbitListener(queues = "${spring.rabbitmq.queue.email}")
    public void receiveMessage(byte[] content) {

        if(count.get() % 10 == 0)
            log.info(LocalDateTime.now() + ": " + count.getAndIncrement());

//        if (rabbitMQDto == null){
//            log.error("정보가 안담겨옴");
//        }
//        else {
//            if (start){
//                recordDto.add(new CheckRecordPer1000(1L, LocalDateTime.now()));
////                log.info("1st record consume time: " + LocalDateTime.now());
//                start = false;
//            }
//
//            if (count.getAndIncrement() % 1000 == 0) {
//                recordDto.add(new CheckRecordPer1000(count.get()-1, LocalDateTime.now()));
//            }
//
//            log.info(rabbitMQDto.getTargetEmail(), rabbitMQDto.getRandomNum());
//
//            //처리할 로직 구현
//            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
//
//            try {
//                MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
//                mimeMessageHelper.setTo(rabbitMQDto.getTargetEmail());
//                mimeMessageHelper.setSubject("sw-capstone 인증 이메일 테스트");
//                mimeMessageHelper.setText(setContext(rabbitMQDto.getRandomNum()), true);
//                javaMailSender.send(mimeMessage);
//
////                log.info(EmailConverter.toEmailResultDto(rabbitMQDto.getTargetEmail(), rabbitMQDto.getRandomNum(), "success").toString());
//            } catch (MessagingException e) {
////                log.info(EmailConverter.toEmailResultDto(rabbitMQDto.getTargetEmail(), rabbitMQDto.getRandomNum(), "fail").toString());
//            }
//        }
    }

    public void getLog() {
        recordDto.stream().forEach(record -> log.info(record.getCount() + ": " + record.getLocalDateTime()));
    }
}
