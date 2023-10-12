package sw.capstone.rabbitMQ.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Value("${spring.rabbitmq.host}")
    private String rabbitmqHost;

    @Value("${spring.rabbitmq.port}")
    private int rabbitmqPort;

    @Value("${spring.rabbitmq.username}")
    private String rabbitmqUsername;

    @Value("${spring.rabbitmq.password}")
    private String rabbitmqPassword;

    @Value("${spring.rabbitmq.exchange.sms}")
    private String smsExchange;

    @Value("${spring.rabbitmq.exchange.email}")
    private String emailExchange;

    @Value("${spring.rabbitmq.exchange.fcm}")
    private String fcmExchange;

    @Value("${spring.rabbitmq.queue.sms}")
    private String smsQueue;

    @Value("${spring.rabbitmq.queue.email}")
    private String emailQueue;

    @Value("${spring.rabbitmq.routing.fcm}")
    private String fcmQueue;

    @Value("${spring.rabbitmq.routing.sms}")
    private String smsRoutingKey;

    @Value("${spring.rabbitmq.routing.email}")
    private String emailRoutingKey;

    @Value("${spring.rabbitmq.routing.fcm}")
    private String fcmRoutingKey;

    /**
     * 지정된 큐 이름으로 Queue 빈을 생성
     *
     * @return Queue 빈 객체
     */
    @Bean
    public Queue smsQueue() {
        return new Queue(smsQueue);
    }

    @Bean
    public Queue emailQueue() {
        return new Queue(emailQueue);
    }

    @Bean
    public Queue fcmQueue() {
        return new Queue(fcmQueue);
    }

    /**
     * 지정된 익스체인지 이름으로 DirectExchange 빈을 생성
     *
     * @return TopicExchange 빈 객체
     */
    @Bean
    public DirectExchange smsExchange() {
        return new DirectExchange(smsExchange);
    }

    @Bean
    public DirectExchange emailExchange() {
        return new DirectExchange(emailExchange);
    }

    @Bean
    public DirectExchange fcmExchange() {
        return new DirectExchange(fcmExchange);
    }

    /**
     * 주어진 큐와 익스체인지를 바인딩하고 라우팅 키를 사용하여 Binding 빈을 생성
     *
     * @param queue    바인딩할 Queue
     * @param exchange 바인딩할 TopicExchange
     * @return Binding 빈 객체
     */
    @Bean
    public Binding smsBinding(Queue smsQueue, DirectExchange smsExchange) {
        return BindingBuilder.bind(smsQueue).to(smsExchange).with(smsRoutingKey);
    }

    @Bean
    public Binding emailBinding(Queue emailQueue, DirectExchange emailExchange) {
        return BindingBuilder.bind(emailQueue).to(emailExchange).with(emailRoutingKey);
    }

    @Bean
    public Binding fcmBinding(Queue fcmQueue, DirectExchange fcmExchange) {
        return BindingBuilder.bind(fcmQueue).to(fcmExchange).with(fcmRoutingKey);
    }

    /**
     * RabbitMQ 연결을 위한 ConnectionFactory 빈을 생성하여 반환
     *
     * @return ConnectionFactory 객체
     */
    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(rabbitmqHost);
        connectionFactory.setPort(rabbitmqPort);
        connectionFactory.setUsername(rabbitmqUsername);
        connectionFactory.setPassword(rabbitmqPassword);
        return connectionFactory;
    }

    /**
     * RabbitTemplate을 생성하여 반환
     *
     * @param connectionFactory RabbitMQ와의 연결을 위한 ConnectionFactory 객체
     * @return RabbitTemplate 객체
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        // JSON 형식의 메시지를 직렬화하고 역직렬할 수 있도록 설정
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

    /**
     * Jackson 라이브러리를 사용하여 메시지를 JSON 형식으로 변환하는 MessageConverter 빈을 생성
     *
     * @return MessageConverter 객체
     */
    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
