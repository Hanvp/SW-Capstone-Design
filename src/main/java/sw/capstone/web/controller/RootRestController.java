package sw.capstone.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sw.capstone.service.FcmService;
import sw.capstone.web.dto.reponseDto.FcmResponseDto;
import sw.capstone.web.dto.requestDto.FcmRequestDto;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
public class RootRestController {

    private final FcmService fcmService;

    @GetMapping("/health")
    public String healthAPi(){
        return "i'm healthy";
    }

    @PostMapping("/fcm")
    public FcmResponseDto.FcmSendResultDto sendToFcmApi(@RequestBody FcmRequestDto.FcmSendInfo request) {
        return fcmService.sendFcm(request);
    }
}
