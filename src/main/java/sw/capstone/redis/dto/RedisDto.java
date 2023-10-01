package sw.capstone.redis.dto;

import lombok.*;

public class RedisDto {

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class SmsRedisStream{
        private String targetPhoneNum;
        private String randomNum;
    }
}
