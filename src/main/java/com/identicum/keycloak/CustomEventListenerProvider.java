package com.identicum.keycloak;

import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;

import java.util.Map;

import static org.jboss.logging.Logger.getLogger;
import static org.keycloak.events.EventType.LOGIN;
import static org.keycloak.events.EventType.REGISTER;

public class CustomEventListenerProvider implements EventListenerProvider {

	private static final Logger logger = getLogger(CustomEventListenerProvider.class);
	private RemoteSsoHandler handler;

	public CustomEventListenerProvider(KeycloakSession session, RemoteSsoHandler handler) {
		logger.infov("Initializing CustomEventListenerProvider.");
		this.handler = handler;
	}

	@Override
	public void onEvent(Event event) {
		logger.debugv("onEvent(Event): {0}", toString(event));
		if(REGISTER.equals(event.getType())) {
			String username = event.getDetails().get("username");
			logger.infov("Username created: {0}", username);
			this.publishEvent(username, event);
		}
		else if(LOGIN.equals(event.getType())) {
			String username = event.getDetails().get("username");
			logger.debugv("User logged in: {0}", username);
			this.publishEvent(username, event);
		}
	}

	@Override
	public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {
		logger.tracev("onEvent (AdminEvent): {0}", toString(adminEvent));
	}

	@Override
	public void close() {

	}

	private void publishEvent(String username, Event event) {
		try {
			this.handler.registerUser(username, event.getRealmId());
		} catch (Exception e) {
			logger.errorv("Error publishing user event {0}: {1}", username, e);
		}
	}

	private String toString(Event event) {
		StringBuilder sb = new StringBuilder();
		sb.append("type=");
		sb.append(event.getType());
		sb.append(", realmId=");
		sb.append(event.getRealmId());
		sb.append(", clientId=");
		sb.append(event.getClientId());
		sb.append(", userId=");
		sb.append(event.getUserId());
		sb.append(", ipAddress=");
		sb.append(event.getIpAddress());
		if (event.getError() != null) {
			sb.append(", error=");
			sb.append(event.getError());
		}
		if (event.getDetails() != null) {
			for (Map.Entry<String, String> e : event.getDetails().entrySet()) {
				sb.append(", ");
				sb.append(e.getKey());
				if (e.getValue() == null || e.getValue().indexOf(' ') == -1) {
					sb.append("=");
					sb.append(e.getValue());
				} else {
					sb.append("='");
					sb.append(e.getValue());
					sb.append("'");
				}
			}
		}
		return sb.toString();
	}

	private String toString(AdminEvent adminEvent) {
		StringBuilder sb = new StringBuilder();
		sb.append("operationType=");
		sb.append(adminEvent.getOperationType());
		sb.append(", realmId=");
		sb.append(adminEvent.getAuthDetails().getRealmId());
		sb.append(", clientId=");
		sb.append(adminEvent.getAuthDetails().getClientId());
		sb.append(", userId=");
		sb.append(adminEvent.getAuthDetails().getUserId());
		sb.append(", ipAddress=");
		sb.append(adminEvent.getAuthDetails().getIpAddress());
		sb.append(", resourcePath=");
		sb.append(adminEvent.getResourcePath());
		if (adminEvent.getError() != null) {
			sb.append(", error=");
			sb.append(adminEvent.getError());
		}
		return sb.toString();
	}
}
