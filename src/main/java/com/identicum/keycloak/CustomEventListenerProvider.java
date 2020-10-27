package com.identicum.keycloak;

import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import javax.json.JsonObject;
import java.util.List;
import java.util.Map;

public class CustomEventListenerProvider implements EventListenerProvider {

	private static final Logger logger = Logger.getLogger(CustomEventListenerProvider.class);
	private KeycloakSession session;
	private RemoteSsoHandler handler;

	public CustomEventListenerProvider(KeycloakSession session, RemoteSsoHandler handler) {
		logger.infov("Initializing CustomEventListenerProvider.");
		this.session = session;
		this.handler = handler;
	}

	@Override
	public void onEvent(Event event) {
		logger.tracev("onEvent: {0}", toString(event));
		if(EventType.REGISTER.equals(event.getType())) {
			logger.infov("Matched realm: {0}", event.getRealmId());
			String username = event.getDetails().get("username");
			logger.infov("Username created: {0}", username);
			try {
				JsonObject response = this.handler.registerUser(username, event.getRealmId());
				RealmModel realm = session.realms().getRealm(event.getRealmId());
				UserModel user = session.users().getUserById(event.getUserId(), realm);
				user.setSingleAttribute("ssoId", response.get("user").asJsonObject().getString("id"));
			} catch (Exception e) {
				logger.errorv("Error registering user {0}: ", username, e);
			}
		}
	}

	@Override
	public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {
		logger.tracev("onEvent (admin): {0}", toString(adminEvent));
	}

	@Override
	public void close() {

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
