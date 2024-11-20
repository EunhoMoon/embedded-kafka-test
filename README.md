# Embedded Kafka Test
- This guide outlines how to set up and test Kafka functionality using Embedded Kafka in a Spring Boot application.
- Spring boot 어플리케이션에서 Embedded Kafka를 사용하여 Kafka 기능을 설정하고 테스트하는 방법에 대해 설명합니다.

## Prerequisites
- Java 17 or higher
- Spring Boot version 3.3.5
- Gradle 

## Setting up Embedded Kafka
1. Add the following dependencies to your `build.gradle` file:
```groovy
dependencies {
    implementation 'org.springframework.kafka:spring-kafka'
    implementation 'org.apache.kafka:kafka-clients'
    testImplementation 'org.springframework.kafka:spring-kafka-test'
}
```

2. Configure Embedded Kafka in your `application.yml` file:
```yml
spring:
  kafka:
    consumer:
      auto-offset-reset: earliest
      group-id: test-group

test:
  topic: test-topic
```

3. Create Producer and Consumer classes:
```java 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void send(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }
}
```

```java
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Component
public class KafkaConsumer {

    private final CountDownLatch latch = new CountDownLatch(1);
    private String payload;

    @KafkaListener(topics = "${test.topic}", groupId = "test-group")
    public void consume(String message) {
        this.payload = message;
        latch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public String getPayload() {
        return payload;
    }
}
```

4. Create a test class to test the Kafka functionality:
```java
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
        assertThat(consumer.getPayload()).contains(message);
    }
}
```