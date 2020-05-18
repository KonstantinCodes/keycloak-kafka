# keycloak-kafka
Push Keycloak Events to Kafka

# Build & deploy
```bash
mvn clean install
mvn clean package
cp ./ear/target/kafka-events-bundle-1.0-SNAPSHOT.ear /opt/jboss/keycloak/standalone/deployments/
```

# Run kafka.cli to set up config parameters
Execute following command to enable the plugin and add configuration:
```bash
/opt/jboss/keycloak/bin/jboss-cli.sh --file=/opt/jboss/keycloak/standalone/configuration/kafka.cli
```

