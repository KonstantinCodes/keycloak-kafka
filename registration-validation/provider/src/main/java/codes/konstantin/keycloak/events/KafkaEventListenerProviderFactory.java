package codes.konstantin.keycloak.events;

import org.jboss.logging.Logger;
import org.keycloak.Config.Scope;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class KafkaEventListenerProviderFactory implements EventListenerProviderFactory {

    private static final Logger LOG = Logger.getLogger(KafkaEventListenerProviderFactory.class);
    private static final String ID = "kafka";

    private KafkaEventListenerProvider instance;

    private String bootstrapServers;
    private String username;
    private String password;
    private String topicEvents;
    private String topicAdminEvents;
    private String clientId;
    private String[] events;

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        if (instance == null) {
            instance = new KafkaEventListenerProvider(bootstrapServers, username, password, clientId, topicEvents, events, topicAdminEvents);
        }

        return instance;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void init(Scope config) {
        LOG.info("Init kafka module ...");
        LOG.info(config.get("bootstrapServers"));
        topicEvents = config.get("topicEvents");
        clientId = config.get("clientId", "keycloak");
        bootstrapServers = config.get("bootstrapServers");
        username = config.get("username");
        password = config.get("password");
        topicAdminEvents = config.get("topicAdminEvents");

        String eventsString = config.get("events");
        events = eventsString.split(",");

        if (topicEvents == null) {
            throw new NullPointerException("topic must not be null.");
        }

        if (clientId == null) {
            throw new NullPointerException("clientId must not be null.");
        }

        if (bootstrapServers == null) {
            throw new NullPointerException("bootstrapServers must not be null");
        }

        if (username == null) {
            throw new NullPointerException("username must not be null");
        }

        if (password == null) {
            throw new NullPointerException("password must not be null");
        }

        if (events.length == 0) {
            events = new String[1];
            events[0] = "REGISTER";
        }
    }

    @Override
    public void postInit(KeycloakSessionFactory arg0) {
        // ignore
    }

    @Override
    public void close() {
        // ignore
    }
}