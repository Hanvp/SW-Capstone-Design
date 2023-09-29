package sw.capstone.web.dto.requestDto;

import lombok.Getter;

public class FcmRequestDto {
    @Getter
    public static class FcmSendInfo{
        private String targetFcmToken;
        private String accessToken;
        private String title;
        private String body;
    }
}
