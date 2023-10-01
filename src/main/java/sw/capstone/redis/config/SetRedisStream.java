package sw.capstone.redis.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sw.capstone.redis.util.RedisOperator;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
public class SetRedisStream {

    @Value("${spring.redis.key}")
    private String streamKey;

    @Value("${spring.redis.group.sms}")
    private String smsGroup;

    @Value("${spring.redis.group.email}")
    private String emailGroup;

    @Value("${spring.redis.group.fcm}")
    private String fcmGroup;


    private final RedisOperator redisOperator;

    @PostConstruct
    public void init() {
        redisOperator.createStreamConsumerGroup(streamKey, smsGroup);
        redisOperator.createStreamConsumerGroup(streamKey, emailGroup);
        redisOperator.createStreamConsumerGroup(streamKey, fcmGroup);
    }
}
