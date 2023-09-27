package sw.sms.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sw.sms.service.SmsService;
import sw.sms.web.dto.requestDto.SmsRequestDto;
import sw.sms.web.dto.responseDto.SmsReponseDto;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
public class RootRestController {

    private final SmsService smsService;

    @GetMapping("/health")
    public String healthAPi(){
        return "i'm healthy";
    }

    @PostMapping("/sms")
    public SmsReponseDto.SmsSendResultDto sendToSmsApi(@RequestBody SmsRequestDto.SmsSendInfo request){
        return smsService.sendSms(request);
    }
}
