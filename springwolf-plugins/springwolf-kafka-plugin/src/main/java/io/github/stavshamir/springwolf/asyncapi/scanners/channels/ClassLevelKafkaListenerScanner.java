package io.github.stavshamir.springwolf.asyncapi.scanners.channels;

import com.asyncapi.v2.binding.OperationBinding;
import com.asyncapi.v2.model.channel.ChannelItem;
import com.asyncapi.v2.model.channel.operation.Operation;
import com.google.common.collect.Maps;
import io.github.stavshamir.springwolf.configuration.AsyncApiDocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.*;

import static java.util.stream.Collectors.*;

@Slf4j
@Service
public class ClassLevelKafkaListenerScanner implements ChannelsScanner {

    @Autowired
    private AsyncApiDocket docket;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private PayloadTypeResolver payloadTypeResolver;

    @Autowired
    private KafkaListenerOperationBindingMapper operationBindingMapper;

    @Autowired
    private KafkaListenerChannelNameMapper channelNameMapper;

    public Map<String, ChannelItem> scan() {
        return docket.getComponentsScanner().scanForComponents().stream()
                .filter(this::isAnnotatedWithKafkaListener)
                .map(this::mapClassToChannel)
                .filter(Optional::isPresent).map(Optional::get)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private boolean isAnnotatedWithKafkaListener(Class<?> component) {
        return component.isAnnotationPresent(KafkaListener.class);
    }

    private Optional<Map.Entry<String, ChannelItem>> mapClassToChannel(Class<?> component) {
        log.debug("Mapping class \"{}\" to channel", component.getName());

        KafkaListener annotation = component.getAnnotation(KafkaListener.class);
        String channelName = getChannelName(annotation);
        Map<String, ? extends OperationBinding> operationBinding = buildOperationBinding(annotation);
        Set<Method> annotatedMethods = getAnnotatedMethods(component);

        if (annotatedMethods.isEmpty()) {
            return Optional.empty();
        }

        ChannelItem channelItem = buildChannel(annotatedMethods, operationBinding);
        return Optional.of(Maps.immutableEntry(channelName, channelItem));
    }

    protected String getChannelName(KafkaListener annotation) {
        return channelNameMapper.mapToChannelName(annotation);
    }

    protected Map<String, ? extends OperationBinding> buildOperationBinding(KafkaListener annotation) {
        return operationBindingMapper.mapToOperationBinding(annotation);
    }

    private Set<Method> getAnnotatedMethods(Class<?> component) {
        Class<KafkaHandler> annotationClass = KafkaHandler.class;
        log.debug("Scanning class \"{}\" for @\"{}\" annotated methods", component.getName(), annotationClass.getName());

        return Arrays.stream(component.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(annotationClass))
                .collect(toSet());
    }

    private ChannelItem buildChannel(Set<Method> methods, Map<String, ? extends OperationBinding> operationBinding) {
        Operation operation = Operation.builder()
                .message(buildMessage(methods))
                .bindings(operationBinding)
                .build();

        return ChannelItem.builder()
                .publish(operation)
                .build();
    }

    private Object buildMessage(Set<Method> methods) {
        List<? extends Class<?>> payloadTypes = methods.stream()
                .map(payloadTypeResolver::resolvePayloadType)
                .collect(toList());

        return messageMapper.mapToMessage(payloadTypes);
    }

}
