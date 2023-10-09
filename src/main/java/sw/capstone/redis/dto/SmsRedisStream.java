package sw.capstone.redis.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SmsRedisStream {

    private String targetPhoneNum;
    private String randomNum;

    public Map<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("targetPhoneNum", this.targetPhoneNum);
        map.put("randomNum", this.randomNum);
        return map;
    }


    @Override
    public String toString() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.toString();
    }

}
