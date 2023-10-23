package sw.capstone.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sw.capstone.service.NotificationService;
import sw.capstone.service.RootService;
import sw.capstone.web.dto.requestDto.EmailRequestDto;
import sw.capstone.web.dto.requestDto.FcmRequestDto;
import sw.capstone.web.dto.requestDto.RootRequestDto;
import sw.capstone.web.dto.requestDto.SmsRequestDto;
import sw.capstone.web.dto.responseDto.EmailResponseDto;
import sw.capstone.web.dto.responseDto.FcmResponseDto;
import sw.capstone.web.dto.responseDto.SmsResponseDto;


@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
public class RootRestController {

    private final NotificationService notificationService;
    private final RootService rootService;

    @GetMapping("/health")
    public String healthAPi(){
        return "i'm healthy";
    }

    @PostMapping("/redis/sms")
    public SmsResponseDto.SmsResultDto sendSmsRedis(@RequestBody SmsRequestDto.request request) {
        return notificationService.sendSmsRedisWorker(request);
    }

    @PostMapping("/rabbitMq/sms")
    public SmsResponseDto.SmsResultDto sendSmsRabbitMq(@RequestBody SmsRequestDto.request request) {
        return notificationService.sendSmsMqWorker(request);
    }

    @PostMapping("/redis/email")
    public EmailResponseDto.EmailResultDto sendEmailRedis(@RequestBody EmailRequestDto.request request) {
        return notificationService.sendEmailRedisWorker(request);
    }

    @PostMapping("/rabbitMq/email")
    public EmailResponseDto.EmailResultDto sendEmailRabbitMq(@RequestBody EmailRequestDto.request request) {
        return notificationService.sendEmailMqWorker(request);
    }

    @PostMapping("/kafka/email")
    public EmailResponseDto.EmailResultDto sendEmailKafka(@RequestBody EmailRequestDto.request request) throws JsonProcessingException {
        return notificationService.sendEmailKafkaWorker(request);
    }

    @PostMapping("/server/fcm")
    public FcmResponseDto.FcmResultDto sendFcm(@RequestBody FcmRequestDto.request request) {
        return notificationService.sendFcmWorker(request);
    }

    @PostMapping("/setDB")
    public void setDB(@RequestBody RootRequestDto.request request) {
        rootService.setDB(request);
    }
}
