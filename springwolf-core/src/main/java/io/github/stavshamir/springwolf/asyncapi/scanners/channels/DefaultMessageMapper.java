package io.github.stavshamir.springwolf.asyncapi.scanners.channels;

import com.google.common.collect.ImmutableMap;
import io.github.stavshamir.springwolf.asyncapi.types.channel.operation.message.Message;
import io.github.stavshamir.springwolf.asyncapi.types.channel.operation.message.PayloadReference;
import io.github.stavshamir.springwolf.schemas.SchemasService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static io.github.stavshamir.springwolf.asyncapi.Constants.ONE_OF;
import static java.util.stream.Collectors.toSet;

@Service
@RequiredArgsConstructor
public class DefaultMessageMapper implements MessageMapper {

    private final SchemasService schemasService;

    @Override
    public Object mapToMessage(List<? extends Class<?>> payloadTypes) {
        if (payloadTypes == null || payloadTypes.isEmpty()) {
            throw new IllegalArgumentException("Payload types cannot be null or empty");
        }

        Set<Message> messages = payloadTypes.stream()
                .map(this::mapToMessage)
                .collect(toSet());

        return messages.size() == 1
                ? messages.toArray()[0]
                : ImmutableMap.of(ONE_OF, messages);

    }

    @Override
    public Message mapToMessage(Class<?> payloadType) {
        if (payloadType == null) {
            throw new IllegalArgumentException("Payload type cannot be null");
        }

        String modelName = schemasService.register(payloadType);

        return Message.builder()
                .name(payloadType.getName())
                .title(modelName)
                .payload(PayloadReference.fromModelName(modelName))
                .build();
    }

}
