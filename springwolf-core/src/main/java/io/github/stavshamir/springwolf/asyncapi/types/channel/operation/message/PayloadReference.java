package io.github.stavshamir.springwolf.asyncapi.types.channel.operation.message;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class PayloadReference {

    @Getter
    private String $ref;

    private PayloadReference(String $ref) {
        this.$ref = $ref;
    }

    public static PayloadReference fromModelName(String modelName) {
        return new PayloadReference("#/components/schemas/" + modelName);
    }

}
