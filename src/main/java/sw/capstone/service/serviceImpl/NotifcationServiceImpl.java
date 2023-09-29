package sw.capstone.service.serviceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sw.capstone.domain.*;
import sw.capstone.repository.*;
import sw.capstone.service.NotifcationService;
import sw.capstone.web.dto.requestDto.FcmRequestDto;
import sw.capstone.web.dto.responseDto.FcmResponseDto;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotifcationServiceImpl implements NotifcationService {

    private final FcmTokenRepository fcmTokenRepository;
    private final RandomNumRepository randomNumRepository;
    private final MemberRandomNumRepository memberRandomNumRepository;
    private final MemberNotificationRepository memberNotificationRepository;

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
