package sw.capstone.web.dto.responseDto;

import lombok.*;

public class BasicResponseDto {

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ResultDto{
        private String succeed;
    }
}
