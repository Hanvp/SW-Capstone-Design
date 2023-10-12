package sw.capstone.web.dto.responseDto;

import lombok.*;

public class EmailResponseDto {

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class EmailResultDto{
        private String email;
        private String succeed;
    }
}
