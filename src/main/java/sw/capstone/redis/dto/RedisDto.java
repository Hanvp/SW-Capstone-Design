package sw.capstone.redis.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

public class RedisDto {

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class SmsRedisStream{
        private String targetPhoneNum;
        private String randomNum;

        @Override
        public String toString() {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.writeValueAsString(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return super.toString();
        }
    }
}
