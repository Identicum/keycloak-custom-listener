package com.identicum.keycloak;

import com.identicum.http.HttpTools;
import com.identicum.http.SimpleHttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HTTP;
import org.jboss.logging.Logger;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class RemoteSsoHandler {

	private static final Logger logger = Logger.getLogger(RemoteSsoHandler.class);

	private String endpoint;
	private CloseableHttpClient httpClient;

	public RemoteSsoHandler(CloseableHttpClient client, String endpoint) {
		this.endpoint = endpoint;
		this.httpClient = client;
	}

	public CloseableHttpClient getHttpClient() {
		return httpClient;
	}

	public JsonObject registerUser(String username, String realm) {
		logger.infov("Registering user {0}", username);

		HttpPost httpPost = new HttpPost(this.endpoint );
		httpPost.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);
		httpPost.setHeader("Content-Type", "application/json");

		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("source", "AUP");
		builder.add("user" , Json.createObjectBuilder()
			.add("loginname", Json.createArrayBuilder().add(username).build())
			.add("locale", getLocaleByRealm(realm))
			.build()
		);
		builder.add("parameters", Json.createArrayBuilder()
			.add(Json.createObjectBuilder()
					.add("key", "Version")
					.add("value", "Legacy")
					.add("class", "java.lang.String")
					.build()
			).build()
		);

		JsonObject requestJson = builder.build();
		logger.infov("Setting create body as: {0}", requestJson.toString());
		HttpEntity httpEntity = new ByteArrayEntity(requestJson.toString().getBytes());
		httpPost.setEntity(httpEntity);

		SimpleHttpResponse response = HttpTools.executeCall(this.httpClient, httpPost);
		HttpTools.stopOnError(response);
		return response.getResponseAsJsonObject();
	}

	private String getLocaleByRealm(String realm) {
		switch (realm){
			case "claro":
				return "es-AR";
			case "claro-py":
				return "es-PY";
			case "claro-uy":
				return "es-UY";
			default:
				return "es-AR";
		}
	}
}
