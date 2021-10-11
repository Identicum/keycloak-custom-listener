# keycloak-custom-listener

Keycloak repo provider SPI to call an API on specific events.

The example triggers on USER_LOGIN and USER_REGISTER

## Compile module
```sh
mvn clean install
```

## Run project
```sh
docker-compose up
```

## Test
- Navigate to http://localhost:8080/auth/realms/customlistener/account
- Select `Sign In`
- Register a new user
- Sign Out
- Sign In again, as the newly registered user

## How it works
<TODO>

## Realm configuration
A realm is automatically imported to simplify testing.

The imported realm has the following configuration:
- Manage -> Events -> "Config" tab -> Add the Event Listener (it should be displayed on the autocomplete menu)

## Standalone.xml configuration
The custom event listener has to be declared in the standalone.xml
In the sample configuration, this is done automatically, configured in ./startup-scripts/custom.cli
```xml
<spi name="eventsListener">
    <provider name="custom-event-listener" enabled="true">
        <properties>
            <property name="apiEndpoint" value="http://my.api.com/api/v1/endpoint"/>
            <property name="apiMaxConnections" value="10"/>
            <property name="apiConnectionRequestTimeout" value="2000"/>
            <property name="apiConnectTimeout" value="1000"/>
            <property name="apiSocketTimeout" value="1500"/>
            <property name="httpStatsInterval" value="60"/>
        </properties>
    </provider>
</spi>
```

## Troubleshooting
- Keycloak log should detail module activity, configured in ./startup-scripts/custom.cli

- Login to the admin console at http://localhost:8080/auth/admin/
  - username: admin
  - password: admin
