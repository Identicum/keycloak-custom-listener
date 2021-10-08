package com.identicum.keycloak;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jboss.logging.Logger;
import org.keycloak.Config.Scope;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

import static com.identicum.http.HttpTools.closeQuietly;
import static org.jboss.logging.Logger.getLogger;

public class CustomEventListenerProviderFactory implements EventListenerProviderFactory  {

	private static final Logger logger = getLogger(CustomEventListenerProviderFactory.class);

	private RemoteSsoHandler remoteSsoHandler;
	private PoolingHttpClientConnectionManager poolingHttpClientConnectionManager;
	private Boolean statsEnabled;

	@Override
	public EventListenerProvider create(KeycloakSession keycloakSession) {
		return new CustomEventListenerProvider(keycloakSession, this.remoteSsoHandler, this.poolingHttpClientConnectionManager, this.statsEnabled);
	}

	@Override
	public void init(Scope config) {
		String endpoint = config.get("apiEndpoint");
		Integer maxConnections = config.getInt("apiMaxConnections", 10);
		Integer connectionRequestTimeout = config.getInt("apiConnectionRequestTimeout", 2000);
		Integer connectTimeout = config.getInt("apiConnectTimeout", 2000);
		Integer socketTimeout = config.getInt("apiSocketTimeout", 2000);
		String statsEnabledString = config.get("httpStatsEnabled", "No");
		this.statsEnabled = statsEnabledString.equals("Yes");
		logger.infov("Initializing HTTP pool with API endpoint: {0}, maxConnections: {1}, connectionRequestTimeout: {2}, connectTimeout: {3}, socketTimeout: {4}", endpoint, maxConnections, connectionRequestTimeout, connectTimeout, socketTimeout);
		this.poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
		this.poolingHttpClientConnectionManager.setMaxTotal(maxConnections);
		this.poolingHttpClientConnectionManager.setDefaultMaxPerRoute(maxConnections);
		this.poolingHttpClientConnectionManager.setDefaultSocketConfig(SocketConfig.custom()
			.setSoTimeout(socketTimeout)
			.build());
		RequestConfig requestConfig = RequestConfig.custom()
			.setConnectTimeout(connectTimeout)
			.setConnectionRequestTimeout(connectionRequestTimeout)
			.build();
		CloseableHttpClient httpClient = HttpClients.custom()
			.setDefaultRequestConfig(requestConfig)
			.setConnectionManager(this.poolingHttpClientConnectionManager)
			.build();
		this.remoteSsoHandler = new RemoteSsoHandler(httpClient, endpoint);
	}

	@Override
	public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
	}

	@Override
	public void close() {
		if( this.remoteSsoHandler != null) {
			closeQuietly( this.remoteSsoHandler.getHttpClient() );
		}
	}

	@Override
	public String getId() {
		return "custom-event-listener";
	}
}
