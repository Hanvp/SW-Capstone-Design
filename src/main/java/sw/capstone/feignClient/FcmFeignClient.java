package sw.capstone.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import sw.capstone.feignClient.dto.requestDto.FcmFeignRequestDto;

@FeignClient(name = "FcmFeignClient", url="https://fcm.googleapis.com/v1/projects")
public interface FcmFeignClient {

    @PostMapping(value = "/${oauth2.fcm.projectId}/messages:send")
    String sendFcm(@RequestBody String message,
                          @RequestHeader(name = "Authorization") String authorization,
                          @RequestHeader(name = "Content-Type") String contentType);
}
