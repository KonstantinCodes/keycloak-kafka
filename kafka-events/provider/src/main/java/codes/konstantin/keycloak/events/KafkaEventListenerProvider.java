package codes.konstantin.keycloak.events;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.jboss.logging.Logger;
import org.json.JSONObject;
import org.keycloak.events.Details;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProviderFactory;
import org.keycloak.models.jpa.JpaUserProviderFactory;

public class KafkaEventListenerProvider implements EventListenerProvider {

    private static final Logger LOG = Logger.getLogger(KafkaEventListenerProvider.class);

    private KeycloakSession session;
    private String topicEvents;
    private List<EventType> events;
    private String topicAdminEvents;
    private Producer<String, String> producer;
    private ObjectMapper mapper;

    public KafkaEventListenerProvider(
            String bootstrapServers,
            String username,
            String password,
            String clientId,
            String topicEvents,
            String[] events,
            String topicAdminEvents
    ) {
        this.topicEvents = topicEvents;
        this.events = new ArrayList<>();
        this.topicAdminEvents = topicAdminEvents;

        for (String event : events) {
            try {
                EventType eventType = EventType.valueOf(event.toUpperCase());
                this.events.add(eventType);
            } catch (IllegalArgumentException e) {
                LOG.debug("Ignoring event >" + event + "<. Event does not exist.");
            }
        }

        producer = KafkaProducerFactory.createProducer(this.getClass().getClassLoader(), clientId, bootstrapServers, username, password);
        mapper = new ObjectMapper();
    }

    private void produceEvent(String eventAsString, String topic) throws InterruptedException, ExecutionException {
        LOG.warn("Produce to topic: " + topicEvents + " ...");

        JSONObject message = new JSONObject();
        message.put("body", eventAsString);

        ProducerRecord<String, String> record = new ProducerRecord<>(topic, message.toString());
        Future<RecordMetadata> metaData = producer.send(record);
        RecordMetadata recordMetadata = metaData.get();
        LOG.debug("Produced to topic: " + recordMetadata.topic());
    }

    @Override
    public void onEvent(Event event) {
        if (events.contains(event.getType())) {
            try {
                produceEvent(mapper.writeValueAsString(event), topicEvents);
            } catch (JsonProcessingException | ExecutionException e) {
                LOG.error(e.getMessage(), e);
            } catch (InterruptedException e) {
                LOG.error(e.getMessage(), e);
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        if (topicAdminEvents != null) {
            if (Objects.equals(event.getResourceType(), ResourceType.USER)) {
                try {
                    produceEvent(mapper.writeValueAsString(event), topicAdminEvents);
                } catch (JsonProcessingException | ExecutionException e) {
                    LOG.error(e.getMessage(), e);
                } catch (InterruptedException e) {
                    LOG.error(e.getMessage(), e);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Override
    public void close() {
        // ignore
    }
}