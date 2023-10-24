package sw.capstone.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import sw.capstone.service.NotificationService;
import sw.capstone.service.RootService;
import sw.capstone.web.dto.requestDto.RootRequestDto;
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

    @GetMapping("/redis/sms/{memberId}")
    public SmsResponseDto.SmsResultDto sendSmsRedis(@PathVariable(name = "memberId") Long request) {
        return notificationService.sendSmsRedisWorker(request);
    }

    @PostMapping("/rabbitMq/sms/{memberId}")
    public SmsResponseDto.SmsResultDto sendSmsRabbitMq(@PathVariable(name = "memberId") Long request) {
        return notificationService.sendSmsMqWorker(request);
    }

    @PostMapping("/redis/email/{memberId}")
    public EmailResponseDto.EmailResultDto sendEmailRedis(@PathVariable(name = "memberId") Long request) {
        return notificationService.sendEmailRedisWorker(request);
    }

    @PostMapping("/rabbitMq/email/{memberId}")
    public EmailResponseDto.EmailResultDto sendEmailRabbitMq(@PathVariable(name = "memberId") Long request) {
        return notificationService.sendEmailMqWorker(request);
    }

    @PostMapping("/kafka/email/{memberId}")
    public EmailResponseDto.EmailResultDto sendEmailKafka(@PathVariable(name = "memberId") Long request) throws JsonProcessingException {
        return notificationService.sendEmailKafkaWorker(request);
    }

    @PostMapping("/server/fcm/{memberId}")
    public FcmResponseDto.FcmResultDto sendFcm(@PathVariable(name = "memberId") Long request) {
        return notificationService.sendFcmWorker(request);
    }

    @PostMapping("/setDB")
    public void setDB(@RequestBody RootRequestDto.request request) {
        rootService.setDB(request);
    }
}
