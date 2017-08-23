package com.sebastian_daschner.scalable_coffee_shop.events.control;

import com.sebastian_daschner.scalable_coffee_shop.events.entity.CoffeeEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static java.util.Arrays.asList;

public class EventConsumer implements Runnable {

    private final KafkaConsumer<String, CoffeeEvent> consumer;
    private final OffsetTracker offsetTracker;
    private final Consumer<CoffeeEvent> eventConsumer;
    private final AtomicBoolean closed = new AtomicBoolean();

    public EventConsumer(Properties kafkaProperties, Consumer<CoffeeEvent> eventConsumer, OffsetTracker offsetTracker, String... topics) {
        this.eventConsumer = eventConsumer;
        this.offsetTracker = offsetTracker;
        consumer = new KafkaConsumer<>(kafkaProperties);
        consumer.subscribe(asList(topics), new OffsetTrackingRebalanceListener(consumer, offsetTracker));
    }

    @Override
    public void run() {
        try {
            while (!closed.get()) {
                consume();
            }
        } catch (WakeupException e) {
            // will wakeup for closing
        } finally {
            consumer.close();
        }
    }

    private void consume() {
        ConsumerRecords<String, CoffeeEvent> records = consumer.poll(Long.MAX_VALUE);
        for (ConsumerRecord<String, CoffeeEvent> record : records) {
            eventConsumer.accept(record.value());
            offsetTracker.trackOffset(record.topic(), record.partition(), record.offset() + 1);
        }
    }

    public void stop() {
        closed.set(true);
        consumer.wakeup();
    }

}
