package com.identicum.http;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.net.SocketTimeoutException;

import static java.lang.String.format;
import static java.util.stream.Stream.of;
import static org.apache.http.HttpHeaders.CONNECTION;
import static org.apache.http.protocol.HTTP.CONN_KEEP_ALIVE;
import static org.jboss.logging.Logger.getLogger;

public class HttpTools {

	private static final Logger logger = getLogger(HttpTools.class);

	/**
	 * Execute http request with the connection pool and handle the received response.
	 * If the response status is not OK it throws a {@link RuntimeException} to stop the flow.
	 *
	 * @param request Request to be executed with all needed headers.
	 * @return SimpleHttpResponse with code received and body
	 * @throws RuntimeException if status code received is not 200
	 */
	public static SimpleHttpResponse executeCall(CloseableHttpClient httpClient, HttpRequestBase request) {
		logger.debugv("Executing Http Request [{0}] on [{1}]", request.getMethod(), request.getURI());
		request.setHeader(CONNECTION, CONN_KEEP_ALIVE);

		of( request.getAllHeaders() ).forEach(header -> logger.debugv("Request header: {0} -> {1}", header.getName(), header.getValue() ));
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(request);
			String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
			logger.debugv("Response code obtained from server: {0}", response.getStatusLine().getStatusCode());
			logger.debugv("Response body obtained from server: {0}", responseString);
			return new SimpleHttpResponse(response.getStatusLine().getStatusCode(), responseString);
		}
		catch(ConnectionPoolTimeoutException cpte) {
			throw new RuntimeException(format("Connection pool timeout exception: %s", cpte), cpte);
		}
		catch(ConnectTimeoutException cte) {
			throw new RuntimeException(format("Connect timeout exception: %s", cte), cte);
		}
		catch(SocketTimeoutException ste) {
			throw new RuntimeException(format("Socket timeout exception: %s", ste), ste);
		}
		catch(IOException io) {
			throw new RuntimeException(format("Error executing request: %s", io), io);
		}
		finally {
			closeQuietly(response);
		}
	}

	public static void stopOnError(SimpleHttpResponse response) {
		if(!response.isSuccess()) {
			throw new RuntimeException(
					format(
							"Response status code was not success. Code received: %s\n" +
									"Response received: %s\n" +
									"Http Request was not success. Check logs to get more information",
							response.getStatus(), response.getResponse()
					)
			);
		}
	}

	/**
	 * Close quietly a http response
	 * @param response Response to be closed
	 */
	public static void closeQuietly(CloseableHttpResponse response) {
		if (response != null)
			try {
				response.close();
			} catch (IOException io) {
				logger.warn("Error closing http response", io);
			}
	}

	/**
	 * Close quietly a http client
	 * @param client HttpClient to be closed
	 */
	public static void closeQuietly(CloseableHttpClient client) {
		if (client != null)
			try {
				client.close();
			} catch (IOException io) {
				logger.warn("Error closing http response", io);
			}
	}
}
