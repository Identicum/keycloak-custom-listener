# Keycloak configuration

## Add module parameters

* Edit standalone.xml file
* Add this node:

```xml
<spi name="eventsListener">
    <provider name="custom-event-listener" enabled="true">
        <properties>
            <property name="apiEndpoint" value="https://e5fdaf1155f7a2e9feeae00b482ae056.m.pipedream.net"/>
            <property name="apiMaxConnections" value="10"/>
        </properties>
    </provider>
</spi>
```

* Save and restart Keycloak

## Add the listener to the desired realm 

* Go to Manage - Events on the left bar
* Click con "Config" tab
* On the Event Config section add the Event Listener (it should be displayed on the autocomplete menu)