package com.identicum.keycloak;

import com.identicum.http.HttpStats;
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

import java.util.Timer;

import static com.identicum.http.Constants.API_CONNECTION_REQUEST_TIMEOUT_DEFAULT;
import static com.identicum.http.Constants.API_CONNECT_TIMEOUT_DEFAULT;
import static com.identicum.http.Constants.API_MAX_CONNECTIONS_DEFAULT;
import static com.identicum.http.Constants.API_SOCKET_TIMEOUT_DEFAULT;
import static com.identicum.http.Constants.HTTP_STATS_INTERVAL_DEFAULT;
import static com.identicum.http.Constants.TO_MILLISECONDS;
import static com.identicum.http.HttpTools.closeQuietly;
import static org.jboss.logging.Logger.getLogger;

public class CustomEventListenerProviderFactory implements EventListenerProviderFactory  {

	private static final Logger logger = getLogger(CustomEventListenerProviderFactory.class);

	private RemoteSsoHandler remoteSsoHandler;
	private PoolingHttpClientConnectionManager poolingHttpClientConnectionManager;
	private Integer httpStatsInterval;
	private Timer httpStats;

	@Override
	public EventListenerProvider create(KeycloakSession keycloakSession) {
		return new CustomEventListenerProvider(keycloakSession, this.remoteSsoHandler);
	}

	@Override
	public void init(Scope config) {
		String endpoint = config.get("apiEndpoint");
		Integer maxConnections = config.getInt("apiMaxConnections", API_MAX_CONNECTIONS_DEFAULT);
		Integer connectionRequestTimeout = config.getInt("apiConnectionRequestTimeout", API_CONNECTION_REQUEST_TIMEOUT_DEFAULT);
		Integer connectTimeout = config.getInt("apiConnectTimeout", API_CONNECT_TIMEOUT_DEFAULT);
		Integer socketTimeout = config.getInt("apiSocketTimeout", API_SOCKET_TIMEOUT_DEFAULT);
		this.httpStatsInterval = config.getInt("httpStatsInterval", HTTP_STATS_INTERVAL_DEFAULT);
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
		if(httpStatsInterval > 0){
			this.httpStats = new Timer();
			this.httpStats.schedule(new HttpStats(logger, poolingHttpClientConnectionManager), 0, httpStatsInterval * TO_MILLISECONDS);
		}
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
