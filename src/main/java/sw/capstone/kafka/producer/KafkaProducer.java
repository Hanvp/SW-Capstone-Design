package sw.capstone.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public Object send(String topic, Object dto) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        String jsonInString = objectMapper.writeValueAsString(dto);
        jsonInString += LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        kafkaTemplate.send(topic, jsonInString);
//        log.info("kafka producer sent: ", dto.toString());

        return dto;
    }
}
