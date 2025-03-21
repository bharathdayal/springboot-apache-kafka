# Introduction to Kafka Producer and Consumer in Spring Boot

Apache Kafka is a distributed streaming platform commonly used for building real-time data pipelines and streaming applications. In a Spring Boot application, you can integrate Kafka to handle asynchronous messaging between microservices or components. In this document, we will implement a basic Kafka Producer to send messages to a Kafka topic and a Kafka Consumer to consume those messages.

## Prerequisites

- Spring Boot 3.4
- Java 17
- Apache Kafka running locally or on a server (you can install it via Kafka's documentation).
- Maven or Gradle for dependency management.

## Step-by-Step Guide to Implement Kafka Producer and Consumer Service

### 3.1. Create Two Services (Application)

- Kafka Producer service and Kafka Consumer Service

### 3.2. Add Dependencies (Both Kafka Producer and Kafka Consumer Service)

Using Gradle, add this to your `build.gradle`:

```groovy
dependencies {
  implementation 'org.springframework.boot:spring-boot-starter-web'
  implementation 'org.springframework.kafka:spring-kafka'
  testImplementation 'org.springframework.boot:spring-boot-starter-test'
  testImplementation 'org.springframework.kafka:spring-kafka-test'
  testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```

### 3.3. Configure Kafka Producer Service Properties

Next, configure the connection details for Kafka in your `application.properties` or `application.yml` file:

```properties
# Kafka Properties
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.topic=spring-kafka-topic
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer
```

- `spring.kafka.bootstrap-servers`: Kafka broker address.
- `spring.kafka.topic`: Create a Topic: In properties file or using Kafka installed in local/server 
  ```sh
  bin/kafka-topics.sh --create --topic spring-kafka-topic --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
  ```
- `key-serializer` / `value-serializer`: For Kafka producers.

### 3.4. Create Kafka Producer

To create a Kafka Producer, you'll use Spring’s `KafkaTemplate` for sending messages.

**Producer Configuration (`KafkaProducerConfig.java`):**

```java
@Configuration
@EnableKafka
public class KafkaProducerConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String kafkaAddress;

  @Value("${spring.kafka.producer.key-serializer}")
  private String kafkaProducerKey;

  @Value("${spring.kafka.producer.value-serializer}")
  private String kafkaProducerValue;

  @Bean
  public ProducerFactory<String, String> producerFactory() {
    Map<String, Object> configProps = new HashMap<>();
    configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaAddress);
    configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, kafkaProducerKey);
    configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, kafkaProducerValue);
    return new DefaultKafkaProducerFactory<>(configProps);
  }

  @Bean
  public KafkaTemplate<String, String> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
  }
}
```

**Kafka Producer Service (`KafkaProducer.java`):**

```java
@Service
public class KafkaProducer {

  @Autowired
  private final KafkaTemplate<String,String> kafkaTemplate;

  @Value("${spring.kafka.producer.topic}")
  public String topic;

  public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void sendMessage(String message) {
    kafkaTemplate.send(topic, message);
    System.out.println("Message Sent from Producer :" + message);
  }
}
```

In this example:
- `KafkaProducerService` is responsible for sending messages to a Kafka topic.
- `sendMessage` method sends messages to the specified topic.

### 3.5. Configure Kafka Consumer Service Properties

```properties
# Kafka Properties
server.port=8081
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.auto-offset-reset=latest
spring.kafka.topic=spring-kafka-topic
spring.kafka.group-id=consumer-group
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
```

- `spring.kafka.bootstrap-servers`: Kafka broker address.
- `spring.kafka.topic`: Topic created
- `spring.kafka.consumer.group-id`: Consumer group ID.
- `spring.kafka.consumer.auto-offset-reset`: Specifies what to do when there’s no initial offset or the offset is out of range (i.e., "earliest" means the consumer will start reading from the earliest message).
- `key-deserializer` / `value-deserializer`: For Kafka consumers.

### 3.6. Create Kafka Consumer Service

**Consumer Configuration (`KafkaConsumerConfig.java`):**

```java
public class KafkaConsumerConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String kafkaAddress;

  @Value("${spring.kafka.group-id}")
  private String groupId;

  @Value("${spring.kafka.consumer.key-serializer}")
  private String kafkaConsumerKey;

  @Value("${spring.kafka.consumer.value-serializer}")
  private String kafkaConsumerValue;

  @Bean
  public ConsumerFactory<String, String> consumerFactory() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaAddress);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, kafkaConsumerKey);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, kafkaConsumerValue);
    return new DefaultKafkaConsumerFactory<>(props);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    return factory;
  }
}
```

Use `@KafkaListener` annotations to listen for messages from the Kafka topic.

**Kafka Consumer (`KafkaConsumer.java`):**

```java
@Component
public class KafkaConsumer {

  @KafkaListener(topics = "${spring.kafka.topic}", groupId = "${spring.kafka.group-id}")
  public void consumerListener(String message) {
    System.out.println("Message received at Consumer :" + message);
  }
}
```

### 3.7. Sending Messages (Using Controller or Service)

You can create a REST Controller to send a message to Kafka via the producer service.

**Kafka Controller (`KafkaController.java`):**

```java
@RestController
public class KafkaController {

  private final KafkaProducer kafkaProducer;

  public KafkaController(KafkaProducer kafkaProducer) {
    this.kafkaProducer = kafkaProducer;
  }

  @GetMapping("/api/send")
  public String sendMessage(@RequestParam String message) {
    kafkaProducer.sendMessage(message);
    return message;
  }
}
```

In this example:
- We have created a simple REST API `/send-message` that takes a message as a request parameter and sends it to Kafka.
- The `KafkaProducer` Service is used to send the message to the specified Kafka topic.

### 3.8. Running Kafka Locally

To run Kafka locally:

1. Start Zookeeper: Kafka uses Zookeeper for managing distributed servers.
   ```sh
   .\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties
   ```

2. Start Kafka Broker:
   ```sh
   .\bin\windows\kafka-server-start.bat .\config\server.properties
   ```

3. Create a Topic (for the example above, we are using `spring-kafka-topic`):
   ```sh
   bin/kafka-topics.sh --create --topic spring-kafka-topic --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
   ```

## Testing the Application

1. Start the Spring Boot Application:
   - Run the Spring Boot application using your IDE or by executing: 

2. Send a Test Message:
   ```sh
   curl "http://localhost:8080/api/send?message=Message from producer"
   ```
   - The message "Message from producer" will be sent to the Kafka topic `spring-kafka-topic`.

3. Check the Producer Service Logs:
   - Producer service using REST API – sent message to topic – 
   ```
   [springboot-producer-kafka] [afka-producer-1] o.a.k.c.p.internals.TransactionManager : [Producer clientId=springboot-producer-kafka-producer-1] ProducerId set to 9001 with epoch 0
   Message Sent from Producer :Message from producer
   ```

4. Check the Consumer Service Logs:
   - The Kafka Consumer should automatically receive the message and print it in the logs as "Received message: Message from producer".
   ```
   [springboot-consumer-kafka] [ntainer#0-0-C-1] org.apache.kafka.clients.NetworkClient : [Consumer clientId=consumer-consumer-group-1, groupId=consumer-group] Node -1 disconnected.
   Message received at Consumer :Message from producer
   ```

5. Check the Kafka Server Logs:

## Conclusion

You now have a basic Spring Boot application with Kafka Producer and Consumer implementations. You can extend this by adding more advanced features like:
- Handling different data types (e.g., JSON messages) using `MessageConverters`.
- Configuring Error Handling for message delivery failures.
- Using Kafka Streams for more advanced processing.

This setup is a great foundation for building scalable and distributed messaging systems using Apache Kafka with Spring Boot.
