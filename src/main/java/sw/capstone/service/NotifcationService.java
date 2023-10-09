package sw.capstone.service;

import sw.capstone.web.dto.requestDto.FcmRequestDto;
import sw.capstone.web.dto.requestDto.SmsRequestDto;
import sw.capstone.web.dto.responseDto.FcmResponseDto;
import sw.capstone.web.dto.responseDto.SmsResponseDto;

public interface NotifcationService {
    FcmResponseDto.FcmResultDto sendFcmWorker(FcmRequestDto.request request);

    SmsResponseDto.SmsResultDto sendSmsWorker(SmsRequestDto.request request);
}
