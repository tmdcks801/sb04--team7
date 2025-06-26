package com.example.ootd.config;


import com.example.ootd.domain.notification.dto.NotificationRequest;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.util.backoff.FixedBackOff;

//@Configuration
//@EnableKafka
public class KafkaConfig {

//  @Value("${spring.kafka.bootstrap-servers}")//env로 주입 필요
//  private String bootstrapServers;
//
//  @Bean//Producer 인스턴스를 만드는 팩토리
//  public ProducerFactory<String, NotificationRequest> producerFactory() {
//    Map<String, Object> props = new HashMap<>();
//    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class); //직렬화
//    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);//직렬화
//    props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
//    return new DefaultKafkaProducerFactory<>(props);
//  }
//
//  @Primary
//  @Bean(name = "notificationKafkaTemplate")
//  public KafkaTemplate<String, NotificationRequest> notificationKafkaTemplate(
//      ProducerFactory<String, NotificationRequest> notificationProducerFactory) {
//    return new KafkaTemplate<>(notificationProducerFactory);
//  }
//
//  @Bean //	Consumer 인스턴스를 만드는 팩토리
//  public ConsumerFactory<String, NotificationRequest> consumerFactory() {
//    JsonDeserializer<NotificationRequest> deserializer = new JsonDeserializer<>(
//        NotificationRequest.class);
//    deserializer.addTrustedPackages("*");//모든 패키지에서 허용
//    deserializer.setUseTypeMapperForKey(false);
//
//    Map<String, Object> props = new HashMap<>();
//    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//    props.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-service");
//    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer.getClass());
//    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//
//    return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
//  }
//
//  @Bean
//  public ConcurrentKafkaListenerContainerFactory<String, NotificationRequest> kafkaListenerContainerFactory() {
//    ConcurrentKafkaListenerContainerFactory<String, NotificationRequest> factory = new ConcurrentKafkaListenerContainerFactory<>();
//    factory.setConsumerFactory(consumerFactory());
//    factory.setConcurrency(3);//파티션 일단 세개
//    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
//    //실패하면 5초간격으로 3번
//    CommonErrorHandler errorHandler = new DefaultErrorHandler(new FixedBackOff(5000L, 3L));
//    factory.setCommonErrorHandler(errorHandler);
//    return factory;
//  }
//
//  @Bean
//  public NewTopic notificationTopic() {
//    return TopicBuilder.name("notification-events")
//        .partitions(3)
//        .replicas(1)
//        .build();
//  }

}
