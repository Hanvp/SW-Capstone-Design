package sw.capstone.converter;

import lombok.extern.slf4j.Slf4j;
import sw.capstone.web.dto.requestDto.EmailRequestDto;
import sw.capstone.web.dto.responseDto.EmailResponseDto;

@Slf4j
public class EmailConverter {

    public static EmailResponseDto.EmailSendResultDto toEmailResultDto(EmailRequestDto.EmailSendInfo request, String succeed){
        return EmailResponseDto.EmailSendResultDto.builder()
                .email(request.getTargetEmail())
                .randomNum(request.getRandomNum())
                .succeed(succeed)
                .build();
    }
}
