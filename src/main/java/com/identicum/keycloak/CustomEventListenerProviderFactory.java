package com.identicum.keycloak;

import com.identicum.http.HttpTools;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class CustomEventListenerProviderFactory implements EventListenerProviderFactory  {

	private RemoteSsoHandler remoteSsoHandler;

	@Override
	public EventListenerProvider create(KeycloakSession keycloakSession) {
		return new CustomEventListenerProvider(keycloakSession, this.remoteSsoHandler);
	}

	@Override
	public void init(Config.Scope config) {
		Integer maxConnections = config.getInt("apiMaxConnections", 10);
		PoolingHttpClientConnectionManager poolingConnManager = new PoolingHttpClientConnectionManager();
		poolingConnManager.setDefaultMaxPerRoute(maxConnections);
		CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(poolingConnManager).build();
		this.remoteSsoHandler = new RemoteSsoHandler(httpClient, config.get("apiEndpoint"));
	}

	@Override
	public void postInit(KeycloakSessionFactory keycloakSessionFactory) {

	}

	@Override
	public void close() {
		if( this.remoteSsoHandler != null) {
			HttpTools.closeQuietly( this.remoteSsoHandler.getHttpClient() );
		}
	}

	@Override
	public String getId() {
		return "custom-event-listener";
	}
}
