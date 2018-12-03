package com.example.producer.service;

import com.example.producer.model.MatchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import static com.example.producer.config.QueueConfig.TOPIC_EXCHANGE_NAME;

@Service
@Slf4j
public class MatchResultService {

    private final RabbitTemplate rabbitTemplate;

    public MatchResultService(final RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public MatchResult createMatchResult(final MatchResult matchResult) {
        log.info("Sending message... " + matchResult.toString());
        rabbitTemplate.convertAndSend(TOPIC_EXCHANGE_NAME, "foo.bar.baz", matchResult);
        return matchResult;
    }

}
