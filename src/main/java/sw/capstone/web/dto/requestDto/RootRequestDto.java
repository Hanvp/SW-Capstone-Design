package sw.capstone.web.dto.requestDto;

import lombok.Getter;

public class RootRequestDto {

    @Getter
    public static class request{
        private String name;
        private String email;
        private String phoneNum;
    }
}
