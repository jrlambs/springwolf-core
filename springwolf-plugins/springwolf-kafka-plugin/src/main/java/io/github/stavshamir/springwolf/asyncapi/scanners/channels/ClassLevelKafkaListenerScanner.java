package io.github.stavshamir.springwolf.asyncapi.scanners.channels;

import com.asyncapi.v2.binding.OperationBinding;
import com.asyncapi.v2.model.channel.ChannelItem;
import com.asyncapi.v2.model.channel.operation.Operation;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.github.stavshamir.springwolf.asyncapi.types.channel.operation.message.Message;
import io.github.stavshamir.springwolf.asyncapi.types.channel.operation.message.PayloadReference;
import io.github.stavshamir.springwolf.configuration.AsyncApiDocket;
import io.github.stavshamir.springwolf.schemas.SchemasService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.util.StringValueResolver;

import java.lang.reflect.Method;
import java.util.*;

import static io.github.stavshamir.springwolf.asyncapi.Constants.ONE_OF;
import static java.util.stream.Collectors.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClassLevelKafkaListenerScanner
        implements ChannelsScanner, EmbeddedValueResolverAware {

    private StringValueResolver resolver;

    @Autowired
    private AsyncApiDocket docket;

    @Autowired
    private SchemasService schemasService;

    @Autowired
    private PayloadTypeResolver payloadTypeResolver;

    @Autowired
    private KafkaOperationBindingMapper operationBindingMapper;

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.resolver = resolver;
    }

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
        List<String> resolvedTopics = Arrays.stream(annotation.topics())
                .map(resolver::resolveStringValue)
                .collect(toList());

        log.debug("Found topics: {}", String.join(", ", resolvedTopics));
        return resolvedTopics.get(0);
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
                .message(getMessageObject(methods))
                .bindings(operationBinding)
                .build();

        return ChannelItem.builder()
                .publish(operation)
                .build();
    }

    private Object getMessageObject(Set<Method> methods) {
        Set<Message> messages = methods.stream()
                .map(this::buildMessage)
                .collect(toSet());

        return methods.size() == 1
                ? messages.toArray()[0]
                : ImmutableMap.of(ONE_OF, messages);
    }

    private Message buildMessage(Method method) {
        Class<?> payloadType = payloadTypeResolver.resolvePayloadType(method);
        String modelName = schemasService.register(payloadType);

        return Message.builder()
                .name(payloadType.getName())
                .title(modelName)
                .payload(PayloadReference.fromModelName(modelName))
                .build();
    }

}
