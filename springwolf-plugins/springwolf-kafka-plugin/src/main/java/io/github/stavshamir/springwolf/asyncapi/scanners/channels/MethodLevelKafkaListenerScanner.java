package io.github.stavshamir.springwolf.asyncapi.scanners.channels;

import com.asyncapi.v2.binding.OperationBinding;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.util.StringValueResolver;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
public class MethodLevelKafkaListenerScanner extends AbstractMethodLevelListenerScanner<KafkaListener>
        implements ChannelsScanner, EmbeddedValueResolverAware {

    @Autowired
    private KafkaOperationBindingMapper operationBindingMapper;

    private StringValueResolver resolver;

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    protected Class<KafkaListener> getListenerAnnotationClass() {
        return KafkaListener.class;
    }

    @Override
    protected String getChannelName(KafkaListener annotation) {
        List<String> resolvedTopics = Arrays.stream(annotation.topics())
                .map(resolver::resolveStringValue)
                .collect(toList());

        log.debug("Found topics: {}", String.join(", ", resolvedTopics));
        return resolvedTopics.get(0);
    }

    @Override
    protected Map<String, ? extends OperationBinding> buildOperationBinding(KafkaListener annotation) {
        return operationBindingMapper.mapToOperationBinding(annotation);
    }

}
