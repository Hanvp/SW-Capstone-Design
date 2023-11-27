package sw.capstone.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import sw.capstone.kafka.consumer.KafkaConsumer;
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
    private final KafkaConsumer kafkaConsumer;

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
        kafkaConsumer.getLog(size);
    }

    @PostMapping("/data/{ip}")
    public void addData(@PathVariable String ip) {
        kafkaConsumer.addData(ip);
    }

    @GetMapping("/data")
    public List<Long> returnData() {
        return kafkaConsumer.returnData();
    }

    @GetMapping("/produce")
    public List<Long> returnProduce() {
        return kafkaConsumer.returnProduce();
    }

    @GetMapping("/consume")
    public List<Long> returnConsume() {
        return kafkaConsumer.returnConsume();
    }

    @GetMapping("/toBroker")
    public List<Long> returnToBrokerTime() {
        return kafkaConsumer.returnToBrokerTime();
    }

    @GetMapping("/toConsumer")
    public List<Long> returnConsumerTime() {
        return kafkaConsumer.returnConsumerTime();
    }
}
