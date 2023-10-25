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
public class EmailKafkaDto implements Serializable {

    private String targetEmail;
    private String randomNum;
}
