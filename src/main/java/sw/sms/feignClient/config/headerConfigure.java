package sw.sms.feignClient.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Configuration
public class headerConfigure {

    Long timestamp = System.currentTimeMillis();
    String now = timestamp.toString();

    @Value("${naver.access.key}")
    String accessKey;

    @Value("${naver.secret.key}")
    String secretKey;

    @Value("${naver.service.id}")
    String serviceId;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Content-Type", "application/json");
            requestTemplate.header("x-ncp-apigw-timestamp", now);
            requestTemplate.header("x-ncp-iam-access-key", accessKey);
            requestTemplate.header("x-ncp-apigw-signature-v2", makeSignature(timestamp, accessKey, secretKey, serviceId));
        };
    }

    public String makeSignature(Long epoch, String naverAccessKey, String naverSecretKey, String serviceId) {
        String space = " ";					// one space
        String newLine = "\n";					// new line
        String method = "POST";					// method
        String url = "/sms/v2/services/"+serviceId+"/messages";	// url (include query string)
        String timestamp = epoch.toString();			// current timestamp (epoch)
        String accessKey = naverAccessKey;			// access key id (from portal or Sub Account)
        String secretKey = naverSecretKey;

        String message = new StringBuilder()
                .append(method)
                .append(space)
                .append(url)
                .append(newLine)
                .append(timestamp)
                .append(newLine)
                .append(accessKey)
                .toString();

        SecretKeySpec signingKey = null;
        try {
            signingKey = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        Mac mac = null;
        try {
            mac = Mac.getInstance("HmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        try {
            mac.init(signingKey);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }

        byte[] rawHmac = new byte[0];
        try {
            rawHmac = mac.doFinal(message.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        String encodeBase64String = Base64.encodeBase64String(rawHmac);

        return encodeBase64String;
    }
}
