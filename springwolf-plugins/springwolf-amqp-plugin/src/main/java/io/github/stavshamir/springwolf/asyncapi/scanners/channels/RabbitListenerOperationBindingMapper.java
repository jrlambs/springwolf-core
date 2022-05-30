package io.github.stavshamir.springwolf.asyncapi.scanners.channels;

import com.asyncapi.v2.binding.OperationBinding;
import com.asyncapi.v2.binding.amqp.AMQPOperationBinding;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class RabbitListenerOperationBindingMapper implements OperationBindingMapper<RabbitListener> {

    @Override
    public Map<String, ? extends OperationBinding> mapToOperationBinding(RabbitListener annotation) {
        return ImmutableMap.of("amqp", new AMQPOperationBinding());
    }

}
