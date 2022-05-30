package io.github.stavshamir.springwolf.asyncapi.scanners.channels;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.util.StringValueResolver;

import java.util.Arrays;
import java.util.Objects;

@Slf4j
public class RabbitListenerChannelNameMapper implements ChannelNameMapper<RabbitListener>, EmbeddedValueResolverAware {

    private static final String NO_QUEUE_FOUND_MESSAGE = "No queue name was found in @RabbitListener annotation";

    private StringValueResolver resolver;

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public String mapToChannelName(RabbitListener annotation) {
        if (annotation.queues().length > 0) {
            return getChannelNameFromQueues(annotation);
        }

        return getChannelNameFromBindings(annotation);
    }

    private String getChannelNameFromQueues(RabbitListener annotation) {
        return Arrays.stream(annotation.queues())
                .map(resolver::resolveStringValue)
                .peek(queue -> log.debug("Resolved queue name: {}", queue))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(NO_QUEUE_FOUND_MESSAGE));
    }

    private String getChannelNameFromBindings(RabbitListener annotation) {
        return Arrays.stream(annotation.bindings())
                .map(binding -> binding.value().name())
                .map(resolver::resolveStringValue)
                .peek(queue -> log.debug("Resolved queue name: {}", queue))
                .filter(Objects::nonNull)
                .filter(name -> !name.isEmpty())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(NO_QUEUE_FOUND_MESSAGE));
    }

}
