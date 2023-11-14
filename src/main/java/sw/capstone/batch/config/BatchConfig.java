package sw.capstone.batch.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sw.capstone.service.NotificationService;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final NotificationService notificationService;

    @Value("1000")
    private Integer chunkSize;

    @Value("1024")
    private int kb1;

    @Value("5120")
    private int kb5;

    @Value("51200")
    private int kb50;

    @Value("61440")
    private int kb60;


    @Bean
    public Job job() {
        Job job = jobBuilderFactory.get(UUID.randomUUID().toString().substring(5))
                .start(step())
                .build();

        return job;
    }

    @Bean
    public Step step() {
        return stepBuilderFactory.get("RedisJobStep")
                .<byte[], byte[]>chunk(chunkSize)
                .reader(itemReader())
                .writer(itemWriter())
                .build();
    }

    @StepScope
    @Bean
    public ListItemReader<byte[]> itemReader() {
        List<byte[]> content = new ArrayList<>();

//        byte[] byte1 = new byte[kb1]; //byte 수정
//        new SecureRandom().nextBytes(byte1);
//
//        content.add(byte1);
//
//        byte[] byte5 = new byte[kb5]; //byte 수정
//        new SecureRandom().nextBytes(byte5);
//
//        content.add(byte5);
//
//        byte[] byte50 = new byte[kb50]; //byte 수정
//        new SecureRandom().nextBytes(byte1);
//
//        content.add(byte50);
//
//        byte[] byte60 = new byte[kb60]; //byte 수정
//        new SecureRandom().nextBytes(byte60);
//
//        content.add(byte60);

        byte[] randomBytes = new byte[kb1]; //byte 수정
        new SecureRandom().nextBytes(randomBytes);

        for (int i = 0; i < 200000; i++) //횟수 수정
            content.add(randomBytes);

        return new ListItemReader<byte[]>(content);
    }

    @StepScope
    @Bean
    public ItemWriter<byte[]> itemWriter() {

        log.info("Pulished 1st records: " + LocalDateTime.now());

        return randomBytes -> randomBytes.forEach(randomByte -> notificationService.batchEmailRedisWorker(randomByte));
    }


}
