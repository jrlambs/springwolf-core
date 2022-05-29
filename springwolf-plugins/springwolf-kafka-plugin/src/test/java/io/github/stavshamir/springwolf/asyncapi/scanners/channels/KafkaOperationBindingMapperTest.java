package io.github.stavshamir.springwolf.asyncapi.scanners.channels;

import com.asyncapi.v2.binding.OperationBinding;
import com.asyncapi.v2.binding.kafka.KafkaOperationBinding;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {KafkaOperationBindingMapper.class})
@TestPropertySource(properties = "kafka.groupId=groupId")
public class KafkaOperationBindingMapperTest {

    @Autowired
    private KafkaOperationBindingMapper mapper;

    @Test
    public void test_nullGroupId() throws NoSuchMethodException {
        KafkaListener annotation = getKafkaListenerAnnotation("nullGroupId");
        Map<String, ? extends OperationBinding> bindings = mapper.mapToOperationBinding(annotation);

        assertThat(bindings)
                .isEqualTo(ImmutableMap.of("kafka", new KafkaOperationBinding()));
    }

    @Test
    public void test_emptyGroupId() throws NoSuchMethodException {
        KafkaListener annotation = getKafkaListenerAnnotation("emptyGroupId");
        Map<String, ? extends OperationBinding> bindings = mapper.mapToOperationBinding(annotation);

        assertThat(bindings)
                .isEqualTo(ImmutableMap.of("kafka", new KafkaOperationBinding()));
    }

    @Test
    public void test_hardCodedGroupId() throws NoSuchMethodException {
        KafkaListener annotation = getKafkaListenerAnnotation("hardCodedGroupId");
        Map<String, ? extends OperationBinding> bindings = mapper.mapToOperationBinding(annotation);

        KafkaOperationBinding kafkaBinding = (KafkaOperationBinding) bindings.get("kafka");
        assertThat(kafkaBinding)
                .isNotNull();
        assertThat(kafkaBinding.getGroupId())
                .isEqualTo("groupId");
    }

    @Test
    public void test_embeddedGroupId() throws NoSuchMethodException {
        KafkaListener annotation = getKafkaListenerAnnotation("embeddedGroupId");
        Map<String, ? extends OperationBinding> bindings = mapper.mapToOperationBinding(annotation);

        KafkaOperationBinding kafkaBinding = (KafkaOperationBinding) bindings.get("kafka");
        assertThat(kafkaBinding)
                .isNotNull();
        assertThat(kafkaBinding.getGroupId())
                .isEqualTo("groupId");
    }

    private KafkaListener getKafkaListenerAnnotation(String methodName) throws NoSuchMethodException {
        Method method = this.getClass().getDeclaredMethod(methodName);
        return method.getAnnotation(KafkaListener.class);
    }

    @KafkaListener(topics = "topic")
    private void nullGroupId() {
    }

    @KafkaListener(topics = "topic", groupId = "")
    private void emptyGroupId() {
    }

    @KafkaListener(topics = "topic", groupId = "groupId")
    private void hardCodedGroupId() {
    }

    @KafkaListener(topics = "topic", groupId = "${kafka.groupId}")
    private void embeddedGroupId() {
    }

}