package sw.sms.web.dto.requestDto;

import lombok.Getter;
import lombok.Setter;

public class SmsRequestDto {

    @Getter
    public static class SmsSendInfo{
        private String targetPhoneNum;
        private String randomNum;
    }
}
