package io.github.stavshamir.springwolf.asyncapi.scanners.channels;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {RabbitListenerChannelNameMapper.class})
@TestPropertySource(properties = "amqp.queues.test=queue")
public class RabbitListenerChannelNameMapperTest {

    @Autowired
    private RabbitListenerChannelNameMapper mapper;

    @Test
    public void test_noQueues() throws NoSuchMethodException {
        RabbitListener annotation = getKafkaListenerAnnotation("noQueues");
        assertThatThrownBy(() -> mapper.mapToChannelName(annotation))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void test_hardcodedQueue() throws NoSuchMethodException {
        RabbitListener annotation = getKafkaListenerAnnotation("hardcodedQueue");
        String name = mapper.mapToChannelName(annotation);
        assertThat(name).isEqualTo("queue");
    }

    @Test
    public void test_embeddedQueue() throws NoSuchMethodException {
        RabbitListener annotation = getKafkaListenerAnnotation("embeddedQueue");
        String name = mapper.mapToChannelName(annotation);
        assertThat(name).isEqualTo("queue");
    }

    @Test
    public void test_queueBindingNoQueue() throws NoSuchMethodException {
        RabbitListener annotation = getKafkaListenerAnnotation("queueBindingNoQueue");
        assertThatThrownBy(() -> mapper.mapToChannelName(annotation))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void test_queueBindingHardcodedQueue() throws NoSuchMethodException {
        RabbitListener annotation = getKafkaListenerAnnotation("queueBindingHardcodedQueue");
        String name = mapper.mapToChannelName(annotation);
        assertThat(name).isEqualTo("queue");
    }

    @Test
    public void test_queueBindingEmbeddedQueue() throws NoSuchMethodException {
        RabbitListener annotation = getKafkaListenerAnnotation("queueBindingEmbeddedQueue");
        String name = mapper.mapToChannelName(annotation);
        assertThat(name).isEqualTo("queue");
    }

    private RabbitListener getKafkaListenerAnnotation(String methodName) throws NoSuchMethodException {
        Method method = this.getClass().getDeclaredMethod(methodName);
        return method.getAnnotation(RabbitListener.class);
    }

    @RabbitListener()
    private void noQueues() {
    }

    @RabbitListener(queues = "queue")
    private void hardcodedQueue() {
    }

    @RabbitListener(queues = "${amqp.queues.test}")
    private void embeddedQueue() {
    }

    @RabbitListener(bindings = {
            @QueueBinding(
                    exchange = @Exchange(name = "name", type = "topic"),
                    key = "key",
                    value = @Queue())
    })
    private void queueBindingNoQueue() {
    }

    @RabbitListener(bindings = {
            @QueueBinding(
                    exchange = @Exchange(name = "name", type = "topic"),
                    key = "key",
                    value = @Queue(name = "queue"))
    })
    private void queueBindingHardcodedQueue() {
    }

    @RabbitListener(bindings = {
            @QueueBinding(
                    exchange = @Exchange(name = "name", type = "topic"),
                    key = "key",
                    value = @Queue(name = "${amqp.queues.test}"))
    })
    private void queueBindingEmbeddedQueue() {
    }

}