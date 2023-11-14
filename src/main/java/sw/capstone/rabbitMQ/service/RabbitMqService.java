package sw.capstone.rabbitMQ.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import sw.capstone.converter.EmailConverter;
import sw.capstone.rabbitMQ.dto.CheckRecordPer1000;
import sw.capstone.rabbitMQ.dto.EmailRabbitMQDto;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RabbitMqService {

//    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.auth-code-expiration-millis}")
    private Long authCodeExpirationMillis;

    List<Long> result = new ArrayList<>();
    AtomicInteger count= new AtomicInteger(0);


    private String setContext(String randomNum) {
        Context context = new Context();
        context.setVariable("code", randomNum);
        return templateEngine.process("email",context);
    }

    @RabbitListener(queues = "${spring.rabbitmq.queue.email}")
    public void receiveMessage(Map<String, Object> content) {

        Long now = System.currentTimeMillis();
        String sendTime = content.get("claimTime").toString();

//        log.info("Now: "+now+", Send Time: "+sendTime);

        Long differ = now - Long.parseLong(sendTime);

        result.add(differ);
        count.getAndIncrement();
    }

    public void getLog(int size) {
        Collections.sort(result);
//        result.sort((a, b) -> a.compareTo(b));

        int p50 = (int)(size * 0.5);
        int p90 = (int)(size * 0.9);
        int p99 = (int)(size * 0.99);
        int p999 = (int)(size * 0.999);
        int p9999 = (int)(size * 0.9999);

        log.info("p50, p90, p99, p999, p999: "+p50+", "+p90+", "+p99+", "+p999+", "+p9999);

        Long sum = 0L;

        //---- tail latency ----//
        for (int i = 0; i < p50; i++) {
            sum += result.get(i);
        }
        log.info("p50 Average Tail Latency: "+ sum /(p50* 1.0));
        log.info("p50 Max Tail Latency: "+ result.get(p50-1));

        for (int i = p50+1; i < p90; i++) {
            sum += result.get(i);
        }
        log.info("p90 Average Tail Latency: "+ sum /(p90* 1.0));
        log.info("p90 Max Tail Latency: "+ result.get(p90-1));


        for (int i = p90+1; i < p99; i++) {
            sum += result.get(i);
        }
        log.info("p99 Average Tail Latency: "+ sum /(p99* 1.0));
        log.info("p99 Max Tail Latency: "+ result.get(p99-1));


        for (int i = p99+1; i < p999; i++) {
            sum += result.get(i);
        }
        log.info("p999 Average Tail Latency: "+ sum /(p999* 1.0));
        log.info("p999 Max Tail Latency: "+ result.get(p999-1));


        for (int i = p999+1; i < p9999; i++) {
            sum += result.get(i);
        }
        log.info("p9999 Average Tail Latency: "+ sum /(p9999* 1.0));
        log.info("p9999 Max Tail Latency: "+ result.get(p9999-1));


        for (int i = p9999+1; i < size; i++) {
            sum += result.get(i);
        }

        //---- Jitter ----//
        List<Double> jitter = new ArrayList<>();

        double average = sum / (size * 1.0);
        log.info("Average: "+ average);

        for (int i = 0; i < size; i++) {
            jitter.add(result.get(i) - average);
        }

        List<Long> jitterGraph = new ArrayList<>(Collections.nCopies(20, 0L));

        Collections.sort(jitter);
//        Double firstValue = jitter.get(0);
//        Double lastValue = jitter.get(jitter.size() - 1);
//        Double differ = lastValue-firstValue;
//        double section = differ / 100.0;
//
//        log.info("differ: "+differ+", 1% section: "+section);

        int index = 0;

        for (int i = 0; i < 20; i++) {
//            double start = firstValue + section * i;
//            double end = start + section;
            double start = i - 7;
            double end = start + 1;

            while (jitter.get(index) < end) {
                if(jitter.get(index) >= start) {
                    jitterGraph.set(i, jitterGraph.get(i) + 1L);
                    index++;
                }
            }

            log.info(start+"~"+end+"구간: "+jitterGraph.get(i)/(size*1.0)*100+"%");
        }
    }

    public void addData(String ip) {

        log.info("추가 전 size: " + result.size());

        String uri = "http://" + ip + ":8080/data";

        WebClient webClient = WebClient.create();
        List<Long> returnValue = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToFlux(Long.class)
                .collectList()
                .block();

        result.addAll(returnValue);
        log.info("추가 후 size: " + result.size());
    }

    public List<Long> returnData() {
        return result;
    }
}
