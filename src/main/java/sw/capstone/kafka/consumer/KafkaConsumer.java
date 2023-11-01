package sw.capstone.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import sw.capstone.converter.EmailConverter;
import sw.capstone.web.dto.CheckRecordPer1000;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.auth-code-expiration-millis}")
    private Long authCodeExpirationMillis;

    List<CheckRecordPer1000> recordDto = new ArrayList<>();

    private Boolean start = true;
    AtomicLong count= new AtomicLong(1);

    private String setContext(String randomNum) {
        Context context = new Context();
        context.setVariable("code", randomNum);
        return templateEngine.process("email",context);
    }


    @KafkaListener(topics = "${spring.kafka.topic.email}")
    public void receiveMessage(String kafkaMessage) {
        if (kafkaMessage == null){
            log.error("정보가 안담겨옴");
        }
        else {
            Map<Object, Object> map = new HashMap<>();
            ObjectMapper objectMapper = new ObjectMapper();

            try {
                map = objectMapper.readValue(kafkaMessage, new TypeReference<Map<Object, Object>>() {});
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            log.info(map.get("targetEmail").toString(), map.get("randomNum").toString());


            //처리할 로직 구현
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            try {
                if (start){
                    recordDto.add(new CheckRecordPer1000(1L, LocalDateTime.now()));
                    start = false;
                }

                if (count.getAndIncrement() % 1000 == 0) {
                    recordDto.add(new CheckRecordPer1000(count.get()-1, LocalDateTime.now()));
                }

                MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
                mimeMessageHelper.setTo(map.get("targetEmail").toString());
                mimeMessageHelper.setSubject("sw-capstone 인증 이메일 테스트");
                mimeMessageHelper.setText(setContext(map.get("randomNum").toString()), true);
                javaMailSender.send(mimeMessage);

                log.info(EmailConverter.toEmailResultDto(map.get("targetEmail").toString(), map.get("randomNum").toString(), "success").toString());
            } catch (MessagingException e) {
                log.info(EmailConverter.toEmailResultDto(map.get("targetEmail").toString(), map.get("randomNum").toString(), "fail").toString());
            }
        }
    }

    public void getLog() {
        recordDto.stream().forEach(record -> log.info(record.getCount() + ": "+ record.getLocalDateTime()));
    }
}
