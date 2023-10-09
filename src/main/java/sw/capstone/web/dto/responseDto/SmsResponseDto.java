package sw.capstone.web.dto.responseDto;

import lombok.*;

public class SmsResponseDto {

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class SmsResultDto{
        private String phoneNum;
        private String succeed;
    }
}
