package sw.capstone.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import sw.capstone.redis.consumer.RedisStreamConsumer;
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
    private final RedisStreamConsumer redisStreamConsumer;

    @GetMapping("/health")
    public String healthAPi() {
        return "i'm healthy";
    }

    @PostMapping("/email")
    public EmailResponseDto.EmailSendResultDto sendToEmailApi(@RequestBody EmailRequestDto.EmailSendInfo request) {
        return emailService.sendEmail(request);
    }

    @GetMapping("/log/{size}")
    public void getCount(@PathVariable int size) {
        redisStreamConsumer.getLog(size);
    }

    @PostMapping("/data/{ip}")
    public void addData(@PathVariable String ip) {
        redisStreamConsumer.addData(ip);
    }

    @GetMapping("/data")
    public List<Long> returnData() {
        return redisStreamConsumer.returnData();
    }

    @GetMapping("/produce")
    public List<Long> returnProduce() {
        return redisStreamConsumer.returnProduce();
    }

    @GetMapping("/consume")
    public List<Long> returnConsume() {
        return redisStreamConsumer.returnConsume();
    }

    @GetMapping("/toBroker")
    public List<Long> returnToBrokerTime() {
        return redisStreamConsumer.returnToBrokerTime();
    }

    @GetMapping("/toConsumer")
    public List<Long> returnConsumerTime() {
        return redisStreamConsumer.returnConsumerTime();
    }
}