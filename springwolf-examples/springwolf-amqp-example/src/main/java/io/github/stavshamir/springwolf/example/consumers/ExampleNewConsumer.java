package io.github.stavshamir.springwolf.example.consumers;


import io.github.stavshamir.springwolf.example.dtos.ExamplePayloadDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class ExampleNewConsumer {
    @Bean
    public Consumer<ExamplePayloadDto> exampleNewConsumerMethod() {
        return t -> System.out.println(t.getClass());
    }
}
