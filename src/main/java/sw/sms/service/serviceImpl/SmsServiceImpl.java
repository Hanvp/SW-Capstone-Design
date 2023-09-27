package sw.sms.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sw.sms.feignClient.NaverFeignClient;
import sw.sms.feignClient.dto.requestDto.NaverFeignRequestDto;
import sw.sms.feignClient.dto.responseDto.NaverFeignResponseDto;
import sw.sms.service.SmsService;
import sw.sms.web.dto.requestDto.SmsRequestDto;
import sw.sms.web.dto.responseDto.SmsReponseDto;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static sw.sms.feignClient.converter.SmsConverter.toSmsReqeustDto;
import static sw.sms.feignClient.converter.SmsConverter.toSmsSendResultDto;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SmsServiceImpl implements SmsService {

    private final NaverFeignClient naverFeignClient;

    @Value("${naver.send.phoneNum}")
    String from;

    @Override
    public SmsReponseDto.SmsSendResultDto sendSms(SmsRequestDto.SmsSendInfo request) {


        NaverFeignRequestDto.SmsRequestDto smsRequestDto = toSmsReqeustDto(request, from);
        NaverFeignResponseDto.SmsResponseDto response = naverFeignClient.sendSms(smsRequestDto);
        return toSmsSendResultDto(request,response);
    }
}
