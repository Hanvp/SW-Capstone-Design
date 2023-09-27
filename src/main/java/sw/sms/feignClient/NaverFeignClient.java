package sw.sms.feignClient;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import sw.sms.feignClient.config.headerConfigure;
import sw.sms.feignClient.dto.requestDto.NaverFeignRequestDto;
import sw.sms.feignClient.dto.responseDto.NaverFeignResponseDto;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@FeignClient(name="NaverFeignClient",url="https://sens.apigw.ntruss.com/sms/v2", configuration = headerConfigure.class)
public interface NaverFeignClient {

    @PostMapping(value= "/services/${naver.service.id}/messages")
    NaverFeignResponseDto.SmsResponseDto sendSms(@RequestBody NaverFeignRequestDto.SmsRequestDto request);


}
