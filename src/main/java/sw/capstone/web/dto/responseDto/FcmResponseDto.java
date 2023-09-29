package sw.capstone.web.dto.responseDto;

import lombok.*;

public class FcmResponseDto {

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class FcmResultDto{
        private String phoneNum;
        private String succeed;
    }
}
