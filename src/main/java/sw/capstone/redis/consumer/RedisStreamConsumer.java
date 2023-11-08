package sw.capstone.redis.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import sw.capstone.converter.EmailConverter;
import sw.capstone.redis.dto.EmailRedisStream;
import sw.capstone.redis.util.RedisOperator;
import sw.capstone.web.dto.CheckRecordPer1000;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
//@Component
@Service
@RequiredArgsConstructor
public class RedisStreamConsumer implements StreamListener<String, MapRecord<String, Object, Object>>, InitializingBean, DisposableBean {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.auth-code-expiration-millis}")
    private Long authCodeExpirationMillis;

    private StreamMessageListenerContainer<String, MapRecord<String, Object, Object>> listenerContainer;
    private Subscription subscription;
    private String streamKey;
    private String consumerGroupName;
    private String consumerName;

    // 위에 구현한 Redis Streamd에 필요한 기본 Command를 구현한 Component
    private final RedisOperator redisOperator;

    List<CheckRecordPer1000> recordDto = new ArrayList<>();

    private Boolean start = true;
    AtomicLong count= new AtomicLong(1);

    private String setContext(String randomNum) {
        Context context = new Context();
        context.setVariable("code", randomNum);
        return templateEngine.process("email",context);
    }

    @Override
    public void onMessage(MapRecord<String, Object, Object> record) {
        // 처리할 로직 구현

        if(count.get() % 10 == 0)
            log.info(LocalDateTime.now() + ": " + count.getAndIncrement());

        // 이후, ack stream
        this.redisOperator.ackStream("email", record);



//        // 처리할 로직 구현
//        log.info(record.toString());
//
//        String data = (String) record.getValue().get("info");
//
//        Optional<EmailRedisStream> value = null;
//
//        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            value = Optional.of(objectMapper.readValue(data, EmailRedisStream.class));
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//
//        if (value.get() == null){
//            log.error("정보가 안담겨옴");
//        }
//        else {
//            if (start){
//                recordDto.add(new CheckRecordPer1000(1L, LocalDateTime.now()));
//                start = false;
//            }
//
//            if (count.getAndIncrement() % 10000 == 0) {
//                recordDto.add(new CheckRecordPer1000(count.get()-1, LocalDateTime.now()));
//            }
//
//            //처리할 로직 구현
//            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
//
//            try {
//                MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
//                mimeMessageHelper.setTo(value.get().getTargetEmail());
//                mimeMessageHelper.setSubject("sw-capstone 인증 이메일 테스트");
//                mimeMessageHelper.setText(setContext(value.get().getRandomNum()), true);
//                javaMailSender.send(mimeMessage);
//
//                log.info(EmailConverter.toEmailResultDto(value.get().getTargetEmail(), value.get().getRandomNum(), "success").toString());
//            } catch (MessagingException e) {
//                log.info(EmailConverter.toEmailResultDto(value.get().getTargetEmail(), value.get().getRandomNum(), "fail").toString());
//            }
//
//            // 이후, ack stream
//            this.redisOperator.ackStream("email", record);
//        }
    }

    @Override
    public void destroy() throws Exception {
        if(this.subscription != null){
            this.subscription.cancel();
        }
        if(this.listenerContainer != null){
            this.listenerContainer .stop();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Stream 기본 정보
        this.streamKey = "stream:email";
        this.consumerGroupName = "email";
        this.consumerName = UUID.randomUUID().toString().substring(5);

        // Consumer Group 설정
        this.redisOperator.createStreamConsumerGroup(streamKey, consumerGroupName);

        // StreamMessageListenerContainer 설정
        this.listenerContainer = this.redisOperator.createStreamMessageListenerContainer();

        //Subscription 설정
        this.subscription = this.listenerContainer.receive(
                Consumer.from(this.consumerGroupName, consumerName),
                StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
                this
        );

        // 0.01밀리초 마다, 정보 GET
//        this.subscription.await(Duration.ofNanos(10000));

        // redis listen 시작
        this.listenerContainer.start();
    }

    public void getLog() {
        recordDto.stream().forEach(record -> log.info(record.getCount() + ": "+ record.getLocalDateTime()));
    }
}
