package sw.capstone.service.serviceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sw.capstone.domain.*;
import sw.capstone.kafka.dto.EmailKafkaDto;
import sw.capstone.kafka.producer.KafkaProducer;
import sw.capstone.rabbitMQ.dto.EmailRabbitMQDto;
import sw.capstone.rabbitMQ.dto.SmsRabbitMQDto;
import sw.capstone.redis.dto.EmailRedisStream;
import sw.capstone.redis.dto.SmsRedisStream;
import sw.capstone.repository.*;
import sw.capstone.service.NotificationService;
import sw.capstone.web.dto.CheckRecordPer1000;
import sw.capstone.web.dto.responseDto.BasicResponseDto;
import sw.capstone.web.dto.responseDto.EmailResponseDto;
import sw.capstone.web.dto.responseDto.FcmResponseDto;
import sw.capstone.web.dto.responseDto.SmsResponseDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final MemberRepository memberRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final RandomNumRepository randomNumRepository;
    private final MemberRandomNumRepository memberRandomNumRepository;
    private final MemberNotificationRepository memberNotificationRepository;

    private final RedisTemplate<String, Object> redisTemplate;

    private final RabbitTemplate rabbitTemplate;
    private final KafkaProducer kafkaProducer;

    List<CheckRecordPer1000> recordDto = new ArrayList<>();

    @Value("${spring.redis.key.sms}")
    private String smsStream;

    @Value("${spring.redis.key.email}")
    private String emailStream;

    @Value("${spring.redis.key.fcm}")
    private String fcmStream;

    @Value("${spring.rabbitmq.exchange.sms}")
    private String smsExchange;

    @Value("${spring.rabbitmq.exchange.email}")
    private String emailExchange;

    @Value("${spring.rabbitmq.exchange.fcm}")
    private String fcmExchange;

    @Value("${spring.rabbitmq.routing.sms}")
    private String smsRoutingKey;

    @Value("${spring.rabbitmq.routing.email}")
    private String emailRoutingKey;

    @Value("${spring.rabbitmq.routing.fcm}")
    private String fcmRoutingKey;

    @Value("${spring.kafka.topic.email}")
    private String emailTopic;

    @Value("${spring.kafka.topic.sms}")
    private String smsTopic;

    @Value("${spring.kafka.topic.fcm}")
    private String fcmTopic;

    private Boolean start = true;
    AtomicLong count= new AtomicLong(1);


    @Transactional(readOnly = false)
    @Override
    public SmsResponseDto.SmsResultDto sendSmsRedisWorker(Long memberId) {

        Optional<Member> findMember = memberRepository.findById(memberId);

        MemberRandomNum memberRandomNum = setRandomNum(findMember);

        SmsRedisStream smsRedisStream = new SmsRedisStream(memberRandomNum.getMember().getPhoneNum(), memberRandomNum.getRandomNum().getValue());

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String value = objectMapper.writeValueAsString(smsRedisStream);
            HashMap<String, String> map = new HashMap<>();
            map.put("info",value);
            this.redisTemplate.opsForStream().add(smsStream, map);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return SmsResponseDto.SmsResultDto.builder()
                .phoneNum(memberRandomNum.getMember().getPhoneNum())
                .succeed("success")
                .build();
    }

    @Override
    @Transactional(readOnly = false)
    public SmsResponseDto.SmsResultDto sendSmsMqWorker(Long memberId) {

        Optional<Member> findMember = memberRepository.findById(memberId);

        MemberRandomNum memberRandomNum = setRandomNum(findMember);

        rabbitTemplate.convertAndSend(smsExchange, smsRoutingKey,
                new SmsRabbitMQDto(memberRandomNum.getMember().getPhoneNum(),
                memberRandomNum.getRandomNum().getValue()));

        return SmsResponseDto.SmsResultDto.builder()
                .phoneNum(memberRandomNum.getMember().getPhoneNum())
                .succeed("success")
                .build();
    }

    @Transactional(readOnly = false)
    @Override
    public EmailResponseDto.EmailResultDto sendEmailRedisWorker(Long memberId) {

        Optional<Member> findMember = memberRepository.findById(memberId);

        MemberRandomNum memberRandomNum = setRandomNum(findMember);
        EmailRedisStream emailRedisStream = new EmailRedisStream(memberRandomNum.getMember().getEmail(), memberRandomNum.getRandomNum().getValue());

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String value = objectMapper.writeValueAsString(emailRedisStream);
            HashMap<String, String> map = new HashMap<>();
            map.put("info",value);
            this.redisTemplate.opsForStream().add(emailStream, map);

            if (start){
                log.info("1st record produce time: "+ LocalDateTime.now());
                start = false;
            }

            if (count.getAndIncrement() % 10000 == 0) {
                recordDto.add(new CheckRecordPer1000(count.get()-1, LocalDateTime.now()));
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return EmailResponseDto.EmailResultDto.builder()
                .email(memberRandomNum.getMember().getEmail())
                .succeed("success")
                .build();

    }

    @Transactional(readOnly = false)
    @Override
    public EmailResponseDto.EmailResultDto sendEmailMqWorker(Long memberId) {
        Optional<Member> findMember = memberRepository.findById(memberId);

        MemberRandomNum memberRandomNum = setRandomNum(findMember);
        rabbitTemplate.convertAndSend(emailExchange, emailRoutingKey,
                new EmailRabbitMQDto(memberRandomNum.getMember().getEmail(),
                        memberRandomNum.getRandomNum().getValue()));

        if (start){
            log.info("1st record produce time: "+ LocalDateTime.now());
            start = false;
        }

        if (count.getAndIncrement() % 10000 == 0) {
            recordDto.add(new CheckRecordPer1000(count.get()-1, LocalDateTime.now()));
        }

        return EmailResponseDto.EmailResultDto.builder()
                .email(memberRandomNum.getMember().getEmail())
                .succeed("success")
                .build();
    }

    @Transactional(readOnly = false)
    @Override
    public EmailResponseDto.EmailResultDto sendEmailKafkaWorker(Long memberId) throws JsonProcessingException {
        Optional<Member> findMember = memberRepository.findById(memberId);

        MemberRandomNum memberRandomNum = setRandomNum(findMember);


        kafkaProducer.send(emailTopic, EmailKafkaDto.builder()
                .targetEmail(findMember.get().getEmail())
                .randomNum(memberRandomNum.getRandomNum().getValue()).build());

        if (start){
            log.info("1st record produce time: "+ LocalDateTime.now());
            start = false;
        }

        if (count.getAndIncrement() % 10000 == 0) {
            recordDto.add(new CheckRecordPer1000(count.get()-1, LocalDateTime.now()));
        }


        return EmailResponseDto.EmailResultDto.builder()
                .email(memberRandomNum.getMember().getEmail())
                .succeed("success")
                .build();
    }

    @Transactional(readOnly = false)
    @Override
    public FcmResponseDto.FcmResultDto sendFcmWorker(Long memberId) {

        Member findMember = memberRepository.findById(memberId).get();
        Optional<FcmToken> findFcmInfo = fcmTokenRepository.findByMember(findMember);

        if (findFcmInfo == null){
            log.error("해당 Fcm Token을 가지고 있는 사용자가 없습니다.");
            return FcmResponseDto.FcmResultDto.builder().succeed("fail").build();
        }

        Notification notification = memberNotificationRepository.findNotificationByMember(findFcmInfo.get().getMember());

        String accessToken;
        try {
            accessToken = getAccessToken();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }



//        log.info("accessToken: ", accessToken, ", targetFcmToken: ", request.getFcmToken(),
//                ", title: ", notification.getTitle(), ", body: ", notification.getBody());


        return null;
    }

    /**
     * firebase로 부터 access token을 가져온다.
     */
    private String getAccessToken() throws IOException {

        /**
         * json 파일 내 경로(new ClassPathResource 내) 맞는지 확인 필요
         */
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource("././config/FcmConfig.json").getInputStream())
                .createScoped(Arrays.asList("https://www.googleapis.com/auth/cloud-platform"));

        googleCredentials.refreshIfExpired();

        return googleCredentials.getAccessToken().getTokenValue();

    }

    private MemberRandomNum setRandomNum(Optional<Member> findMember) {

        if(findMember == null){
            log.error("해당 id를을 가지고 있는 사용자가 없습니다.");
            throw new RuntimeException("해당 id를을 가지고 있는 사용자가 없습니다.");
        }

        return saveRandomNum(findMember.get());
    }

    private MemberRandomNum saveRandomNum(Member member) {
        String randomNum = UUID.randomUUID().toString().substring(0,6);

        while (randomNumRepository.existsByValue(randomNum)){
            randomNum = UUID.randomUUID().toString().substring(0,6);
        }

        RandomNum savedRandomNum = randomNumRepository.save(RandomNum.builder().value(randomNum).build());
        return memberRandomNumRepository.save(MemberRandomNum.builder()
                .member(member)
                .randomNum(savedRandomNum)
                .build());
    }

    @Override
    public void getLog() {
        recordDto.stream().forEach(record -> log.info(record.getCount() + ": "+ record.getLocalDateTime()));
    }

    //----- batch로 동작 -----

    @Transactional(readOnly = false)
    @Override
    public BasicResponseDto.ResultDto batchEmailRedisWorker(byte[] content) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            HashMap<String, String> map = new HashMap<>();
            String value = objectMapper.writeValueAsString(content);

            map.put("info", value);
            map.put("claimTime",String.valueOf(System.currentTimeMillis()));
            this.redisTemplate.opsForStream().add(emailStream, map);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return BasicResponseDto.ResultDto.builder()
                .succeed("success")
                .build();

    }

    @Transactional(readOnly = false)
    @Override
    public BasicResponseDto.ResultDto batchEmailMqWorker(byte[] content) {

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String value = objectMapper.writeValueAsString(content);
            value += LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            rabbitTemplate.convertAndSend(emailExchange, emailRoutingKey, content);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

//        if (count.getAndIncrement() % 10000 == 0) {
//            recordDto.add(new CheckRecordPer1000(count.get()-1, LocalDateTime.now()));
//        }

        return BasicResponseDto.ResultDto.builder()
                .succeed("success")
                .build();
    }

    @Transactional(readOnly = false)
    @Override
    public BasicResponseDto.ResultDto batchEmailKafkaWorker(byte[] content) throws JsonProcessingException {


        kafkaProducer.send(emailTopic, content);

//        if (count.getAndIncrement() % 10000 == 0) {
//            recordDto.add(new CheckRecordPer1000(count.get()-1, LocalDateTime.now()));
//        }

        return BasicResponseDto.ResultDto.builder()
                .succeed("success")
                .build();
    }
}
