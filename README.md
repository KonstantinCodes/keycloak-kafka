# keycloak-kafka
Push Keycloak Events to Kafka

# Build
```bash
mvn clean install
mvn clean package
```

# Run kafka.cli to set up config parameters
Execute following command to enable the plugin and add configuration:
```bash
/opt/jboss/keycloak/bin/jboss-cli.sh --file=/opt/jboss/keycloak/standalone/configuration/kafka.cli
```

