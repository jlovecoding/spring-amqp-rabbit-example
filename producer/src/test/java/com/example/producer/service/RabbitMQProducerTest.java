package com.example.producer.service;


import com.example.producer.SpringRabbitExProducerApplication;
import com.example.producer.model.MatchResult;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Delivery;
import io.vavr.control.Try;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static com.example.producer.config.QueueConfig.QUEUE_NAME;
import static java.lang.Integer.valueOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = RabbitMQProducerTest.Initializer.class,
        classes = SpringRabbitExProducerApplication.class)
public class RabbitMQProducerTest {

    @Autowired
    private MatchResultService matchResultService;

    @ClassRule
    public static GenericContainer queue = new GenericContainer("rabbitmq").withExposedPorts(5672);

    @Value("${spring.rabbitmq.host}")
    private String queueHost;

    @Value("${spring.rabbitmq.port}")
    private String queuePort;

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

    @Test
    public void shouldSendMessage() throws Exception {
        //Given
        MatchResult matchResult = MatchResult.builder().localTeam("A").visitorTeam("B").result("1-3").build();
        prepareQueueConsumer(matchResult);

        //When && Then
        matchResultService.createMatchResult(matchResult);

    }

    private void prepareQueueConsumer(MatchResult matchResult) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(queueHost);
        factory.setPort(valueOf(queuePort));
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.basicConsume(QUEUE_NAME, true,
                (consumerTag, delivery) -> Try.run(() ->
                        assertReceivedMessage(delivery, matchResult))
                        .onFailure(throwable -> fail(throwable.getMessage(), throwable)),
                consumerTag -> {
                    fail("Cancel callback");
                });
    }

    public void assertReceivedMessage(final Delivery delivery, final MatchResult expected) throws Exception {
        assertThat(delivery.getBody()).isNotNull();
        String receivedJson = new String(delivery.getBody(), "UTF-8");
        MatchResult received = new ObjectMapper().readValue(receivedJson, MatchResult.class);
        assertThat(received.getLocalTeam()).isEqualTo(expected.getLocalTeam());
        assertThat(received.getVisitorTeam()).isEqualTo(expected.getVisitorTeam());
        assertThat(received.getResult()).isEqualTo(expected.getResult());
    }
}
