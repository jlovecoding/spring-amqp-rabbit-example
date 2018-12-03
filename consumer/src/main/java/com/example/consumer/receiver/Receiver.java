package com.example.consumer.receiver;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

import static java.util.Optional.ofNullable;

@Component
@Slf4j
public class Receiver implements MessageListener {

    private MessageConverter messageConverter;

    public Receiver(final MessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    @Override
    public void onMessage(final Message message) {
        ofNullable(message)
                .map(messageConverter::fromMessage)
                .ifPresentOrElse(matchResult -> log.info("Received <" + matchResult.toString() + ">"),
                        () -> log.warn("null message"));
    }
}
