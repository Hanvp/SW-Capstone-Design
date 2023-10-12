package sw.sms.rabbitMQ.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sw.sms.feignClient.NaverFeignClient;
import sw.sms.feignClient.dto.requestDto.NaverFeignRequestDto;
import sw.sms.feignClient.dto.responseDto.NaverFeignResponseDto;
import sw.sms.rabbitMQ.dto.SmsRabbitMQDto;

import static sw.sms.feignClient.converter.SmsConverter.toSmsRequestDto;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RabbitMqService {
    private final NaverFeignClient naverFeignClient;

    @Value("${naver.send.phoneNum}")
    String from;

    @RabbitListener(queues = "${spring.rabbitmq.queue.sms}")
    public void receiveMessage(SmsRabbitMQDto rabbitMQDto) {

        if (rabbitMQDto == null){
            log.error("정보가 안담겨옴");
        }
        else {
            log.info(rabbitMQDto.getTargetPhoneNum(), rabbitMQDto.getRandomNum());

            //처리할 로직 구현
            NaverFeignRequestDto.SmsRequestDto smsRequestDto = toSmsRequestDto(rabbitMQDto.getTargetPhoneNum(), rabbitMQDto.getRandomNum(), from);
            NaverFeignResponseDto.SmsResponseDto response = naverFeignClient.sendSms(smsRequestDto);

            log.info(response.toString());
        }
    }
}
