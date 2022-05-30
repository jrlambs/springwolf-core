package io.github.stavshamir.springwolf.asyncapi.scanners.channels;

import java.lang.annotation.Annotation;

public interface ChannelNameMapper<T extends Annotation> {

    /**
     * Maps the given annotation to the channel name.
     *
     * @param annotation An instance of a listener annotation.
     * @return The channel name associated with this instance of listener annotation.
     */
    String mapToChannelName(T annotation);

}
