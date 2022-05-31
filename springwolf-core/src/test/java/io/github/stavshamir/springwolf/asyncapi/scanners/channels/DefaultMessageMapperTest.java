package io.github.stavshamir.springwolf.asyncapi.scanners.channels;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.github.stavshamir.springwolf.asyncapi.types.channel.operation.message.Message;
import io.github.stavshamir.springwolf.asyncapi.types.channel.operation.message.PayloadReference;
import io.github.stavshamir.springwolf.schemas.DefaultSchemasService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.List;

import static io.github.stavshamir.springwolf.asyncapi.Constants.ONE_OF;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        DefaultMessageMapper.class,
        DefaultSchemasService.class
})
public class DefaultMessageMapperTest {

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void nullList() {
        List<Class<?>> nullList = null;
        assertThatThrownBy(() -> messageMapper.mapToMessage(nullList))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void nullPayloadType() {
        Class<?> nullClass = null;
        assertThatThrownBy(() -> messageMapper.mapToMessage(nullClass))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void emptyList() {
        assertThatThrownBy(() -> messageMapper.mapToMessage(Collections.emptyList()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void listWithOnePayloadType() {
        Class<String> payloadType = String.class;
        assertThat(messageMapper.mapToMessage(payloadType))
                .isEqualTo(messageMapper.mapToMessage(ImmutableList.of(payloadType)));
    }

    @Test
    public void listWithMultiplePayloadTypes() {
        List<Class<?>> payloadTypes = ImmutableList.of(String.class, Integer.class);
        ImmutableMap<String, ImmutableSet<Message>> expectedMessage = ImmutableMap.of(ONE_OF, ImmutableSet.of(
                messageMapper.mapToMessage(payloadTypes.get(0)),
                messageMapper.mapToMessage(payloadTypes.get(1))
        ));
        assertThat(messageMapper.mapToMessage(payloadTypes))
                .isEqualTo(expectedMessage);
    }

    // TODO one not in list

    @Test
    public void onePayloadTypeNotInList() {
        Message message = messageMapper.mapToMessage(String.class);

        Message expectedMessage = Message.builder()
                .name("java.lang.String")
                .title("String")
                .payload(PayloadReference.fromModelName("java.lang.String"))
                .build();

        assertThat(message)
                .isEqualTo(expectedMessage);
    }
}