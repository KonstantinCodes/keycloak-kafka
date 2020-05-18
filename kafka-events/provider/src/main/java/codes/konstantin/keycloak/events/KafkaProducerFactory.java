package codes.konstantin.keycloak.events;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.StringSerializer;

public final class KafkaProducerFactory {

    static Producer<String, String> instance;

    private KafkaProducerFactory() {

    }

    public static Producer<String, String> createProducer(
            ClassLoader classLoader,
            String clientId,
            String bootstrapServer,
            String username,
            String password
    ) {
        if (instance == null) {
            String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
            String jaasCfg = String.format(jaasTemplate, username, password);

            String serializer = StringSerializer.class.getName();

            Properties props = new Properties();
            props.put("bootstrap.servers", bootstrapServer);
            //props.put("client.id", clientId);
            props.put("key.serializer", serializer);
            props.put("value.serializer", serializer);
            props.put("partitioner.class", "org.apache.kafka.clients.producer.internals.DefaultPartitioner");
            props.put("security.protocol", "SASL_SSL");
            props.put("sasl.mechanism", "SCRAM-SHA-256");
            props.put("sasl.jaas.config", jaasCfg);

            // fix Class org.apache.kafka.common.serialization.StringSerializer could not be
            // found. see https://stackoverflow.com/a/50981469
            Thread.currentThread().setContextClassLoader(classLoader);

            instance = new KafkaProducer<>(props);
        }

        return instance;
    }
}