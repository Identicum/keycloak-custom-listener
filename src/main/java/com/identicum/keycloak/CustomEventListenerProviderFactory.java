package com.identicum.keycloak;

import com.identicum.http.HttpTools;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class CustomEventListenerProviderFactory implements EventListenerProviderFactory  {

	private static final Logger logger = Logger.getLogger(CustomEventListenerProviderFactory.class);

	private RemoteSsoHandler remoteSsoHandler;

	@Override
	public EventListenerProvider create(KeycloakSession keycloakSession) {
		return new CustomEventListenerProvider(keycloakSession, this.remoteSsoHandler);
	}

	@Override
	public void init(Config.Scope config) {
		String endpoint = config.get("apiEndpoint");
		Integer maxConnections = config.getInt("apiMaxConnections", 10);
		Integer timeout = config.getInt("apiTimeout", 2000);
		logger.infov("Initializing HTTP pool with API endpoint: {0}, maxConnections: {1}, timeout: {2}", endpoint, maxConnections, timeout);
		PoolingHttpClientConnectionManager poolingConnManager = new PoolingHttpClientConnectionManager();
		poolingConnManager.setDefaultMaxPerRoute(maxConnections);
		poolingConnManager.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(timeout).build());
		CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(poolingConnManager).build();
		this.remoteSsoHandler = new RemoteSsoHandler(httpClient, endpoint);
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
