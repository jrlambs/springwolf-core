package io.github.stavshamir.springwolf.asyncapi.scanners.channels;

import com.asyncapi.v2.binding.OperationBinding;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class MethodLevelRabbitListenerScanner extends AbstractMethodLevelListenerScanner<RabbitListener>
        implements ChannelsScanner {

    @Autowired
    private RabbitListenerChannelNameMapper channelNameMapper;

    @Autowired
    private RabbitListenerOperationBindingMapper operationBindingMapper;

    @Override
    protected Class<RabbitListener> getListenerAnnotationClass() {
        return RabbitListener.class;
    }

    @Override
    protected String getChannelName(RabbitListener annotation) {
        return channelNameMapper.mapToChannelName(annotation);
    }

    @Override
    protected Map<String, ? extends OperationBinding> buildOperationBinding(RabbitListener annotation) {
        return operationBindingMapper.mapToOperationBinding(annotation);
    }

}
