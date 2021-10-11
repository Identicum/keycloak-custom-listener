package com.identicum.keycloak;

import com.identicum.http.SimpleHttpResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jboss.logging.Logger;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import static com.identicum.http.HttpTools.executeCall;
import static com.identicum.http.HttpTools.stopOnError;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.apache.http.protocol.HTTP.CONN_DIRECTIVE;
import static org.apache.http.protocol.HTTP.CONN_KEEP_ALIVE;
import static org.jboss.logging.Logger.getLogger;


@Getter
@AllArgsConstructor
public class RemoteSsoHandler {

	private static final Logger logger = getLogger(RemoteSsoHandler.class);

	private CloseableHttpClient httpClient;
	private String endpoint;
	
	public JsonObject registerUser(String username, String realm) {
		logger.infov("Registering user {0}", username);

		HttpPost httpPost = new HttpPost(this.endpoint);
		httpPost.setHeader(CONN_DIRECTIVE, CONN_KEEP_ALIVE);
		httpPost.setHeader("Content-Type", "application/json");

		JsonObjectBuilder builder = createObjectBuilder();
		builder.add("user" , createObjectBuilder().add("loginname", createArrayBuilder().add(username).build()).build());

		JsonObject requestJson = builder.build();
		logger.debugv("Setting create body as: {0}", requestJson.toString());
		HttpEntity httpEntity = new ByteArrayEntity(requestJson.toString().getBytes());
		httpPost.setEntity(httpEntity);

		logger.debugv("Executing call for user {0}", username);
		SimpleHttpResponse response = executeCall(this.httpClient, httpPost);
		logger.debugv("Checking response for user {0}", username);
		stopOnError(response);
		logger.debugv("Returning response for user {0}", username);
		return response.getResponseAsJsonObject();
	}

}
