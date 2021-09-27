package com.identicum.keycloak;

import com.identicum.http.HttpTools;
import com.identicum.http.SimpleHttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.PoolStats;
import org.apache.http.protocol.HTTP;
import org.jboss.logging.Logger;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class RemoteSsoHandler {

	private static final Logger logger = Logger.getLogger(RemoteSsoHandler.class);

	private String endpoint;
	private CloseableHttpClient httpClient;
	private Boolean statsEnabled;

	public RemoteSsoHandler(CloseableHttpClient client, String endpoint, Boolean statsEnabled) {
		this.endpoint = endpoint;
		this.httpClient = client;
		this.statsEnabled = statsEnabled;
	}

	public CloseableHttpClient getHttpClient() {
		return httpClient;
	}

	public Boolean isStatsEnabled() {
		return this.statsEnabled;
	}

	public Map getStats() {
		HashMap<String, Integer> stats = new HashMap<>();
		PoolStats poolStats = this.poolingHttpClientConnectionManager.getTotalStats();
		stats.put("availableConnections", poolStats.getAvailable());
		stats.put("maxConnections", poolStats.getMax());
		stats.put("leasedConnections", poolStats.getLeased());
		stats.put("pendingConnections", poolStats.getPending());
		stats.put("defaultMaxPerRoute", this.poolingHttpClientConnectionManager.getDefaultMaxPerRoute());
		return stats;
	}
	
	public JsonObject registerUser(String username, String realm) {
		logger.infov("Registering user {0}", username);

		HttpPost httpPost = new HttpPost(this.endpoint );
		httpPost.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);
		httpPost.setHeader("Content-Type", "application/json");

		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("user" , Json.createObjectBuilder()
			.add("loginname", Json.createArrayBuilder().add(username).build())
			.build()
		);

		JsonObject requestJson = builder.build();
		logger.debugv("Setting create body as: {0}", requestJson.toString());
		HttpEntity httpEntity = new ByteArrayEntity(requestJson.toString().getBytes());
		httpPost.setEntity(httpEntity);

		logger.debugv("Executing call for user {0}", username);
		SimpleHttpResponse response = HttpTools.executeCall(this.httpClient, httpPost);
		logger.debugv("Checking response for user {0}", username);
		HttpTools.stopOnError(response);
		logger.debugv("Returning response for user {0}", username);
		return response.getResponseAsJsonObject();
	}

}
