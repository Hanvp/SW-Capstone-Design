package sw.sms.feignClient.dto.requestDto;

import lombok.*;

import java.util.List;

public class NaverFeignRequestDto {

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class SmsRequestDto{
        private String type;
        private String contentType;
        private String countryCode;
        private String from;
        private String content;
        private List<Message> messages;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Message{
        private String to;
        private String content;
    }
}
