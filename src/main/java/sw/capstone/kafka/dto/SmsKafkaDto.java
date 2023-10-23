package sw.capstone.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SmsKafkaDto implements Serializable {

    private String targetPhoneNum;
    private String randomNum;
}
