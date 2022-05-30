package io.github.stavshamir.springwolf.asyncapi.scanners.channels;

import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.util.StringValueResolver;

import java.util.Arrays;

public class KafkaListenerChannelNameMapper implements ChannelNameMapper<KafkaListener>, EmbeddedValueResolverAware {

    private StringValueResolver resolver;

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public String mapToChannelName(KafkaListener annotation) {
        return Arrays.stream(annotation.topics())
                .map(resolver::resolveStringValue)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No topic found in @KafkaListener " + annotation));
    }

}
