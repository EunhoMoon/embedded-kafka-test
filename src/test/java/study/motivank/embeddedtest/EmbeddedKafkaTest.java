package study.motivank.embeddedtest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import study.motivank.embeddedtest.kafka.KafkaConsumer;
import study.motivank.embeddedtest.kafka.KafkaProducer;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EmbeddedKafkaTest {

    @Autowired
    KafkaConsumer consumer;

    @Autowired
    KafkaProducer producer;

    @Value("${test.topic}")
    private String topic;

    @Test
    void testKafkaTemplate() throws Exception {
        // given
        var message = "hello world";

        // when
        producer.send(topic, message);

        // then
        boolean consumed = consumer.getLatch().await(10, TimeUnit.SECONDS);

        assertThat(consumed).isTrue();
        System.out.println(consumer.getPayload());
        assertThat(consumer.getPayload()).contains(message);
    }

}