package sw.capstone.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import sw.capstone.web.dto.responseDto.BasicResponseDto;
import sw.capstone.web.dto.responseDto.EmailResponseDto;
import sw.capstone.web.dto.responseDto.FcmResponseDto;
import sw.capstone.web.dto.responseDto.SmsResponseDto;

public interface NotificationService {
    FcmResponseDto.FcmResultDto sendFcmWorker(Long memberId);

    SmsResponseDto.SmsResultDto sendSmsRedisWorker(Long memberId);

    SmsResponseDto.SmsResultDto sendSmsMqWorker(Long memberId);

    EmailResponseDto.EmailResultDto sendEmailRedisWorker(Long memberId);

    EmailResponseDto.EmailResultDto sendEmailMqWorker(Long memberId);

    EmailResponseDto.EmailResultDto sendEmailKafkaWorker(Long memberId) throws JsonProcessingException;

    void getLog();

    BasicResponseDto.ResultDto batchEmailRedisWorker(byte[] content);
    BasicResponseDto.ResultDto batchEmailMqWorker(byte[] content);
    BasicResponseDto.ResultDto batchEmailKafkaWorker(byte[] content) throws JsonProcessingException;
}
