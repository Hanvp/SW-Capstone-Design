package sw.sms.service;

import sw.sms.web.dto.requestDto.SmsRequestDto;
import sw.sms.web.dto.responseDto.SmsReponseDto;

public interface SmsService {
    SmsReponseDto.SmsSendResultDto sendSms(SmsRequestDto.SmsSendInfo request);
}
