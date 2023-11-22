package sw.capstone.redis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

//@Configuration
//@EnableAsync(proxyTargetClass = true)
public class AsyncConfig {

//    @Bean(name = "threadPoolTaskExecutor")
    public Executor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(3);
        taskExecutor.setMaxPoolSize(8);
        taskExecutor.setQueueCapacity(1000);
        taskExecutor.setThreadNamePrefix("Executor-");
        return taskExecutor;
    }
}
