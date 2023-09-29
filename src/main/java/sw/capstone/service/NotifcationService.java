package sw.capstone.service;

import sw.capstone.web.dto.requestDto.FcmRequestDto;
import sw.capstone.web.dto.responseDto.FcmResponseDto;

public interface NotifcationService {
    FcmResponseDto.FcmResultDto sendFcmWorker(FcmRequestDto.request request);

}
