package io.github.stavshamir.springwolf.asyncapi.scanners.channels;

import com.asyncapi.v2.binding.OperationBinding;
import com.asyncapi.v2.binding.kafka.KafkaOperationBinding;
import com.google.common.collect.ImmutableMap;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.util.StringValueResolver;

import java.util.Map;

@Slf4j
@Service
public class KafkaOperationBindingMapper implements OperationBindingMapper<KafkaListener>, EmbeddedValueResolverAware {

    private StringValueResolver resolver;

    @Override
    public void setEmbeddedValueResolver(@NonNull StringValueResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public Map<String, ? extends OperationBinding> mapToOperationBinding(KafkaListener annotation) {
        String groupId = resolver.resolveStringValue(annotation.groupId());
        if (groupId == null || groupId.isEmpty()) {
            log.debug("No group ID found for this listener");
            groupId = null;
        } else {
            log.debug("Found group id: {}", groupId);
        }

        KafkaOperationBinding binding = new KafkaOperationBinding();
        binding.setGroupId(groupId);
        return ImmutableMap.of("kafka", binding);
    }

}
