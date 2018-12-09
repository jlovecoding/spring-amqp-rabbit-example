package com.example.consumer.receiver;

import com.example.consumer.SpringRabbitExConsumerApplication;
import com.example.producer.model.MatchResult;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static com.example.consumer.config.QueueConfig.QUEUE_NAME;
import static java.lang.Integer.valueOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = RabbitMQConsumerTest.Initializer.class,
        classes = {SpringRabbitExConsumerApplication.class, RabbitMQConsumerTest.TestConfiguration.class})
public class RabbitMQConsumerTest {

    @ClassRule
    public static GenericContainer queue = new GenericContainer("rabbitmq").withExposedPorts(5672);

    @Value("${spring.rabbitmq.host}")
    private String queueHost;

    @Value("${spring.rabbitmq.port}")
    private String queuePort;

    @Autowired
    private Receiver receiver;

    @Captor
    ArgumentCaptor<Message> messageArgumentCaptor;

    @MockBean
    private MessageConverter messageConverter;


    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues values = TestPropertyValues.of(
                    "spring.rabbitmq.port=" + queue.getMappedPort(5672),
                    "spring.rabbitmq.username=guest",
                    "spring.rabbitmq.password=guest",
                    "spring.rabbitmq.host=localhost");
            values.applyTo(configurableApplicationContext);
        }
    }

    @Configuration
    static class TestConfiguration {
        @Bean
        @Primary
        public Receiver receiverSpy(final Receiver receiver) {
            return spy(receiver);
        }


    }

    @Test
    public void shouldSendMessage() throws Exception {
        //Given
        MatchResult matchResult = MatchResult.builder().localTeam("A").visitorTeam("B").result("1-3").build();

        //When
        sendMessage(matchResult);

        //Then
        verify(receiver, times(1)).onMessage(any(Message.class));
        verify(messageConverter, times(1)).fromMessage(messageArgumentCaptor.capture());
        MatchResult received = new ObjectMapper().readValue(new String(messageArgumentCaptor.getValue().getBody(),
                "UTF-8"), MatchResult.class);
        assertThat(received.getLocalTeam()).isEqualTo("A");
        assertThat(received.getVisitorTeam()).isEqualTo("B");
        assertThat(received.getResult()).isEqualTo("1-3");
    }

    private void sendMessage(MatchResult matchResult) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(queueHost);
        factory.setPort(valueOf(queuePort));
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.basicPublish("", QUEUE_NAME, null,
                new ObjectMapper().writeValueAsString(matchResult).getBytes());
    }


}