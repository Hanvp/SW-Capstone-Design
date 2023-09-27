package sw.sms.feignClient.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import sw.sms.feignClient.dto.requestDto.NaverFeignRequestDto;
import sw.sms.feignClient.dto.responseDto.NaverFeignResponseDto;
import sw.sms.web.dto.requestDto.SmsRequestDto;
import sw.sms.web.dto.responseDto.SmsReponseDto;

import java.util.ArrayList;
import java.util.List;

import static sw.sms.feignClient.dto.requestDto.NaverFeignRequestDto.*;

@Slf4j
public class SmsConverter {


    public static NaverFeignRequestDto.SmsRequestDto toSmsReqeustDto(SmsRequestDto.SmsSendInfo request, String from){

        List<Message> messageList = new ArrayList<>();
        messageList.add(toMessage(request));

        return NaverFeignRequestDto.SmsRequestDto.builder()
                .type("SMS")
                .contentType("COMM")
                .countryCode("82")
                .from(from)
                .content("sw-capstone 인증 문자 테스트")
                .messages(messageList)
                .build();
    }

    public static Message toMessage(SmsRequestDto.SmsSendInfo request) {
        return Message.builder()
                .to(request.getTargetPhoneNum())
                .content("sw-capstone 인증 문자 테스트\n"+"["+request.getRandomNum()+"]")
                .build();
    }

    public static SmsReponseDto.SmsSendResultDto toSmsSendResultDto(SmsRequestDto.SmsSendInfo request, NaverFeignResponseDto.SmsResponseDto response){
        return SmsReponseDto.SmsSendResultDto.builder()
                .phoneNum(request.getTargetPhoneNum())
                .randomNum(request.getRandomNum())
                .succeed(response.getStatusName())
                .build();
    }
}
