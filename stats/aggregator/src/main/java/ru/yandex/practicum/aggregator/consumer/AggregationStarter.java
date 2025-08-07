package ru.yandex.practicum.aggregator.consumer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.aggregator.kafka.KafkaProperties;
import ru.yandex.practicum.aggregator.producer.AggregatorKafkaProducer;
import ru.yandex.practicum.aggregator.service.AggregatorService;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private final KafkaConsumer<Long, UserActionAvro> consumer;
    private final AggregatorService aggregatorService;
    private final KafkaProperties kafkaProperties;
    private final AggregatorKafkaProducer producer;

    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
        String topic = kafkaProperties.getConsumer().getTopic();
        Duration pollTimeout = Duration.ofMillis(kafkaProperties.getConsumer().getPollTimeout());

        try {
            consumer.subscribe(List.of(topic));
            log.info("Aggregator subscribed to topic: {}", topic);

            while (true) {
                ConsumerRecords<Long, UserActionAvro> records = consumer.poll(pollTimeout);
                if (!records.isEmpty()) {
                    log.info("Received {} records from Kafka", records.count());
                }

                int count = 0;
                for (ConsumerRecord<Long, UserActionAvro> record : records) {
                    try {
                        aggregatorService.processUserAction(record.value());
                    } catch (Exception e) {
                        log.error("Failed to process record with offset {}: {}", record.offset(), e.getMessage(), e);
                    }

                    currentOffsets.put(
                            new TopicPartition(record.topic(), record.partition()),
                            new OffsetAndMetadata(record.offset() + 1)
                    );

                    if (++count % 10 == 0) {
                        consumer.commitAsync(currentOffsets, (offsets, ex) -> {
                            if (ex != null) {
                                log.warn("Failed to commit offsets: {}", offsets, ex);
                            }
                        });
                    }
                }

                consumer.commitAsync(currentOffsets, (offsets, ex) -> {
                    if (ex != null) {
                        log.warn("Final commit failed: {}", offsets, ex);
                    }
                });
            }
        } catch (WakeupException ignored) {
            log.info("Received shutdown signal for Aggregator.");
        } catch (Exception e) {
            log.error("Unexpected error in Aggregator consumer loop", e);
        } finally {
            try {
                consumer.commitSync(currentOffsets);
                log.info("Final offsets committed successfully.");
                producer.flush();
                log.info("Producer buffer flushed.");
            } finally {
                log.info("Closing Kafka consumer");
                consumer.close();
                log.info("Closing Kafka producer...");
                producer.close();
            }
        }
    }
}