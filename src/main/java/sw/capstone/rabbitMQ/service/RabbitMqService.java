package sw.capstone.rabbitMQ.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
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
import java.io.IOException;
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

    List<Long> produceTime = new ArrayList<>();
    List<Long> consumeTime = new ArrayList<>();

    List<Long> producerToBroker = new ArrayList<>();
    List<Long> brokerToConsumer = new ArrayList<>();

    private String setContext(String randomNum) {
        Context context = new Context();
        context.setVariable("code", randomNum);
        return templateEngine.process("email",context);
    }

    @RabbitListener(queues = "${spring.rabbitmq.queue.email}")
    public void receiveMessage(Message message) throws IOException {

        Long now = System.currentTimeMillis();

        ObjectMapper objectMapper = new ObjectMapper();
        String info = message.getBody().toString();

        Long sendTime = Long.parseLong(message.getMessageProperties().getHeaders().get("claimTime").toString());

        log.info(message.getMessageProperties().toString());

        Long brokerTime = message.getMessageProperties().getTimestamp().getTime();


        Long differ = now - sendTime;

        produceTime.add(sendTime);
        consumeTime.add(now);

        producerToBroker.add(brokerTime - sendTime);
        brokerToConsumer.add(now-brokerTime);

        result.add(differ);
        count.getAndIncrement();

    }

    public void getLog(int size) {
        Collections.sort(result);

        if(size > 1) {
            produceTime.remove(0);
            consumeTime.remove(0);
            producerToBroker.remove(0);
            brokerToConsumer.remove(0);

            Collections.sort(produceTime);
            Collections.sort(consumeTime);
            Collections.sort(producerToBroker);
            Collections.sort(brokerToConsumer);
        }

        log.info("전체 소요 시간: "+ (consumeTime.get(size-1) - produceTime.get(0)));

        if(size > 1) {
            Long produceTermSum = 0L;
            Long consumeTermSum = 0L;

            for (Integer i = 1; i < size; i++)
                produceTermSum += produceTime.get(i) - produceTime.get(i - 1);

            for (Integer i = 1; i < size; i++)
                consumeTermSum += consumeTime.get(i) - consumeTime.get(i - 1);

            log.info("Produce Term 최소 소요시간: " + (produceTime.get(1) - produceTime.get(0)));
            log.info("Produce Term 평균 소요시간: " + produceTermSum / (size * 1.0 - 1));
            log.info("Produce Term 최대 소요시간: " + (produceTime.get(size - 1) - produceTime.get(size - 2)));

            log.info("Consume Term 최소 소요시간: " + (consumeTime.get(1) - consumeTime.get(0)));
            log.info("Consume Term 평균 소요시간: " + consumeTermSum / (size * 1.0 - 1));
            log.info("Consume Term 최대 소요시간: " + (consumeTime.get(size - 1) - consumeTime.get(size - 2)));
            log.info("");
        }

        Long produceSum = 0L;
        Long consumeSum = 0L;

        for (Long value : producerToBroker)
            produceSum += value;

        for (Long value : brokerToConsumer)
            consumeSum += value;

        log.info("Producer -> Broker 최소 소요시간: " + producerToBroker.get(0));
        log.info("Producer -> Broker 평균 소요시간: "+ produceSum / (size*1.0));
        log.info("Producer -> Broker 최대 소요시간: " + producerToBroker.get(size-1));

        log.info("Broker -> Consumer 최소 소요시간: " + brokerToConsumer.get(0));
        log.info("Broker -> Consumer 평균 소요시간: "+ consumeSum / (size*1.0));
        log.info("Broker -> Consumer 최대 소요시간: " + brokerToConsumer.get(size-1));


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

        Collections.sort(jitter);

        double sector = Math.max(jitter.get(size - 1), Math.abs(jitter.get(0))) / 10.0;

        List<Long> jitterGraph = new ArrayList<>(Collections.nCopies(20, 0L));

        int index = 0;

        double first = (-1)*sector*10;

        log.info("Min jitter value: "+ jitter.get(0));
        log.info("Max jitter value: "+ jitter.get(size-1));
        for (int i = 0; i < 20; i++) {

            double start = first + sector*i;
            double end = start + sector;

            while (index < size && jitter.get(index) < end) {
                if(jitter.get(index) >= start) {
                    jitterGraph.set(i, jitterGraph.get(i) + 1L);
                    index++;
                }
            }
            log.info((i*5)+"%구간: "+jitterGraph.get(i)/(size*1.0)*100+"%");
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

        uri = "http://" + ip + ":8080/produce";

        List<Long> produceTimeList = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToFlux(Long.class)
                .collectList()
                .block();

        uri = "http://" + ip + ":8080/consume";

        List<Long> consumeTimeList = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToFlux(Long.class)
                .collectList()
                .block();

        uri = "http://" + ip + ":8080/toBroker";

        List<Long> toBrokerTimeList = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToFlux(Long.class)
                .collectList()
                .block();

        uri = "http://" + ip + ":8080/toConsumer";

        List<Long> toConsumerTimeList = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToFlux(Long.class)
                .collectList()
                .block();

        result.addAll(returnValue);
        produceTime.addAll(produceTimeList);
        consumeTime.addAll(consumeTimeList);
        producerToBroker.addAll(toBrokerTimeList);
        brokerToConsumer.addAll(toConsumerTimeList);
        log.info("추가 후 size: " + result.size());
    }

    public List<Long> returnData() {
        return result;
    }

    public List<Long> returnProduce() {
        return produceTime;
    }

    public List<Long> returnConsume() {
        return consumeTime;
    }

    public List<Long> returnToBrokerTime() {
        return producerToBroker;
    }

    public List<Long> returnConsumerTime() {
        return brokerToConsumer;
    }
}
