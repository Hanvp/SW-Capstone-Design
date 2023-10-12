package sw.capstone.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sw.capstone.service.NotifcationService;
import sw.capstone.web.dto.requestDto.EmailRequestDto;
import sw.capstone.web.dto.requestDto.FcmRequestDto;
import sw.capstone.web.dto.requestDto.SmsRequestDto;
import sw.capstone.web.dto.responseDto.EmailResponseDto;
import sw.capstone.web.dto.responseDto.FcmResponseDto;
import sw.capstone.web.dto.responseDto.SmsResponseDto;


@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
public class RootRestController {

    private final NotifcationService notifcationService;

    @GetMapping("/health")
    public String healthAPi(){
        return "i'm healthy";
    }

    @PostMapping("/redis/sms")
    public SmsResponseDto.SmsResultDto sendSmsRedis(@RequestBody SmsRequestDto.request request) {
        return notifcationService.sendSmsRedisWorker(request);
    }

    @PostMapping("/rabbitMq/sms")
    public SmsResponseDto.SmsResultDto sendSmsRabbitMq(@RequestBody SmsRequestDto.request request) {
        return notifcationService.sendSmsMqWorker(request);
    }

    @PostMapping("/redis/email")
    public EmailResponseDto.EmailResultDto sendEmailRedis(@RequestBody EmailRequestDto.request request) {
        return notifcationService.sendEmailRedisWorker(request);
    }

    @PostMapping("/rabbitMq/email")
    public EmailResponseDto.EmailResultDto sendEmailRabbitMq(@RequestBody EmailRequestDto.request request) {
        return notifcationService.sendEmailMqWorker(request);
    }

    @PostMapping("/server/fcm")
    public FcmResponseDto.FcmResultDto sendFcm(@RequestBody FcmRequestDto.request request) {
        return notifcationService.sendFcmWorker(request);
    }
}
