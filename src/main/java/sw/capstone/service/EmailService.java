package sw.capstone.service;

import sw.capstone.web.dto.requestDto.EmailRequestDto;
import sw.capstone.web.dto.responseDto.EmailResponseDto;

public interface EmailService {
    EmailResponseDto.EmailSendResultDto sendEmail(EmailRequestDto.EmailSendInfo request);
}
