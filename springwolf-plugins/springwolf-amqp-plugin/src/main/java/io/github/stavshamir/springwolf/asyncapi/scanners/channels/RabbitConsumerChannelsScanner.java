package io.github.stavshamir.springwolf.asyncapi.scanners.channels;

import com.asyncapi.v2.binding.ChannelBinding;
import com.asyncapi.v2.binding.OperationBinding;
import com.asyncapi.v2.binding.amqp.AMQPChannelBinding;
import com.asyncapi.v2.binding.amqp.AMQPOperationBinding;
import com.asyncapi.v2.model.channel.ChannelItem;
import com.asyncapi.v2.model.channel.operation.Operation;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.github.stavshamir.springwolf.asyncapi.types.channel.operation.message.Message;
import io.github.stavshamir.springwolf.asyncapi.types.channel.operation.message.PayloadReference;
import io.github.stavshamir.springwolf.configuration.AsyncApiDocket;
import io.github.stavshamir.springwolf.schemas.SchemasService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.util.StringValueResolver;
import org.springframework.web.context.support.StandardServletEnvironment;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Slf4j
@Service
public class RabbitConsumerChannelsScanner implements ChannelsScanner, EmbeddedValueResolverAware {

    private final Map<String, Binding> bindingsMap;
    private StringValueResolver resolver;
    @Autowired
    private AsyncApiDocket docket;

    @Autowired
    private SchemasService schemasService;

    @Autowired
    private Environment env;

    @Override
    public Map<String, ChannelItem> scan() {
        return docket.getComponentsScanner().scanForComponents().stream()
                .map(this::getAnnotatedMethods).flatMap(Collection::stream)
                .map(this::mapMethodToChannel)
                .filter(Objects::nonNull)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Set<Method> getAnnotatedMethods(Class<?> type) {
        Class<Bean> annotationClass = getListenerAnnotationClass();
        log.debug("Scanning class \"{}\" for @\"{}\" annotated methods", type.getName(), annotationClass.getName());

        Set<Method> declaredBeans = Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(annotationClass))
                .collect(toSet());



        return declaredBeans.stream().filter((method) ->
                Consumer.class.equals(method.getReturnType())).collect(toSet());
    }

    private Map.Entry<String, ChannelItem> mapMethodToChannel(Method method) {
        log.debug("Mapping method \"{}\" to channels", method.getName());

        String methodName = method.getName();
        String channelName = null;
        Class<?> payload = null;
        Type genericReturnType = method.getGenericReturnType();
        Class<?> returnType = null;
        if(genericReturnType != null && genericReturnType instanceof ParameterizedTypeImpl) {
            Type[] typeArguments = ((ParameterizedTypeImpl)genericReturnType).getActualTypeArguments();
            if(typeArguments != null && typeArguments.length == 1) {
                String typeName = typeArguments[0].getTypeName();
                try {
                    returnType = Class.forName(typeName);
                } catch(ClassNotFoundException classNotFoundException) {
                    //should never happen;
                }
            }
        }
        if(returnType != null) {
            payload = returnType;
        }

        MutablePropertySources propertySources = ((StandardServletEnvironment) env).getPropertySources();
        Set<PropertySource> sources = propertySources.stream().collect(toSet());
        for(PropertySource source : sources) {
            System.out.println(source);
            if (source instanceof MapPropertySource) {
                String[] names = ((MapPropertySource)source).getPropertyNames();
                for(String name : names) {
                    if(name.startsWith("spring.cloud.stream.bindings."+methodName+"-") || name.startsWith("spring.cloud.stream.bindings."+methodName+".")) {
                        List<String> parts = Arrays.stream(name.split("\\.")).collect(Collectors.toList());
                        channelName = parts.get(parts.indexOf("bindings")+1);

                    }
                }
            }
        }
        if(returnType != null && channelName != null && methodName != null) {
            Map<String, ? extends ChannelBinding> channelBinding = buildChannelBinding(channelName);
            Map<String, ? extends OperationBinding> operationBinding = buildOperationBinding(channelName);
            ChannelItem channel = buildChannel(channelBinding, payload, operationBinding);
            return Maps.immutableEntry(channelName, channel);
        }
        return null;
    }

    private ChannelItem buildChannel(Map<String, ? extends ChannelBinding> channelBinding,
                                     Class<?> payloadType,
                                     Map<String, ? extends OperationBinding> operationBinding) {
        String modelName = schemasService.register(payloadType);

        Message message = Message.builder()
                .name(payloadType.getName())
                .title(modelName)
                .payload(PayloadReference.fromModelName(modelName))
                .build();

        Operation operation = Operation.builder()
                .message(message)
                .bindings(operationBinding)
                .build();

        return ChannelItem.builder()
                .bindings(channelBinding)
                .publish(operation)
                .build();
    }


    public RabbitConsumerChannelsScanner(List<Binding> bindings) {
        bindingsMap = bindings.stream()
                .filter(Binding::isDestinationQueue)
                .collect(Collectors.toMap(Binding::getDestination, Function.identity()));
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.resolver = resolver;
    }

    protected Class<Bean> getListenerAnnotationClass() {
        return Bean.class;
    }





    protected Map<String, ? extends ChannelBinding> buildChannelBinding(String channelName) {


        AMQPChannelBinding.ExchangeProperties exchangeProperties = new AMQPChannelBinding.ExchangeProperties();
        exchangeProperties.setName(getExchangeName(channelName));
        return ImmutableMap.of("amqp", AMQPChannelBinding.builder()
                .is("routingKey")
                .exchange(exchangeProperties)
                .build());
    }

    private String getExchangeName(String channelName) {
        String exchangeName =  env.getProperty("spring.cloud.stream.bindings."+channelName+".destination");
        if(exchangeName == null) {
            exchangeName = "";
        }
        return exchangeName;
    }

    protected Map<String, ? extends OperationBinding> buildOperationBinding(String channelName) {
        return ImmutableMap.of("amqp", AMQPOperationBinding.builder()
                .cc(getRoutingKeys(channelName))
                .build());
    }

    private List<String> getRoutingKeys(String channelName) {
        List<String> response = new ArrayList<>();
        String routingKey = env.getProperty("spring.cloud.stream.bindings."+channelName+".consumer.bindingRoutingKey");
        if(routingKey != null) {
            response.add(routingKey);
        }
        return response;
    }

}
