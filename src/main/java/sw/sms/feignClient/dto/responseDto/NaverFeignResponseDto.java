package sw.sms.feignClient.dto.responseDto;

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
    }
}
