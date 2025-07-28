package com.ecole221.microservice.kafka.consumer;

import com.ecole221.microservice.kafka.data.config.KafkaConsumerDataConfig;
import com.ecole221.microservice.kafka.data.config.KafkaDataConfig;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig<K extends Serializable, V extends SpecificRecordBase> {
    private final KafkaDataConfig kafkaDataConfig;
    private final KafkaConsumerDataConfig kafkaConsumerDataConfig;

    public KafkaConsumerConfig(KafkaDataConfig kafkaDataConfig, KafkaConsumerDataConfig kafkaConsumerDataConfig) {
        this.kafkaDataConfig = kafkaDataConfig;
        this.kafkaConsumerDataConfig = kafkaConsumerDataConfig;
    }
    @Bean
    public Map<String,Object> consumerConfigs(){
        Map<String,Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,kafkaDataConfig.getBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, kafkaConsumerDataConfig.getKeyDeserializer());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, kafkaConsumerDataConfig.getValueDeserializer());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaConsumerDataConfig.getAutoOffsetReset());
        props.put(kafkaDataConfig.getSchemaRegistryUrlKey(), kafkaDataConfig.getSchemaRegistryUrl());
        props.put(kafkaConsumerDataConfig.getSpecificAvroReaderKey(), kafkaConsumerDataConfig.getSpecificAvroReader());
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, kafkaConsumerDataConfig.getSessionTimeoutMs());
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, kafkaConsumerDataConfig.getHeartbeatIntervalMs());
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, kafkaConsumerDataConfig.getMaxPollIntervalMs());
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG,
                kafkaConsumerDataConfig.getMaxPartitionFetchBytesDefault() *
                        kafkaConsumerDataConfig.getMaxPartitionFetchBytesBoostFactor());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, kafkaConsumerDataConfig.getMaxPollRecords());
     return props;
    }

    @Bean
    public ConsumerFactory<K, V> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<K, V>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<K, V> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setBatchListener(kafkaConsumerDataConfig.getBatchListener());
        factory.setConcurrency(kafkaConsumerDataConfig.getConcurrencyLevel());
        factory.setAutoStartup(kafkaConsumerDataConfig.getAutoStartup());
        factory.getContainerProperties().setPollTimeout(kafkaConsumerDataConfig.getPollTimeoutMs());
        return factory;
    }
}
