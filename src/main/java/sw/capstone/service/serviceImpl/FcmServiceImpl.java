package sw.capstone.service.serviceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sw.capstone.feignClient.FcmFeignClient;
import sw.capstone.feignClient.dto.requestDto.FcmFeignRequestDto;
import sw.capstone.service.FcmService;
import sw.capstone.web.dto.reponseDto.FcmResponseDto;
import sw.capstone.web.dto.requestDto.FcmRequestDto;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FcmServiceImpl implements FcmService {

    private final FcmFeignClient fcmFeignClient;
    private final ObjectMapper objectMapper;

    public String makeMessage( FcmRequestDto.FcmSendInfo request) throws JsonProcessingException {

        FcmFeignRequestDto.FcmRequestDto fcmMessage = FcmFeignRequestDto.FcmRequestDto.builder()
                .message(
                        FcmFeignRequestDto.Message.builder()
                                .token(request.getTargetFcmToken())
                                .notification(
                                        FcmFeignRequestDto.Notification.builder()
                                                .title(request.getTitle())
                                                .body(request.getBody())
                                                .build()
                                )
                                .data(
                                        FcmFeignRequestDto.Data.builder()
                                                .title(request.getTitle())
                                                .body(request.getBody())
                                                .build()
                                )
                                .build()
                )
                .validateOnly(false)
                .build();

        return objectMapper.writeValueAsString(fcmMessage);

    }

    @Override
    public FcmResponseDto.FcmSendResultDto sendFcm(FcmRequestDto.FcmSendInfo request) throws JsonProcessingException {
        String result = fcmFeignClient.sendFcm(makeMessage(request), "application/json; UTF-8", "Bearer " + request.getAccessToken());

        return FcmResponseDto.FcmSendResultDto.builder()
                .fcmToken(request.getTargetFcmToken())
                .title(request.getTitle())
                .succeed(result)
                .build();
    }
}
