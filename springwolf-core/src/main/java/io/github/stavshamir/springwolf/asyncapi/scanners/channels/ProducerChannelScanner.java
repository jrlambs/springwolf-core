package io.github.stavshamir.springwolf.asyncapi.scanners.channels;

import com.asyncapi.v2.binding.OperationBinding;
import com.asyncapi.v2.model.channel.ChannelItem;
import com.asyncapi.v2.model.channel.operation.Operation;
import io.github.stavshamir.springwolf.asyncapi.types.ProducerData;
import io.github.stavshamir.springwolf.configuration.AsyncApiDocket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProducerChannelScanner implements ChannelsScanner {

    private final AsyncApiDocket docket;
    private final MessageMapper messageMapper;

    @Override
    public Map<String, ChannelItem> scan() {
        Map<String, List<ProducerData>> producerDataGroupedByChannelName = docket.getProducers().stream()
                .filter(this::allFieldsAreNonNull)
                .collect(groupingBy(ProducerData::getChannelName));

        return producerDataGroupedByChannelName.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, entry -> buildChannel(entry.getValue())));
    }

    private boolean allFieldsAreNonNull(ProducerData producerData) {
        boolean allNonNull = producerData.getChannelName() != null
                && producerData.getPayloadType() != null
                && producerData.getBinding() != null;

        if (!allNonNull) {
            log.warn("Some producer data fields are null - this producer will not be documented: {}", producerData);
        }

        return allNonNull;
    }

    private ChannelItem buildChannel(List<ProducerData> producerDataList) {
        // All bindings in the group are assumed to be the same
        // AsyncApi does not support multiple bindings on a single channel
        Map<String, ? extends OperationBinding> binding = producerDataList.get(0).getBinding();

        Operation operation = Operation.builder()
                .message(getMessage(producerDataList))
                .bindings(binding)
                .build();

        return ChannelItem.builder()
                .subscribe(operation)
                .build();
    }

    private Object getMessage(List<ProducerData> producerDataList) {
        List<? extends Class<?>> payloadTypes = producerDataList.stream()
                .map(ProducerData::getPayloadType)
                .collect(toList());

        return messageMapper.mapToMessage(payloadTypes);
    }

}