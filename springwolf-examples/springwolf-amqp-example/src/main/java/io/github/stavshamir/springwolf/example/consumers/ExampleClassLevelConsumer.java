package io.github.stavshamir.springwolf.example.consumers;

import io.github.stavshamir.springwolf.example.dtos.AnotherPayloadDto;
import io.github.stavshamir.springwolf.example.dtos.ExamplePayloadDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RabbitListener(queues = "multi-payload-queue")
public class ExampleClassLevelConsumer {

    private static final Logger logger = LoggerFactory.getLogger(ExampleClassLevelConsumer.class);

    @RabbitHandler
    public void receiveExamplePayload(ExamplePayloadDto payload) {
        logger.info("Received new ExamplePayloadDto message in example-queue: {}", payload.toString());
    }

    @RabbitHandler
    public void receiveAnotherPayload(AnotherPayloadDto payload) {
        logger.info("Received new AnotherPayloadDto message in example-queue: {}", payload.toString());
    }

}
