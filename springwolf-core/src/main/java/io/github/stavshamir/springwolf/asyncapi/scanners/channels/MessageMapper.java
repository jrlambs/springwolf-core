package io.github.stavshamir.springwolf.asyncapi.scanners.channels;

import io.github.stavshamir.springwolf.asyncapi.types.channel.operation.message.Message;

import java.util.List;

public interface MessageMapper {

    Object mapToMessage(List<? extends Class<?>> payloadTypes);

    Message mapToMessage(Class<?> payloadType);

}
