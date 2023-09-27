package sw.capstone.web.dto.requestDto;

import lombok.Getter;

public class EmailRequestDto {

    @Getter
    public static class EmailSendInfo{
        private String targetEmail;
        private String randomNum;
    }
}
