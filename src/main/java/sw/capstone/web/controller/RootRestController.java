package sw.capstone.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import sw.capstone.rabbitMQ.service.RabbitMqService;
import sw.capstone.service.EmailService;
import sw.capstone.web.dto.requestDto.EmailRequestDto;
import sw.capstone.web.dto.responseDto.EmailResponseDto;

import java.util.List;

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

    @GetMapping("/log/{size}")
    public void getCount(@PathVariable int size) {
        rabbitMqService.getLog(size);
    }

    @PostMapping("/data/{ip}")
    public void addData(@PathVariable String ip) {
        rabbitMqService.addData(ip);
    }

    @GetMapping("/data")
    public List<Long> returnData() {
        return rabbitMqService.returnData();
    }

    @GetMapping("/produce")
    public List<Long> returnProduce() {
        return rabbitMqService.returnProduce();
    }

    @GetMapping("/consume")
    public List<Long> returnConsume() {
        return rabbitMqService.returnConsume();
    }

    @GetMapping("/toBroker")
    public List<Long> returnToBrokerTime() {
        return rabbitMqService.returnToBrokerTime();
    }

    @GetMapping("/toConsumer")
    public List<Long> returnConsumerTime() {
        return rabbitMqService.returnConsumerTime();
    }
}
