package io.github.stavshamir.springwolf.asyncapi.scanners.channels;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {DefaultPayloadTypeResolver.class})
public class DefaultPayloadTypeResolverTest extends TestCase {

    @Autowired
    private PayloadTypeResolver payloadTypeResolver;

    @Test
    public void zeroParameters() throws NoSuchMethodException {
        // Given:
        // - A method with more than zero parameters
        Method method = this.getClass().getDeclaredMethod("zeroParamsMethod");

        // Then an exception is thrown when resolvePayloadType is called
        assertThatThrownBy(() -> payloadTypeResolver.resolvePayloadType(method))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void singleParam_withoutPayloadAnnotation() throws NoSuchMethodException {
        // Given:
        // - A method with one parameter
        Method method = this.getClass().getDeclaredMethod("singleParamMethodWithoutPayloadAnnotations", String.class);

        // Then the payload type is resolved
        Class<?> payloadType = payloadTypeResolver.resolvePayloadType(method);
        assertThat(payloadType)
                .isEqualTo(String.class);
    }

    @Test
    public void singleParam_withPayloadAnnotation() throws NoSuchMethodException {
        // Given:
        // - A method with one parameter annotated with @Payload
        Method method = this.getClass().getDeclaredMethod("singleParamMethodWithPayloadAnnotations", String.class);

        // Then the payload type is resolved
        Class<?> payloadType = payloadTypeResolver.resolvePayloadType(method);
        assertThat(payloadType)
                .isEqualTo(String.class);
    }

    @Test
    public void multipleParams_withoutPayloadAnnotation() throws NoSuchMethodException {
        // Given:
        // - A method with more than one parameter
        // - No parameter is annotated with @Payload
        Method method = this.getClass()
                .getDeclaredMethod("multiParamsMethodWithoutPayloadAnnotation", String.class, int.class);

        // Then an exception is thrown when resolvePayloadType is called
        assertThatThrownBy(() -> payloadTypeResolver.resolvePayloadType(method))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void multipleParams_withPayloadAnnotation() throws NoSuchMethodException {
        // Given:
        // - A method with more than one parameter
        // - One parameter is annotated with @Payload
        Method method = this.getClass().getDeclaredMethod("multiParamsMethodWithPayloadAnnotation", String.class, int.class);

        // Then the payload type is resolved
        Class<?> payloadType = payloadTypeResolver.resolvePayloadType(method);
        assertThat(payloadType)
                .isEqualTo(int.class);
    }

    private void zeroParamsMethod() {
    }

    private void singleParamMethodWithoutPayloadAnnotations(String payload) {
    }

    private void singleParamMethodWithPayloadAnnotations(@Payload String payload) {
    }

    private void multiParamsMethodWithoutPayloadAnnotation(String payload, int anotherParam) {
    }

    private void multiParamsMethodWithPayloadAnnotation(String anotherParam, @Payload int payload) {
    }

}