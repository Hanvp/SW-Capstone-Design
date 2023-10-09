package sw.sms.redis.dto;

import lombok.Getter;

public class RedisSmsDto {

    @Getter
    public static class SmsInfo{
        private String targetPhoneNum;
        private String randomNum;
    }
}
