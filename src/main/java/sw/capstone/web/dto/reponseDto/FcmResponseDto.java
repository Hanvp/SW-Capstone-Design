package sw.capstone.web.dto.reponseDto;

import lombok.*;

public class FcmResponseDto {

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class FcmSendResultDto{
        private String fcmToken;
        private String title;
        private String succeed;
    }
}
