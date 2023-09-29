package sw.capstone.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import sw.capstone.web.dto.reponseDto.FcmResponseDto;
import sw.capstone.web.dto.requestDto.FcmRequestDto;

public interface FcmService {
    FcmResponseDto.FcmSendResultDto sendFcm(FcmRequestDto.FcmSendInfo request) throws JsonProcessingException;
}

