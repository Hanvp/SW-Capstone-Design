package sw.capstone.service.serviceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sw.capstone.domain.*;
import sw.capstone.redis.dto.RedisDto;
import sw.capstone.redis.util.RedisOperator;
import sw.capstone.repository.*;
import sw.capstone.service.NotifcationService;
import sw.capstone.web.dto.requestDto.FcmRequestDto;
import sw.capstone.web.dto.requestDto.SmsRequestDto;
import sw.capstone.web.dto.responseDto.FcmResponseDto;
import sw.capstone.web.dto.responseDto.SmsResponseDto;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotifcationServiceImpl implements NotifcationService {

    private final MemberRepository memberRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final RandomNumRepository randomNumRepository;
    private final MemberRandomNumRepository memberRandomNumRepository;
    private final MemberNotificationRepository memberNotificationRepository;

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisOperator redisOperator;

    @Value("${spring.redis.key}")
    private String streamKey;

    @Value("${spring.redis.group.sms}")
    private String smsGroup;

    @Value("${spring.redis.group.email}")
    private String emailGroup;

    @Value("${spring.redis.group.fcm}")
    private String fcmGroup;

    @Scheduled(fixedRateString = "${spring.redis.publish.rate}")
    public void publishEvent(){

    }

    @Transactional(readOnly = false)
    @Override
    public SmsResponseDto.SmsResultDto sendSmsWorker(SmsRequestDto.request request) {
        Optional<Member> findMember = memberRepository.findByPhoneNum(request.getTargetPhoneNum());

        if(findMember == null){
            log.error("해당 전화번호를을 가지고 있는 사용자가 없습니다.");
            return SmsResponseDto.SmsResultDto.builder().succeed("fail").build();
        }

        String randomNum = saveRandomNum(findMember.get());

        /*
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();

        valueOperations.set("targetPhoneNum", request.getTargetPhoneNum());
        valueOperations.set("randomNum", randomNum);
         */

        ObjectRecord<String, Object> record = StreamRecords.newRecord()
                .ofObject((Object) RedisDto.SmsRedisStream.builder()
                        .targetPhoneNum(findMember.get().getPhoneNum())
                        .randomNum(randomNum)
                        .build())
                .withStreamKey(streamKey);

        redisOperator.sendToGroup(streamKey, smsGroup, record);


        return SmsResponseDto.SmsResultDto.builder()
                .phoneNum(findMember.get().getPhoneNum())
                .succeed("success")
                .build();
    }

    @Transactional(readOnly = false)
    @Override
    public FcmResponseDto.FcmResultDto sendFcmWorker(FcmRequestDto.request request) {

        Optional<FcmToken> findFcmInfo = fcmTokenRepository.findByFcmToken(request.getFcmToken());

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



        log.info("accessToken: ", accessToken, ", targetFcmToken: ", request.getFcmToken(),
                ", title: ", notification.getTitle(), ", body: ", notification.getBody());


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

    private String saveRandomNum(Member member) {
        String randomNum = UUID.randomUUID().toString().substring(0,6);

        while (randomNumRepository.existsByValue(randomNum)){
            randomNum = UUID.randomUUID().toString().substring(0,6);
        }

        RandomNum savedRandomNum = randomNumRepository.save(RandomNum.builder().value(randomNum).build());
        memberRandomNumRepository.save(MemberRandomNum.builder()
                .member(member)
                .randomNum(savedRandomNum)
                .build());

        return randomNum;
    }
}
