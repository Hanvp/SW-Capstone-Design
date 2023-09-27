package sw.sms.web.dto.responseDto;

import lombok.*;

public class SmsReponseDto {

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class SmsSendResultDto{
        private String phoneNum;
        private String randomNum;
        private String succeed;
    }
}
