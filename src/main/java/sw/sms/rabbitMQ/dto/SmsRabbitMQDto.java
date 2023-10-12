package sw.sms.rabbitMQ.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SmsRabbitMQDto {

    private String targetPhoneNum;
    private String randomNum;
}
