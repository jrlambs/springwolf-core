package io.github.stavshamir.springwolf.asyncapi.scanners.channels;

import org.springframework.messaging.handler.annotation.Payload;

import java.lang.reflect.Method;

public interface PayloadTypeResolver {

    /**
     * Resolve the payload type of the given listener method.
     * If the method has a single parameter, the type of that parameter is returned.
     * If the method has multiple parameters, the type of the parameter annotated with @{@link Payload} is returned.
     *
     * @param method A Method object.
     * @return The payload type of the given listener method.
     * @throws IllegalArgumentException If the payload type cannot be resolved.
     */
    Class<?> resolvePayloadType(Method method);

}
