package sw.sms.feignClient.dto.responseDto;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

public class NaverFeignResponseDto {

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class SmsResponseDto{
        private String requestId;
        private String requestTime;
        private String statusCode;
        private String statusName;

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
