package sw.capstone.web.dto.responseDto;

import lombok.*;

public class EmailResponseDto {

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class EmailSendResultDto{
        private String email;
        private String randomNum;
        private String succeed;
    }
}
