package sw.sms.redis.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.stereotype.Component;
import sw.sms.feignClient.NaverFeignClient;
import sw.sms.feignClient.dto.requestDto.NaverFeignRequestDto;
import sw.sms.feignClient.dto.responseDto.NaverFeignResponseDto;
import sw.sms.redis.dto.RedisSmsDto;
import sw.sms.redis.util.RedisOperator;

import java.time.Duration;

import static sw.sms.feignClient.converter.SmsConverter.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStreamConsumer implements StreamListener<String, MapRecord<String, Object, Object>>, InitializingBean, DisposableBean {

    private StreamMessageListenerContainer<String, MapRecord<String, Object, Object>> listenerContainer;
    private Subscription subscription;
    private String streamKey;
    private String consumerGroupName;
    private String consumerName;

    // 위에 구현한 Redis Streamd에 필요한 기본 Command를 구현한 Component
    private final RedisOperator redisOperator;

    private final NaverFeignClient naverFeignClient;

    @Value("${naver.send.phoneNum}")
    String from;

    @Override
    public void onMessage(MapRecord<String, Object, Object> record) {
        String entry = record.getId().toString();

        // 처리할 로직 구현
        NaverFeignRequestDto.SmsRequestDto smsRequestDto = toSmsRequestDto(record.getValue().get(entry), from);
        NaverFeignResponseDto.SmsResponseDto response = naverFeignClient.sendSms(smsRequestDto);

        // 이후, ack stream
        this.redisOperator.ackStream("sms", record);
    }

    @Override
    public void destroy() throws Exception {
        if(this.subscription != null){
            this.subscription.cancel();
        }
        if(this.listenerContainer != null){
            this.listenerContainer .stop();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Stream 기본 정보
        this.streamKey = "swCapstoneStream";
        this.consumerGroupName = "sms";
        this.consumerName = "sms1";

        // Consumer Group 설정
        this.redisOperator.createStreamConsumerGroup(streamKey, consumerGroupName);

        // StreamMessageListenerContainer 설정
        this.listenerContainer = this.redisOperator.createStreamMessageListenerContainer();

        //Subscription 설정
        this.subscription = this.listenerContainer.receive(
                Consumer.from(this.consumerGroupName, consumerName),
                StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
                this
        );

        // 1초 마다, 정보 GET
        this.subscription.await(Duration.ofSeconds(1));

        // redis listen 시작
        this.listenerContainer.start();
    }
}
