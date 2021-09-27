package com.identicum.keycloak;

import com.identicum.http.HttpTools;
import org.apache.http.client.config.RequestConfig;
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
		Integer connectionRequestTimeout = config.getInt("apiConnectionRequestTimeout", 2000);
		Integer connectTimeout = config.getInt("apiConnectTimeout", 2000);
		Integer socketTimeout = config.getInt("apiSocketTimeout", 2000);
		String statsEnabledString = config.get("httpStatsEnabled", "No");
		Boolean statsEnabled = statsEnabledString.equals("Yes");
		logger.infov("Initializing HTTP pool with API endpoint: {0}, maxConnections: {1}, connectionRequestTimeout: {2}, connectTimeout: {3}, socketTimeout: {4}", endpoint, maxConnections, connectionRequestTimeout, connectTimeout, socketTimeout);
		PoolingHttpClientConnectionManager poolingConnManager = new PoolingHttpClientConnectionManager();
		poolingConnManager.setMaxTotal(maxConnections);
		poolingConnManager.setDefaultMaxPerRoute(maxConnections);
		poolingConnManager.setDefaultSocketConfig(SocketConfig.custom()
			.setSoTimeout(socketTimeout)
			.build());
		RequestConfig requestConfig = RequestConfig.custom()
			.setConnectTimeout(connectTimeout)
			.setConnectionRequestTimeout(connectionRequestTimeout)
			.build();
		CloseableHttpClient httpClient = HttpClients.custom()
			.setDefaultRequestConfig(requestConfig)
			.setConnectionManager(poolingConnManager)
			.build();
		this.remoteSsoHandler = new RemoteSsoHandler(httpClient, endpoint, statsEnabled);
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
