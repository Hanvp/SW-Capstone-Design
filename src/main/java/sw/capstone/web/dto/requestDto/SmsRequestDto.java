package sw.capstone.web.dto.requestDto;

import lombok.Getter;

public class SmsRequestDto {

    @Getter
    public static class request{
        private String targetPhoneNum;
    }
}
