package io.github.stavshamir.springwolf.asyncapi.scanners.channels;

import com.asyncapi.v2.binding.OperationBinding;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MethodLevelKafkaListenerScanner extends AbstractMethodLevelListenerScanner<KafkaListener>
        implements ChannelsScanner {

    @Autowired
    private KafkaListenerOperationBindingMapper operationBindingMapper;

    @Autowired
    private KafkaListenerChannelNameMapper channelNameMapper;

    @Override
    protected Class<KafkaListener> getListenerAnnotationClass() {
        return KafkaListener.class;
    }

    @Override
    protected String getChannelName(KafkaListener annotation) {
        return channelNameMapper.mapToChannelName(annotation);
    }

    @Override
    protected Map<String, ? extends OperationBinding> buildOperationBinding(KafkaListener annotation) {
        return operationBindingMapper.mapToOperationBinding(annotation);
    }

}
