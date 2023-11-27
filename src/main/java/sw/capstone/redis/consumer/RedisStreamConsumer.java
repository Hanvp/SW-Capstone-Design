package sw.capstone.redis.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import sw.capstone.converter.EmailConverter;
import sw.capstone.redis.dto.EmailRedisStream;
import sw.capstone.redis.util.RedisOperator;
import sw.capstone.web.dto.CheckRecordPer1000;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
//@Component
@Service
@RequiredArgsConstructor
public class RedisStreamConsumer implements StreamListener<String, MapRecord<String, Object, Object>>, InitializingBean, DisposableBean {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.auth-code-expiration-millis}")
    private Long authCodeExpirationMillis;

    private StreamMessageListenerContainer<String, MapRecord<String, Object, Object>> listenerContainer;
    private Subscription subscription;
    private String streamKey;
    private String consumerGroupName;
    private String consumerName;

    // 위에 구현한 Redis Streamd에 필요한 기본 Command를 구현한 Component
    private final RedisOperator redisOperator;

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

//    @Async("threadPoolTaskExecutor")
    @Override
    public void onMessage(MapRecord<String, Object, Object> record) {
        // 처리할 로직 구현

            Long now = System.currentTimeMillis();
            Long sendTime = Long.parseLong((String) record.getValue().get("claimTime"));

            Long brokerTime = record.getId().getTimestamp();

//        log.info("Now: "+now+", Send Time: "+sendTime);

            Long differ = now - sendTime;

            produceTime.add(sendTime);
            consumeTime.add(now);

            producerToBroker.add(brokerTime - sendTime);
            brokerToConsumer.add(now-brokerTime);

            result.add(differ);
            count.getAndIncrement();

            // 이후, ack stream
            this.redisOperator.ackStream("email", record);

        if(count.get() % 10000 == 0)
            log.info(LocalDateTime.now().toString());

    }

    @Override
    public void destroy() throws Exception {
        if(this.subscription != null){
            this.subscription.cancel();
        }
        if(this.listenerContainer != null){
            this.listenerContainer .stop();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Stream 기본 정보
        this.streamKey = "stream:email";
        this.consumerGroupName = "email";
        this.consumerName = UUID.randomUUID().toString().substring(5);

        // Consumer Group 설정
        this.redisOperator.createStreamConsumerGroup(streamKey, consumerGroupName);

        // StreamMessageListenerContainer 설정
        this.listenerContainer = this.redisOperator.createStreamMessageListenerContainer();

        //Subscription 설정
        this.subscription = this.listenerContainer.receive(
                Consumer.from(this.consumerGroupName, consumerName),
                StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
                this
        );

        // 0.01밀리초 마다, 정보 GET
//        this.subscription.await(Duration.ofNanos(10000));

        // redis listen 시작
        this.listenerContainer.start();
    }

    public void getLog(int size) {

        Collections.sort(result);
//        result.sort((a, b) -> a.compareTo(b));

        Collections.sort(produceTime);
        Collections.sort(consumeTime);
        Collections.sort(producerToBroker);
        Collections.sort(brokerToConsumer);

        produceTime.remove(size);
        consumeTime.remove(size);
        producerToBroker.remove(size);
        brokerToConsumer.remove(size);

        log.info("전체 소요 시간: "+ (consumeTime.get(size-1) - produceTime.get(0)));

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
