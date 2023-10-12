package sw.sms.redis.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.stereotype.Component;
import sw.sms.feignClient.NaverFeignClient;
import sw.sms.feignClient.dto.requestDto.NaverFeignRequestDto;
import sw.sms.feignClient.dto.responseDto.NaverFeignResponseDto;
import sw.sms.redis.dto.SmsRedisStream;
import sw.sms.redis.util.RedisOperator;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

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

        log.info(record.toString());

        String data = (String) record.getValue().get("info");

        Optional<SmsRedisStream> value = null;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            value = Optional.of(objectMapper.readValue(data, SmsRedisStream.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (value.get() == null){
            log.error("정보가 안담겨옴");
        }
        else {
            //처리할 로직 구현
            NaverFeignRequestDto.SmsRequestDto smsRequestDto = toSmsRequestDto(value.get().getTargetPhoneNum(), value.get().getRandomNum(), from);
            NaverFeignResponseDto.SmsResponseDto response = naverFeignClient.sendSms(smsRequestDto);

            log.info(response.toString());

            // 이후, ack stream
            this.redisOperator.ackStream("sms", record);
        }
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
        this.streamKey = "stream:sms";
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

        // 2초 마다, 정보 GET
        this.subscription.await(Duration.ofSeconds(2));

        // redis listen 시작
        this.listenerContainer.start();
    }
}
