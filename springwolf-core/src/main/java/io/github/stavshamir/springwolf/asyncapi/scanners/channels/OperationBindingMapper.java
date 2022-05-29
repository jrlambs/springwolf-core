package io.github.stavshamir.springwolf.asyncapi.scanners.channels;

import com.asyncapi.v2.binding.OperationBinding;

import java.lang.annotation.Annotation;
import java.util.Map;

public interface OperationBindingMapper<T extends Annotation> {

    /**
     * Map the given annotation to an operation binding.
     *
     * @param annotation An instance of a listener annotation.
     * @return A map containing an operation binding pointed to by the protocol binding name.
     */
    Map<String, ? extends OperationBinding> mapToOperationBinding(T annotation);

}
