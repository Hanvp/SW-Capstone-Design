package sw.capstone.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sw.capstone.rabbitMQ.service.RabbitMqService;
import sw.capstone.service.EmailService;
import sw.capstone.web.dto.requestDto.EmailRequestDto;
import sw.capstone.web.dto.responseDto.EmailResponseDto;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
public class RootRestController {

    private final EmailService emailService;
    private final RabbitMqService rabbitMqService;

    @GetMapping("/health")
    public String healthAPi(){
        return "i'm healthy";
    }

    @PostMapping("/email")
    public EmailResponseDto.EmailSendResultDto sendToEmailApi(@RequestBody EmailRequestDto.EmailSendInfo request) {
        return emailService.sendEmail(request);
    }

    @GetMapping("/log")
    public void getCount() {
        rabbitMqService.getLog();
    }
}
