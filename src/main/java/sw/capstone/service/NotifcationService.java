package sw.capstone.service;

import sw.capstone.web.dto.requestDto.EmailRequestDto;
import sw.capstone.web.dto.requestDto.FcmRequestDto;
import sw.capstone.web.dto.requestDto.SmsRequestDto;
import sw.capstone.web.dto.responseDto.EmailResponseDto;
import sw.capstone.web.dto.responseDto.FcmResponseDto;
import sw.capstone.web.dto.responseDto.SmsResponseDto;

public interface NotifcationService {
    FcmResponseDto.FcmResultDto sendFcmWorker(FcmRequestDto.request request);

    SmsResponseDto.SmsResultDto sendSmsRedisWorker(SmsRequestDto.request request);

    SmsResponseDto.SmsResultDto sendSmsMqWorker(SmsRequestDto.request request);

    EmailResponseDto.EmailResultDto sendEmailRedisWorker(EmailRequestDto.request request);

    EmailResponseDto.EmailResultDto sendEmailMqWorker(EmailRequestDto.request request);
}
