package sw.capstone.web.dto.requestDto;

import lombok.*;

public class FcmRequestDto {

    @Getter
    public static class request{
        private String FcmToken;
        private String targetPhoneNum;
    }
}
