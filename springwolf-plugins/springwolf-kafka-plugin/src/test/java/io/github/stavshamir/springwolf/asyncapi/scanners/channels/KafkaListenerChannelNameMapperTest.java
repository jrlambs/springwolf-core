package io.github.stavshamir.springwolf.asyncapi.scanners.channels;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {KafkaListenerChannelNameMapper.class})
@TestPropertySource(properties = "kafka.topic=topic")
public class KafkaListenerChannelNameMapperTest {

    @Autowired
    private KafkaListenerChannelNameMapper mapper;

    @Test
    public void test_embeddedTopic() throws NoSuchMethodException {
        KafkaListener annotation = getKafkaListenerAnnotation("embeddedTopic");
        String name = mapper.mapToChannelName(annotation);
        assertThat(name)
                .isEqualTo("topic");
    }

    @Test
    public void test_hardCodedTopic() throws NoSuchMethodException {
        KafkaListener annotation = getKafkaListenerAnnotation("hardCodedTopic");
        String name = mapper.mapToChannelName(annotation);
        assertThat(name)
                .isEqualTo("topic");
    }

    @Test
    public void test_noTopic() throws NoSuchMethodException {
        KafkaListener annotation = getKafkaListenerAnnotation("noTopic");
        assertThatThrownBy(() -> mapper.mapToChannelName(annotation))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void test_multipleTopics() throws NoSuchMethodException {
        KafkaListener annotation = getKafkaListenerAnnotation("multipleTopics");
        String name = mapper.mapToChannelName(annotation);
        assertThat(name)
                .isEqualTo("topic1");
    }

    private KafkaListener getKafkaListenerAnnotation(String methodName) throws NoSuchMethodException {
        Method method = this.getClass().getDeclaredMethod(methodName);
        return method.getAnnotation(KafkaListener.class);
    }

    @KafkaListener(topics = "${kafka.topic}")
    private void embeddedTopic() {
    }

    @KafkaListener(topics = "topic")
    private void hardCodedTopic() {
    }

    @KafkaListener
    private void noTopic() {
    }

    @KafkaListener(topics = {"topic1", "topic2"})
    private void multipleTopics() {
    }

}